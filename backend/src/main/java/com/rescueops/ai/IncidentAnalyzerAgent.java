package com.rescueops.ai;

import com.rescueops.entity.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class IncidentAnalyzerAgent {

    private final GeminiService geminiService;

    public AgentFinding analyze(String title, String description, String logContent) {
        AgentFinding fallback = AgentFinding.builder()
                .severity(Severity.MEDIUM)
                .rootCause("AI analysis unavailable - configure GEMINI_API_KEY to enable automatic triage.")
                .confidence(0)
                .suggestedFix("Have an engineer review the logs manually.")
                .summary(title)
                .build();

        String prompt = """
                You are an SRE incident analyzer for "RescueOps AI". Read the incident below, which may include \
                Spring Boot, Docker, Kubernetes or Nginx logs, and respond with STRICT JSON only (no markdown fences):
                {"severity": "<CRITICAL|HIGH|MEDIUM|LOW>", "rootCause": "<one or two sentence root cause>", \
                "confidence": <integer 0-100>, "suggestedFix": "<one concrete actionable fix>"}

                Severity guide: CRITICAL = full outage / data loss risk / revenue-impacting; HIGH = major feature broken \
                or significant user impact; MEDIUM = degraded performance or partial failure; LOW = cosmetic or low-impact.

                Incident title: %s
                Description: %s
                Logs / evidence:
                %s
                """.formatted(title, description, truncate(logContent));

        Map<String, Object> json = geminiService.generateJson(prompt, 0.2);
        if (json == null) {
            return fallback;
        }

        return AgentFinding.builder()
                .severity(AgentSupport.severity(json, "severity", fallback.getSeverity()))
                .rootCause(AgentSupport.text(json, "rootCause", fallback.getRootCause()))
                .confidence(AgentSupport.intValue(json, "confidence", fallback.getConfidence()))
                .suggestedFix(AgentSupport.text(json, "suggestedFix", fallback.getSuggestedFix()))
                .summary(title)
                .build();
    }

    private String truncate(String text) {
        if (text == null) {
            return "(no logs provided)";
        }
        int max = 12000;
        return text.length() > max ? text.substring(0, max) + "\n...[truncated]" : text;
    }
}
