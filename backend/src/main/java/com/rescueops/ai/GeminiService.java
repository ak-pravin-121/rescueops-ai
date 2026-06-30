package com.rescueops.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin, dependency-free wrapper around the Google AI Studio (Gemini) generateContent REST API.
 * Every "agent" in RescueOps AI (analyzer, root-cause, runbook, postmortem, security, prediction
 * reasoning) is really a distinct prompt sent through this one client. Degrades gracefully if the
 * API key is missing or a call fails, so a flaky/unconfigured key never crashes the platform.
 */
@Slf4j
@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api-base-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiBaseUrl;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /** Plain free-text generation (used for runbooks, postmortems, security write-ups, chat replies). */
    public String generateText(String prompt, double temperature) {
        if (!isConfigured()) {
            return "AI assistant is not configured yet. Set GEMINI_API_KEY on the server to enable this feature.";
        }
        try {
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature);

            String result = callGemini(buildRequestBody(prompt, generationConfig));
            return result != null ? result.trim() : "The AI model returned an empty response. Please try again.";
        } catch (Exception ex) {
            log.error("Gemini text generation failed: {}", ex.getMessage());
            return "Sorry, the AI service is temporarily unavailable. Please try again shortly.";
        }
    }

    /** Strict-JSON generation. Returns the parsed Map, or null if unconfigured/failed (caller supplies a fallback). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> generateJson(String prompt, double temperature) {
        if (!isConfigured()) {
            return null;
        }
        try {
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature);
            generationConfig.put("responseMimeType", "application/json");

            String rawText = callGemini(buildRequestBody(prompt, generationConfig));
            if (rawText == null) {
                return null;
            }
            return objectMapper.readValue(rawText, Map.class);
        } catch (Exception ex) {
            log.error("Gemini JSON generation failed: {}", ex.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildRequestBody(String promptText, Map<String, Object> generationConfig) {
        Map<String, Object> part = new HashMap<>();
        part.put("text", promptText);

        Map<String, Object> contentEntry = new HashMap<>();
        contentEntry.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contentEntry));
        requestBody.put("generationConfig", generationConfig);
        return requestBody;
    }

    @SuppressWarnings("unchecked")
    private String callGemini(Map<String, Object> requestBody) {
        String url = apiBaseUrl + "/" + model + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
        if (response == null) {
            return null;
        }

        List<Object> candidates = (List<Object>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Map<String, Object> firstCandidate = (Map<String, Object>) candidates.get(0);
        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
        if (content == null) {
            return null;
        }

        List<Object> parts = (List<Object>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        Map<String, Object> firstPart = (Map<String, Object>) parts.get(0);
        Object text = firstPart.get("text");
        return text != null ? text.toString() : null;
    }
}
