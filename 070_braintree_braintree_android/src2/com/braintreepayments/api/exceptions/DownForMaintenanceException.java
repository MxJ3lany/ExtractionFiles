package com.braintreepayments.api.exceptions;

/**
 * Exception thrown when a 503 HTTP_UNAVAILABLE response is encountered. Indicates the server is
 * unreachable or the request timed out.
 */
public class DownForMaintenanceException extends Exception {

    public DownForMaintenanceException(String message) {
        super(message);
    }
}
