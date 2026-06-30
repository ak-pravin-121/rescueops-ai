package com.rescueops.controller;

import com.rescueops.dto.KnowledgeDtos.DocumentResponse;
import com.rescueops.dto.KnowledgeDtos.SearchRequest;
import com.rescueops.dto.KnowledgeDtos.SearchResponse;
import com.rescueops.dto.KnowledgeDtos.UploadDocumentRequest;
import com.rescueops.rag.KnowledgeService;
import com.rescueops.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(@Valid @RequestBody UploadDocumentRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(knowledgeService.uploadDocument(request, principal.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query) {
        return ResponseEntity.ok(knowledgeService.search(query));
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchPost(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(knowledgeService.search(request.getQuery()));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentResponse>> listDocuments() {
        return ResponseEntity.ok(knowledgeService.listDocuments());
    }
}
