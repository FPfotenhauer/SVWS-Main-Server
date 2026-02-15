package de.schultraeger.application.port.out;

/**
 * Exception for NRW client operations.
 */
public class NrwClientException extends Exception {
    public NrwClientException(String message) {
        super(message);
    }

    public NrwClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
