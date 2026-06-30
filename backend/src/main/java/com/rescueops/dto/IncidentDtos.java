package com.rescueops.dto;

import com.rescueops.entity.IncidentStatus;
import com.rescueops.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class IncidentDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateIncidentRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Service name is required")
        private String serviceName;

        /** Optional - if omitted, defaults to MEDIUM until AI analysis runs. */
        private Severity severity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateIncidentRequest {
        private String title;
        private String description;
        private Severity severity;
        private IncidentStatus status;
        private Long assignedToId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddFileRequest {
        @NotBlank(message = "File name is required")
        private String fileName;

        @NotNull(message = "File type is required")
        private String fileType;

        @NotBlank(message = "File content is required")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentResponse {
        private Long id;
        private String title;
        private String description;
        private String serviceName;
        private Severity severity;
        private IncidentStatus status;
        private String rootCause;
        private String suggestedFix;
        private Long createdById;
        private String createdByName;
        private Long assignedToId;
        private String assignedToName;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant resolvedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentFileResponse {
        private Long id;
        private Long incidentId;
        private String fileName;
        private String fileType;
        private String content;
        private Instant createdAt;
    }
}
