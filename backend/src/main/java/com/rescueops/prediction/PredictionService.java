package com.rescueops.prediction;

import com.rescueops.ai.GeminiService;
import com.rescueops.dto.PredictionDtos.PredictionResponse;
import com.rescueops.entity.Incident;
import com.rescueops.entity.Prediction;
import com.rescueops.entity.Severity;
import com.rescueops.repository.IncidentRepository;
import com.rescueops.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * This is a heuristic, statistics-driven engine, not a trained ML model: we compute real
 * frequency/severity signals from incident history, then ask Gemini to reason over those
 * numbers (and explain itself) rather than fabricating a "prediction" out of nothing.
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final IncidentRepository incidentRepository;
    private final PredictionRepository predictionRepository;
    private final GeminiService geminiService;

    @Transactional
    public PredictionResponse runPredictionForService(String serviceName) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Incident> recent = incidentRepository.findByServiceNameIgnoreCaseAndCreatedAtAfter(serviceName, thirtyDaysAgo);

        int incidentCount = recent.size();
        long criticalOrHigh = recent.stream()
                .filter(i -> i.getSeverity() == Severity.CRITICAL || i.getSeverity() == Severity.HIGH)
                .count();
        double criticalRatio = incidentCount == 0 ? 0.0 : (double) criticalOrHigh / incidentCount;

        double heuristicScore = heuristicRiskScore(incidentCount, criticalRatio);

        String historySummary = recent.isEmpty()
                ? "No incidents recorded for this service in the last 30 days."
                : recent.stream()
                .map(i -> "- [%s] %s (status: %s)".formatted(i.getSeverity(), i.getTitle(), i.getStatus()))
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                You are the Predictive Failure Engine for "%s". Based on this real incident history from the \
                last 30 days, estimate the risk that this service fails again soon.

                Incident count (30d): %d
                Critical/High ratio (30d): %.2f
                Statistical baseline risk score (0-100, already computed): %.1f
                Recent incidents:
                %s

                Respond with STRICT JSON only:
                {"riskScore": <integer 0-100, you may adjust the baseline slightly based on the pattern>, \
                "predictedFailure": "<short prediction of what is likely to fail next and when, 1 sentence>", \
                "reasoning": "<2-3 sentence explanation grounded in the numbers above>"}
                """.formatted(serviceName, incidentCount, criticalRatio, heuristicScore, historySummary);

        Map<String, Object> json = geminiService.generateJson(prompt, 0.3);

        double riskScore = heuristicScore;
        String predictedFailure = incidentCount == 0
                ? "No imminent failure predicted - insufficient recent incident history."
                : "Recurring failures may continue in " + serviceName + " without intervention.";
        String reasoning = "Computed from " + incidentCount + " incident(s) in the last 30 days with a "
                + Math.round(criticalRatio * 100) + "% critical/high ratio.";

        if (json != null) {
            Object riskObj = json.get("riskScore");
            if (riskObj != null) {
                try {
                    riskScore = Double.parseDouble(riskObj.toString());
                } catch (NumberFormatException ignored) {
                    // keep heuristic score
                }
            }
            if (json.get("predictedFailure") != null) {
                predictedFailure = json.get("predictedFailure").toString();
            }
            if (json.get("reasoning") != null) {
                reasoning = json.get("reasoning").toString();
            }
        }

        Prediction prediction = Prediction.builder()
                .serviceName(serviceName)
                .riskScore(Math.max(0, Math.min(100, riskScore)))
                .predictedFailure(predictedFailure)
                .reasoning(reasoning)
                .incidentCountLast30Days(incidentCount)
                .criticalRatioLast30Days(criticalRatio)
                .build();
        prediction = predictionRepository.save(prediction);

        return toResponse(prediction);
    }

    public List<PredictionResponse> getRecentPredictions() {
        return predictionRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public PredictionResponse getLatestForService(String serviceName) {
        return predictionRepository.findFirstByServiceNameIgnoreCaseOrderByCreatedAtDesc(serviceName)
                .map(this::toResponse)
                .orElse(null);
    }

    /** Statistical baseline: more incidents and a higher critical ratio push risk up, on a 0-100 scale. */
    private double heuristicRiskScore(int incidentCount, double criticalRatio) {
        double frequencyComponent = Math.min(60, incidentCount * 12.0);
        double severityComponent = criticalRatio * 40.0;
        return Math.round((frequencyComponent + severityComponent) * 10.0) / 10.0;
    }

    private PredictionResponse toResponse(Prediction p) {
        return PredictionResponse.builder()
                .id(p.getId())
                .serviceName(p.getServiceName())
                .riskScore(p.getRiskScore())
                .predictedFailure(p.getPredictedFailure())
                .reasoning(p.getReasoning())
                .incidentCountLast30Days(p.getIncidentCountLast30Days())
                .criticalRatioLast30Days(p.getCriticalRatioLast30Days())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
