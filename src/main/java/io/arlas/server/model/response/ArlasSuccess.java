package io.arlas.server.model.response;

public class ArlasSuccess {
    public int status;
    public String message;

    public ArlasSuccess(int status, String message) {
        super();
        this.status = status;
        this.message = message;
    }
}
