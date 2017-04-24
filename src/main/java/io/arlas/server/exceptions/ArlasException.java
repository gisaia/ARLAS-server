package io.arlas.server.exceptions;

/**
 * Created by hamou on 12/04/17.
 */
public class ArlasException extends Exception {
    private static final long serialVersionUID = 1L;

    public ArlasException() {
    }

    public ArlasException(String message) {
        super(message);
    }
}
