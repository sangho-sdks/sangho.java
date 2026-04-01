package com.sangho.exception;

import java.util.List;
import java.util.Map;

/** 422 — Request validation failed. */
public class SanghoValidationException extends SanghoException {
    public SanghoValidationException(String message, Map<String, Object> raw) {
        super(message, "validation_error", 422, raw);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getFieldErrors() {
        Object detail = getRaw().getOrDefault("detail", getRaw().get("errors"));
        if (detail instanceof Map<?, ?> map) {
            return (Map<String, List<String>>) map;
        }
        return Map.of();
    }
}
