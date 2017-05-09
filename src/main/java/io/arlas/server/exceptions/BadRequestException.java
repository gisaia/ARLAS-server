package io.arlas.server.exceptions;

public class BadRequestException extends ArlasException {
    private static final long serialVersionUID = 1L;
    public BadRequestException(String message) { super(message); }
}
