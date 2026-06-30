package com.rescueops.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostmortemAgent {

    private final GeminiService geminiService;

    public String generatePostmortem(String title, String description, String serviceName, String severity,
                                      String rootCause, String suggestedFix, String runbookContent,
                                      String createdAt, String resolvedAt) {

        String prompt = """
                You are the Postmortem Agent. Write a blameless incident postmortem in plain text with these \
                exact section headers, each on its own line: SUMMARY, ROOT CAUSE, IMPACT, TIMELINE, PREVENTION. \
                Keep each section to 2-5 sentences (TIMELINE can be a short bulleted list using "-"). Be specific \
                and factual, do not invent details not implied by the input.

                Incident: %s
                Service: %s
                Severity: %s
                Description: %s
                Root cause: %s
                Fix applied / direction: %s
                Remediation steps taken:
                %s
                Opened at: %s
                Resolved at: %s
                """.formatted(title, serviceName, severity, description, rootCause, suggestedFix,
                runbookContent == null ? "(none recorded)" : runbookContent,
                createdAt, resolvedAt == null ? "(not yet resolved)" : resolvedAt);

        return geminiService.generateText(prompt, 0.3);
    }
}
