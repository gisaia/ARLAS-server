package io.arlas.server.model;

import org.hibernate.validator.constraints.NotEmpty;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CollectionReferenceParameters", description = "The description of the elasticsearch index and the way ARLAS API will serve it.")
@JsonSnakeCase
public class CollectionReferenceParameters {

    @NotEmpty
    @ApiModelProperty(value = "The collection's index name")
    public String indexName;

    @NotEmpty
    @ApiModelProperty(value = "The collection's type name")
    public String typeName;

    @ApiModelProperty(value = "Path to the collection's id", example = "id")
    public String idPath = "id";

    @ApiModelProperty(value = "Path to the collection's geometry", example = "geometry")
    public String geometryPath = "geometry";

    @ApiModelProperty(value = "Path to the collection's centroid", example = "centroid")
    public String centroidPath = "centroid";

    @ApiModelProperty(value = "Path to the collection's timestamp", example = "timestamp")
    public String timestampPath = "timestamp";

    public CollectionReferenceParameters() {
    }

    public CollectionReferenceParameters(String indexName, String typeName, String idPath, String geometryPath,
            String centroidPath, String timestampPath) {
        this.indexName = indexName;
        this.typeName = typeName;
        this.idPath = idPath;
        this.geometryPath = geometryPath;
        this.centroidPath = centroidPath;
        this.timestampPath = timestampPath;
    }
}
