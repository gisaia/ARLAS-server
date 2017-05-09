package io.arlas.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.elasticsearch.search.aggregations.Aggregation;

import java.util.List;

/**
 * Created by hamou on 25/04/17.
 */
@ApiModel(value = "ArlasBucket", description = "Bucket result")

public class ArlasBucket {
    @ApiModelProperty(name = "key_name", value = "Name")
    public Long docCount;
    @ApiModelProperty(name = "key_name", value = "Name")
    public Object key;
    @ApiModelProperty(name = "agg_list", value = "Name")
    public List<ArlasAggregation> aggregations;
}
