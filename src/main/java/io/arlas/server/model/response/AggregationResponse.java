package io.arlas.server.model.response;

import java.util.List;
import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class AggregationResponse extends OperationInfo {
    public String name;
    public Long count;
    public Object key;
    public Object keyAsString;
    public List<AggregationResponse> elements;
    public AggregationMetric metric = null;
}
