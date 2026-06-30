package com.rescueops.service;

import com.rescueops.dto.DashboardDtos.DashboardSummary;
import com.rescueops.entity.IncidentStatus;
import com.rescueops.entity.Severity;
import com.rescueops.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncidentRepository incidentRepository;

    public DashboardSummary getSummary() {
        long total = incidentRepository.count();
        long open = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long acknowledged = incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);
        long investigating = incidentRepository.countByStatus(IncidentStatus.INVESTIGATING);
        long resolved = incidentRepository.countByStatus(IncidentStatus.RESOLVED);
        long closed = incidentRepository.countByStatus(IncidentStatus.CLOSED);

        long active = open + acknowledged + investigating;

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        long resolvedLast30Days = incidentRepository.findByCreatedAtAfter(thirtyDaysAgo).stream()
                .filter(i -> i.getStatus() == IncidentStatus.RESOLVED || i.getStatus() == IncidentStatus.CLOSED)
                .count();

        double resolutionRate = total == 0 ? 0.0 : Math.round(((double) (resolved + closed) / total) * 1000.0) / 10.0;

        Map<String, Long> severityDistribution = new LinkedHashMap<>();
        for (Severity s : Severity.values()) {
            severityDistribution.put(s.name(), incidentRepository.countBySeverity(s));
        }

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        statusDistribution.put(IncidentStatus.OPEN.name(), open);
        statusDistribution.put(IncidentStatus.ACKNOWLEDGED.name(), acknowledged);
        statusDistribution.put(IncidentStatus.INVESTIGATING.name(), investigating);
        statusDistribution.put(IncidentStatus.RESOLVED.name(), resolved);
        statusDistribution.put(IncidentStatus.CLOSED.name(), closed);

        return DashboardSummary.builder()
                .activeIncidents(active)
                .resolvedLast30Days(resolvedLast30Days)
                .totalIncidents(total)
                .resolutionRatePercent(resolutionRate)
                .severityDistribution(severityDistribution)
                .statusDistribution(statusDistribution)
                .build();
    }
}
