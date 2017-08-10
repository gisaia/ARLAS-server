package io.arlas.server.model.response;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Success {
    public int status;
    public String message;

    public Success(int status, String message) {
        super();
        this.status = status;
        this.message = message;
    }
}
