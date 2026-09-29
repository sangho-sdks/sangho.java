package com.sangho.exception;

import java.util.Map;

/** 409 — clé d'idempotence réutilisée avec un corps différent. */
public class SanghoIdempotencyException extends SanghoException {
    public SanghoIdempotencyException(Map<String, Object> raw) {
        super("Idempotency key reused with different request parameters.", "CONFLICT_ERROR", null, 409, raw);
    }
}
