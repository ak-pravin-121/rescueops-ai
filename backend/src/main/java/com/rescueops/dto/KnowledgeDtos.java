package com.rescueops.dto;

import com.rescueops.entity.DocumentSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

public class KnowledgeDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadDocumentRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Source type is required")
        private DocumentSourceType sourceType;

        @NotBlank(message = "Content is required")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentResponse {
        private Long id;
        private String name;
        private DocumentSourceType sourceType;
        private int chunkCount;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        @NotBlank(message = "Query is required")
        private String query;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private Long documentId;
        private String documentName;
        private String chunkContent;
        private double similarity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResponse {
        private String answer;
        private List<SearchResult> sources;
    }
}
