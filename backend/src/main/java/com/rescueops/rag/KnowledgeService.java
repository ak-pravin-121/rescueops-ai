package com.rescueops.rag;

import com.rescueops.ai.GeminiService;
import com.rescueops.dto.KnowledgeDtos.DocumentResponse;
import com.rescueops.dto.KnowledgeDtos.SearchResponse;
import com.rescueops.dto.KnowledgeDtos.SearchResult;
import com.rescueops.dto.KnowledgeDtos.UploadDocumentRequest;
import com.rescueops.entity.KnowledgeChunk;
import com.rescueops.entity.KnowledgeDocument;
import com.rescueops.entity.User;
import com.rescueops.exception.ResourceNotFoundException;
import com.rescueops.repository.KnowledgeChunkRepository;
import com.rescueops.repository.KnowledgeDocumentRepository;
import com.rescueops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final int MAX_CHUNK_CHARS = 1200;
    private static final int TOP_K = 5;

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    private final GeminiService geminiService;

    @Transactional
    public DocumentResponse uploadDocument(UploadDocumentRequest request, Long uploadedById) {
        User uploader = uploadedById != null ? userRepository.findById(uploadedById).orElse(null) : null;

        KnowledgeDocument document = KnowledgeDocument.builder()
                .name(request.getName())
                .sourceType(request.getSourceType())
                .rawContent(request.getContent())
                .uploadedBy(uploader)
                .build();
        document = documentRepository.save(document);

        List<String> chunks = chunkText(request.getContent());
        int index = 0;
        int embedded = 0;
        for (String chunkContent : chunks) {
            double[] vector = embeddingService.embed(chunkContent);
            if (vector == null) {
                // Skip storing un-embeddable chunks rather than polluting the vector store with zeros.
                index++;
                continue;
            }
            KnowledgeChunk chunk = KnowledgeChunk.builder()
                    .document(document)
                    .chunkIndex(index++)
                    .content(chunkContent)
                    .embedding(VectorMath.serialize(vector))
                    .build();
            chunkRepository.save(chunk);
            embedded++;
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .sourceType(document.getSourceType())
                .chunkCount(embedded)
                .createdAt(document.getCreatedAt())
                .build();
    }

    public SearchResponse search(String query) {
        List<SearchResult> matches = findSimilarChunks(query, TOP_K);

        if (matches.isEmpty()) {
            return SearchResponse.builder()
                    .answer("No related material was found in the knowledge base yet. Try uploading SOPs, " +
                            "architecture docs or past incident write-ups first.")
                    .sources(matches)
                    .build();
        }

        String context = matches.stream()
                .map(m -> "[" + m.getDocumentName() + "] " + m.getChunkContent())
                .collect(Collectors.joining("\n---\n"));

        String prompt = """
                You are RescueOps AI's knowledge assistant. Using ONLY the context snippets below from the \
                team's knowledge base, answer the question. If the context doesn't contain the answer, say so \
                plainly instead of guessing. Be concise (max 150 words) and reference which document a fact came from.

                Question: %s

                Context:
                %s
                """.formatted(query, context);

        String answer = geminiService.generateText(prompt, 0.3);

        return SearchResponse.builder().answer(answer).sources(matches).build();
    }

    /** Used by the Root Cause Agent to ground itself in similar past incidents before forming a hypothesis. */
    public String findSimilarPastIncidentsContext(String incidentTitle, String incidentDescription) {
        List<SearchResult> matches = findSimilarChunks(incidentTitle + " - " + incidentDescription, 3);
        if (matches.isEmpty()) {
            return "";
        }
        return matches.stream()
                .map(m -> "[" + m.getDocumentName() + "] " + m.getChunkContent())
                .collect(Collectors.joining("\n---\n"));
    }

    private List<SearchResult> findSimilarChunks(String query, int topK) {
        double[] queryVector = embeddingService.embed(query);
        if (queryVector == null) {
            return List.of();
        }

        List<KnowledgeChunk> allChunks = chunkRepository.findAll();
        if (allChunks.isEmpty()) {
            return List.of();
        }

        List<SearchResult> scored = new ArrayList<>();
        for (KnowledgeChunk chunk : allChunks) {
            double[] chunkVector = VectorMath.deserialize(chunk.getEmbedding());
            double similarity = VectorMath.cosineSimilarity(queryVector, chunkVector);
            scored.add(SearchResult.builder()
                    .documentId(chunk.getDocument().getId())
                    .documentName(chunk.getDocument().getName())
                    .chunkContent(chunk.getContent())
                    .similarity(similarity)
                    .build());
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(SearchResult::getSimilarity).reversed())
                .limit(topK)
                .filter(r -> r.getSimilarity() > 0.3)
                .toList();
    }

    public DocumentResponse getDocument(Long id) {
        KnowledgeDocument document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found: " + id));
        int chunkCount = chunkRepository.findByDocument_Id(id).size();
        return DocumentResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .sourceType(document.getSourceType())
                .chunkCount(chunkCount)
                .createdAt(document.getCreatedAt())
                .build();
    }

    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAll().stream()
                .map(doc -> DocumentResponse.builder()
                        .id(doc.getId())
                        .name(doc.getName())
                        .sourceType(doc.getSourceType())
                        .chunkCount(chunkRepository.findByDocument_Id(doc.getId()).size())
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }

    private List<String> chunkText(String text) {
        List<String> paragraphs = new ArrayList<>();
        for (String para : text.split("\\n\\s*\\n")) {
            String trimmed = para.trim();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }
        if (paragraphs.isEmpty()) {
            paragraphs.add(text.trim());
        }

        List<String> chunks = new ArrayList<>();
        for (String para : paragraphs) {
            if (para.length() <= MAX_CHUNK_CHARS) {
                chunks.add(para);
            } else {
                for (int start = 0; start < para.length(); start += MAX_CHUNK_CHARS) {
                    int end = Math.min(start + MAX_CHUNK_CHARS, para.length());
                    chunks.add(para.substring(start, end));
                }
            }
        }
        return chunks;
    }
}
