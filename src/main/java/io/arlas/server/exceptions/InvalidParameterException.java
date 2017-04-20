package io.arlas.server.exceptions;

/**
 * Created by hamou on 12/04/17.
 */
public class InvalidParameterException extends ArlasException {
    private static final long serialVersionUID = 1L;
    public InvalidParameterException(String message){
        super(message);
    }
}
