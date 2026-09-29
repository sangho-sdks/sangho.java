package com.sangho.exception;

import java.util.Map;

/** 409 — conflit d'état métier (ex : {@code account_not_claimed}). Distinct de {@link SanghoIdempotencyException}. */
public class SanghoConflictException extends SanghoException {
    public SanghoConflictException(String message, Map<String, Object> raw) {
        super(message, "CONFLICT_ERROR", "conflict", 409, raw);
    }
}
