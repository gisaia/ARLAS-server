package io.arlas.server.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="ArlasHit", description="A hit retrieved from an ARLAS Collection")
public class ArlasHit{
    @ApiModelProperty(name = "md", value = "The hit's metadata")
    public ArlasMD md;

    @ApiModelProperty(name = "data", value = "The hit's data")
    public Object data;
}
