package com.sangho.exception;

import java.util.Map;

/** 409 — Idempotency key reused with a different request payload. */
public class SanghoIdempotencyException extends SanghoException {
    public SanghoIdempotencyException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message, code, statusCode, raw, "CONFLICT_ERROR");
    }
    public SanghoIdempotencyException(String message) { super(message, "api_error", 0, Map.of(), "CONFLICT_ERROR"); }
}
