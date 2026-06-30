package com.rescueops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public class DashboardDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardSummary {
        private long activeIncidents;
        private long resolvedLast30Days;
        private long totalIncidents;
        private double resolutionRatePercent;
        private Map<String, Long> severityDistribution;
        private Map<String, Long> statusDistribution;
    }
}
