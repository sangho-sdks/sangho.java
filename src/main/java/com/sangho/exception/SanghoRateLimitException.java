package com.sangho.exception;

import java.util.Map;

/** 429 — Rate limit exceeded. */
public class SanghoRateLimitException extends SanghoException {
    private final int retryAfter;

    public SanghoRateLimitException(int retryAfter, Map<String, Object> raw) {
        super(buildMessage(retryAfter, raw), "rate_limit_exceeded", 429, raw, "RATE_LIMIT_ERROR");
        this.retryAfter = retryAfter;
    }

    private static String buildMessage(int retryAfter, Map<String, Object> raw) {
        Object message = raw == null ? null : raw.get("message");
        return message instanceof String s ? s : "Rate limit exceeded. Retry after " + retryAfter + "s.";
    }

    public int getRetryAfter() { return retryAfter; }
}
