package io.arlas.server.core;

/**
 * Created by hamou on 12/04/17.
 */
public class InvalidParameter extends ArlasException {
    private static final long serialVersionUID = 1L;
    public InvalidParameter(String message){
        super(message);
    }
}
