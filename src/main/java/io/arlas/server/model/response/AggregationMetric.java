package io.arlas.server.model.response;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class AggregationMetric {
    public String type;
    public String field;
    public Double value;

}
