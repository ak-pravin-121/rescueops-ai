package com.rescueops.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunbookAgent {

    private final GeminiService geminiService;

    public RunbookResult generateRunbook(String title, String serviceName, String rootCause, String suggestedFix) {
        String prompt = """
                You are the Runbook Agent. Turn the diagnosis below into a clear, numbered, step-by-step \
                remediation runbook an on-call engineer can follow under pressure. Use short imperative steps \
                (e.g. "1. Restart payment-service", "2. Verify DB connection pool health"). Include a final \
                verification step. Keep it to 4-8 steps. Do not use markdown headers, just a numbered list.

                Service: %s
                Root cause: %s
                Suggested fix direction: %s
                Incident: %s
                """.formatted(serviceName, rootCause, suggestedFix, title);

        String content = geminiService.generateText(prompt, 0.3);

        return RunbookResult.builder()
                .title("Runbook: " + title)
                .content(content)
                .build();
    }
}
