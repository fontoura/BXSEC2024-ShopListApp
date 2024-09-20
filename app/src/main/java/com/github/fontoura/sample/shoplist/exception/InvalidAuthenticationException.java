package com.github.fontoura.sample.shoplist.exception;

/**
 * Exception thrown when the authentication is invalid.
 */
public class InvalidAuthenticationException extends Exception {

    public InvalidAuthenticationException(String message) {
        super(message);
    }

    public InvalidAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
