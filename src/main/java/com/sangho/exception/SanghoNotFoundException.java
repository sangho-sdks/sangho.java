package com.sangho.exception;

import java.util.Map;

/** 404 — ressource introuvable. */
public class SanghoNotFoundException extends SanghoException {
    public SanghoNotFoundException(String message, Map<String, Object> raw) {
        super(message, "NOT_FOUND_ERROR", null, 404, raw);
    }
}
