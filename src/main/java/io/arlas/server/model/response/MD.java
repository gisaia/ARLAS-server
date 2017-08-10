package io.arlas.server.model.response;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class MD {
    public String id;

    public Long timestamp;

    public Object geometry;

    public Object centroid;
}
