package io.arlas.server.model.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "AggregationMD", description = "Provides the total number of hits and the aggregation execution time.")
public class AggregationMD {
    @ApiModelProperty(name = "query_time", value = "Query execution time")
    public Long queryTime = null;
    @ApiModelProperty(name = "arlas_time", value = "All the aggregation execution time. It includes query time.")
    public Long arlasTime = null;
    @ApiModelProperty(name = "totalnb", value = "Total number of hits matching the query")
    public Long totalnb = null;
}
