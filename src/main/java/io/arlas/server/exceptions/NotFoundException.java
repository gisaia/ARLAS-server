package io.arlas.server.exceptions;

public class NotFoundException extends ArlasException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
	super(message);
    }

}
