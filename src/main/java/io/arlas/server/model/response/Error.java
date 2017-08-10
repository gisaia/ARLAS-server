package io.arlas.server.model.response;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Error {
    public int status;
    public String message;
    public String error;

    public Error(int status, String error, String message) {
        super();
        this.status = status;
        this.error = error;
        this.message = message;
    }
}
