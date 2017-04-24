package io.arlas.server.exceptions;

public class InternalServerErrorException extends ArlasException {

    private static final long serialVersionUID = 1L;

    public InternalServerErrorException(String message) {
        super(message);
    }

}
