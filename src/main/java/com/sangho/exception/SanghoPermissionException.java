package com.sangho.exception;

import java.util.Map;

/** 403 — permissions insuffisantes. */
public class SanghoPermissionException extends SanghoException {
    public SanghoPermissionException(String message, Map<String, Object> raw) {
        super(message, "PERMISSION_ERROR", null, 403, raw);
    }
}
