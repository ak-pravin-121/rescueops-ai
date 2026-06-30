package com.rescueops.ai;

import com.rescueops.entity.Severity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SecurityAgent {

    private final GeminiService geminiService;

    public AgentFinding scan(String title, String logContent) {
        AgentFinding fallback = AgentFinding.builder()
                .severity(Severity.LOW)
                .rootCause("AI security scan unavailable - configure GEMINI_API_KEY to enable it.")
                .confidence(0)
                .suggestedFix("Have a security engineer review the logs manually.")
                .summary("Security scan")
                .build();

        String prompt = """
                You are the Security Agent. Scan the logs below for indicators of SQL injection, brute-force \
                login attempts, XSS, or DDoS-style traffic patterns. Respond with STRICT JSON only:
                {"severity": "<CRITICAL|HIGH|MEDIUM|LOW>", "rootCause": "<threat type found, or 'No clear security threat detected' if none>", \
                "confidence": <integer 0-100>, "suggestedFix": "<recommended security action>"}

                severity here means threat severity, not incident severity. Use LOW when nothing suspicious is found.

                Incident: %s
                Logs:
                %s
                """.formatted(title, truncate(logContent));

        Map<String, Object> json = geminiService.generateJson(prompt, 0.2);
        if (json == null) {
            return fallback;
        }

        return AgentFinding.builder()
                .severity(AgentSupport.severity(json, "severity", fallback.getSeverity()))
                .rootCause(AgentSupport.text(json, "rootCause", fallback.getRootCause()))
                .confidence(AgentSupport.intValue(json, "confidence", fallback.getConfidence()))
                .suggestedFix(AgentSupport.text(json, "suggestedFix", fallback.getSuggestedFix()))
                .summary("Security scan")
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
