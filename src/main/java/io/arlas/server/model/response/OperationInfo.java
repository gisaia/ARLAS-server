package io.arlas.server.model.response;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class OperationInfo {
    public Long queryTime = null;
    public Long totalTime = null;
    public Long totalnb = null;
}

