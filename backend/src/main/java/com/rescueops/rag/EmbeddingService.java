package com.rescueops.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wraps Gemini's embedContent endpoint to turn text into a dense vector.
 * Used both when indexing knowledge base chunks and when embedding a search query.
 */
@Slf4j
@Service
public class EmbeddingService {

    private static final int OUTPUT_DIMENSIONS = 768;

    private final RestTemplate restTemplate;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.embedding-model:gemini-embedding-001}")
    private String embeddingModel;

    @Value("${gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiBaseUrl;

    public EmbeddingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Returns the embedding as a double[], or null if the API key is missing or the call fails.
     * Callers must handle null (e.g. by skipping semantic search and falling back to plain storage).
     */
    @SuppressWarnings("unchecked")
    public double[] embed(String text) {
        if (!isConfigured()) {
            return null;
        }
        try {
            String url = apiBaseUrl + "/" + embeddingModel + ":embedContent";

            Map<String, Object> part = new HashMap<>();
            part.put("text", text);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));

            Map<String, Object> embedConfig = new HashMap<>();
            embedConfig.put("outputDimensionality", OUTPUT_DIMENSIONS);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "models/" + embeddingModel);
            body.put("content", content);
            body.put("embedContentConfig", embedConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
            if (response == null) {
                return null;
            }

            Map<String, Object> embedding = (Map<String, Object>) response.get("embedding");
            if (embedding == null) {
                return null;
            }

            List<Object> values = (List<Object>) embedding.get("values");
            if (values == null) {
                return null;
            }

            double[] vector = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = ((Number) values.get(i)).doubleValue();
            }
            return vector;

        } catch (Exception ex) {
            log.error("Gemini embedContent call failed: {}", ex.getMessage());
            return null;
        }
    }
}
