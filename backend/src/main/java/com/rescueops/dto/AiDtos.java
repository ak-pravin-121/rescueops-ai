package com.rescueops.dto;

import com.rescueops.entity.AnalysisType;
import com.rescueops.entity.Severity;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class AiDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentRequest {
        @NotNull(message = "incidentId is required")
        private Long incidentId;

        /** Optional extra log/context text to analyze in addition to what's already stored on the incident. */
        private String extraContext;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiAnalysisResponse {
        private Long id;
        private Long incidentId;
        private AnalysisType analysisType;
        private Severity severity;
        private String rootCause;
        private String suggestedFix;
        private Integer confidence;
        private String content;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunbookResponse {
        private Long id;
        private Long incidentId;
        private String title;
        private String content;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullTriageResponse {
        private AiAnalysisResponse analysis;
        private AiAnalysisResponse rootCause;
        private RunbookResponse runbook;
    }
}
