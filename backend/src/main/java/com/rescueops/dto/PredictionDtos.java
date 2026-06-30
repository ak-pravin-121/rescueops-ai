package com.rescueops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public class PredictionDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionResponse {
        private Long id;
        private String serviceName;
        private Double riskScore;
        private String predictedFailure;
        private String reasoning;
        private Integer incidentCountLast30Days;
        private Double criticalRatioLast30Days;
        private Instant createdAt;
    }
}
