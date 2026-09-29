package com.sangho.exception;

import java.util.Map;

/** 429 — trop de requêtes. {@link #getRetryAfter()} : délai (secondes) avant de réessayer. */
public class SanghoRateLimitException extends SanghoException {
    private final int retryAfter;

    public SanghoRateLimitException(String message, int retryAfter, Map<String, Object> raw) {
        super(message != null ? message : "Rate limit exceeded. Retry after " + retryAfter + "s.",
              "RATE_LIMIT_ERROR", null, 429, raw);
        this.retryAfter = retryAfter;
    }

    public int getRetryAfter() { return retryAfter; }
}
