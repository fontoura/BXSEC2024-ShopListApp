package com.github.fontoura.sample.shoplist.exception;

/**
 * Exception thrown when a service is unreachable.
 */
public class ServiceUnreachableException extends Exception {

    public ServiceUnreachableException(String message) {
        super(message);
    }

    public ServiceUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
