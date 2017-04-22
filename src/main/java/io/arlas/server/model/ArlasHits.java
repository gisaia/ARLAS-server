package io.arlas.server.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value="ArlasHits", description="A collection of hits retrieved from ARLAS Collections")
public class ArlasHits {
    @ApiModelProperty(name = "hits", value = "ARLAS hits")
    public List<ArlasHit> hits;

    @ApiModelProperty(name = "nbhits", value = "Number of hits contained in hits")
    public long nbhits;

    @ApiModelProperty(name = "totalnb", value = "Total number of hits matching the query")
    public long totalnb;
}