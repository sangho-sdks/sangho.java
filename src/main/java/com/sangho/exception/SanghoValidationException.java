package com.sangho.exception;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 422 — Request validation failed. */
public class SanghoValidationException extends SanghoException {

    public SanghoValidationException(Map<String, Object> raw) {
        super(buildMessage(raw), "validation_error", 422, raw, "VALIDATION_ERROR");
    }

    @SuppressWarnings("unchecked")
    private static String buildMessage(Map<String, Object> raw) {
        Object detail = raw.getOrDefault("detail", raw.get("errors"));
        if (detail instanceof Map<?, ?> map) {
            String summary = map.entrySet().stream()
                .map(e -> e.getKey() + ": " + joinErrors(e.getValue()))
                .collect(Collectors.joining(" | "));
            if (!summary.isBlank()) return summary;
        }
        Object message = raw.get("message");
        return message instanceof String s ? s : "Validation error";
    }

    private static String joinErrors(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getFieldErrors() {
        Object detail = getRaw().getOrDefault("detail", getRaw().get("errors"));
        if (detail instanceof Map<?, ?> map) {
            return (Map<String, List<String>>) map;
        }
        return Map.of();
    }

    public String getParam() {
        Object param = getRaw().get("param");
        return param instanceof String s ? s : null;
    }
}
