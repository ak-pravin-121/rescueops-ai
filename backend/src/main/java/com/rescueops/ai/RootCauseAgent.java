package com.rescueops.ai;

import com.rescueops.entity.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RootCauseAgent {

    private final GeminiService geminiService;

    public AgentFinding investigate(String title, String description, String logContent,
                                     AgentFinding priorAnalysis, String similarPastIncidents) {

        AgentFinding fallback = AgentFinding.builder()
                .severity(priorAnalysis != null ? priorAnalysis.getSeverity() : Severity.MEDIUM)
                .rootCause(priorAnalysis != null ? priorAnalysis.getRootCause() : "Root cause could not be determined automatically.")
                .confidence(priorAnalysis != null ? priorAnalysis.getConfidence() : 0)
                .suggestedFix(priorAnalysis != null ? priorAnalysis.getSuggestedFix() : "Escalate to the service owner.")
                .summary("Root cause investigation")
                .build();

        String prompt = """
                You are the Root Cause Agent in a multi-agent SRE pipeline. Your job is error pattern detection, \
                service dependency mapping, and failure correlation - go deeper than a first-pass triage.

                Initial triage said: severity=%s, rootCause="%s", confidence=%s.

                Similar past incidents from the knowledge base (may be empty):
                %s

                Re-examine the evidence below and either confirm or refine the root cause with more specific, \
                technical detail (e.g. name the exact failing component, dependency, or resource limit if the \
                logs support it). Respond with STRICT JSON only:
                {"severity": "<CRITICAL|HIGH|MEDIUM|LOW>", "rootCause": "<refined, specific root cause, 2-3 sentences>", \
                "confidence": <integer 0-100>, "suggestedFix": "<concrete fix referencing the specific component>"}

                Incident title: %s
                Description: %s
                Logs / evidence:
                %s
                """.formatted(
                priorAnalysis != null ? priorAnalysis.getSeverity() : "UNKNOWN",
                priorAnalysis != null ? priorAnalysis.getRootCause() : "none yet",
                priorAnalysis != null ? priorAnalysis.getConfidence() : "n/a",
                similarPastIncidents == null || similarPastIncidents.isBlank() ? "(none found)" : similarPastIncidents,
                title, description, truncate(logContent)
        );

        Map<String, Object> json = geminiService.generateJson(prompt, 0.25);
        if (json == null) {
            return fallback;
        }

        return AgentFinding.builder()
                .severity(AgentSupport.severity(json, "severity", fallback.getSeverity()))
                .rootCause(AgentSupport.text(json, "rootCause", fallback.getRootCause()))
                .confidence(AgentSupport.intValue(json, "confidence", fallback.getConfidence()))
                .suggestedFix(AgentSupport.text(json, "suggestedFix", fallback.getSuggestedFix()))
                .summary("Root cause investigation")
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
