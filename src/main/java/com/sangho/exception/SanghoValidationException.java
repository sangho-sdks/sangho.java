package com.sangho.exception;

import java.util.List;
import java.util.Map;

/** 422 — validation de la requête (erreurs champ par champ dans {@link #getFieldErrors()}). */
public class SanghoValidationException extends SanghoException {
    public SanghoValidationException(String message, Map<String, Object> raw) {
        super(message, "VALIDATION_ERROR", null, 422, raw);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getFieldErrors() {
        Object detail = getRaw().get("detail") instanceof Map<?, ?> ? getRaw().get("detail") : getRaw().get("errors");
        return detail instanceof Map<?, ?> map ? (Map<String, List<String>>) map : Map.of();
    }
}
