package com.sangho.exception;

import java.util.Map;

/** Requête expirée (timeout). Même raisonnement que SanghoNetworkException. */
public class SanghoTimeoutException extends SanghoException {
    public SanghoTimeoutException(long timeoutSeconds) {
        super("Request timed out after " + timeoutSeconds + "s.", "timeout_error", 0, Map.of(), "TIMEOUT_ERROR");
    }
}
