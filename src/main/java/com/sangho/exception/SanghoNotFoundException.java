package com.sangho.exception;
import java.util.Map;
/** 404 — Resource not found. */
public class SanghoNotFoundException extends SanghoException {
    public SanghoNotFoundException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message, code, statusCode, raw);
    }
    public SanghoNotFoundException(String message) { super(message, "api_error", 0, Map.of()); }
}
