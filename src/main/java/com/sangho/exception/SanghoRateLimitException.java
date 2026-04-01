package com.sangho.exception;

import java.util.Map;

/** 429 — Rate limit exceeded. */
public class SanghoRateLimitException extends SanghoException {
    private final int retryAfter;

    public SanghoRateLimitException(int retryAfter) {
        super("Rate limit exceeded. Retry after " + retryAfter + "s.",
              "rate_limit_exceeded", 429, Map.of());
        this.retryAfter = retryAfter;
    }

    public int getRetryAfter() { return retryAfter; }
}
