package io.arlas.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "ArlasMetric", description = "Metric agg")
public class ArlasMetric {
    @ApiModelProperty(name = "type", value = "Name of the metric aggregation")
    public String type;
    @ApiModelProperty(name = "field", value = "field of the metric aggregation")
    public String field;
    @ApiModelProperty(name = "value", value = "Value of the metric aggregation")
    public Double value;

}
