package io.arlas.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

// TODO: Write ArlasAggregation properties
@ApiModel(value = "ArlasAggregation", description = "Aggregation result")
public class ArlasAggregation extends Timed{
    @ApiModelProperty(name = "name", value = "Name")
    public String name;
    @ApiModelProperty(name = "count", value = "Name")
    public Long count;
    @ApiModelProperty(name = "key", value = "Name")
    public Object key;
    @ApiModelProperty(name = "elements", value = "elements")
    public List<ArlasAggregation> elements;
    @ApiModelProperty(name = "agg_metric", value = "metric")
    public ArlasMetric metric = null;


}
