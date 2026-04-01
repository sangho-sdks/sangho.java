package com.sangho.exception;
import java.util.Map;
/** 401 — Invalid or missing API key. */
public class SanghoAuthException extends SanghoException {
    public SanghoAuthException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message, code, statusCode, raw);
    }
    public SanghoAuthException(String message) { super(message, "api_error", 0, Map.of()); }
}
