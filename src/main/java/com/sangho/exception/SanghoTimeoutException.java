package com.sangho.exception;

import java.util.Map;

/** Délai dépassé. Catégorie propre au SDK : la requête n'a jamais abouti. */
public class SanghoTimeoutException extends SanghoException {
    public SanghoTimeoutException(long timeoutMillis) {
        super("Request timed out after " + timeoutMillis + "ms.", "TIMEOUT_ERROR", null, 0, Map.of());
    }
}
