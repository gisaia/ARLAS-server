package io.arlas.server.exceptions;

public class NotAllowedException extends ArlasException {
    private static final long serialVersionUID = 1L;
    public NotAllowedException(String message) {
        super(message);
    }
}
