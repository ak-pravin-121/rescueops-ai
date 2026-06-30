package com.rescueops.ai;

import com.rescueops.entity.Severity;

import java.util.Map;

final class AgentSupport {

    private AgentSupport() {
    }

    static Severity severity(Map<String, Object> json, String key, Severity fallback) {
        if (json == null || json.get(key) == null) {
            return fallback;
        }
        try {
            return Severity.valueOf(json.get(key).toString().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    static Integer intValue(Map<String, Object> json, String key, Integer fallback) {
        if (json == null || json.get(key) == null) {
            return fallback;
        }
        try {
            return (int) Double.parseDouble(json.get(key).toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    static String text(Map<String, Object> json, String key, String fallback) {
        if (json == null || json.get(key) == null) {
            return fallback;
        }
        return json.get(key).toString();
    }
}
