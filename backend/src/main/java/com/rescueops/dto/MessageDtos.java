package com.rescueops.dto;

import com.rescueops.entity.MessageSenderType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class MessageDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendMessageRequest {
        @NotBlank(message = "Message is required")
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long id;
        private Long incidentId;
        private Long senderId;
        private String senderName;
        private MessageSenderType senderType;
        private String message;
        private Instant createdAt;
    }
}
