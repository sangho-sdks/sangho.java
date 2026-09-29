package com.sangho.exception;

import java.util.Map;

/** 403 — Forbidden. */
public class SanghoPermissionException extends SanghoException {
    public SanghoPermissionException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message, code, statusCode, raw, "PERMISSION_ERROR");
    }
    public SanghoPermissionException(String message) { super(message, "api_error", 0, Map.of(), "PERMISSION_ERROR"); }
}
