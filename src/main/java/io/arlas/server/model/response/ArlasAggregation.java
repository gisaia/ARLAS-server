package io.arlas.server.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

// TODO: Write ArlasAggregation properties
@ApiModel(value = "ArlasAggregation", description = "Aggregation result")
public class ArlasAggregation extends AggregationMD {
    @ApiModelProperty(name = "name", value = "Name")
    public String name;
    @ApiModelProperty(name = "count", value = "Count")
    public Long count;
    @ApiModelProperty(name = "key", value = "Key")
    public Object key;
    @ApiModelProperty(name = "key_as_string", value = "Key as string")
    public Object keyAsString;
    @ApiModelProperty(name = "elements", value = "Sub-aggregations")
    public List<ArlasAggregation> elements;
    @ApiModelProperty(name = "metric", value = "Metric aggregation")
    public ArlasMetric metric = null;


}
