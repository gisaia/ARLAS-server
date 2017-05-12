package io.arlas.server.model.response;

public class ArlasError {
    public int status;
    public String message;
    public String error;

    public ArlasError(int status, String error, String message) {
        super();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
