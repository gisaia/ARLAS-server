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
    public String idPath;

    @ApiModelProperty(value = "Path to the collection's geometry", example = "geometry")
    public String geometryPath;

    @ApiModelProperty(value = "Path to the collection's centroid", example = "centroid")
    public String centroidPath;

    @ApiModelProperty(value = "Path to the collection's timestamp", example = "timestamp")
    public String timestampPath;
    
    @ApiModelProperty(value = "List the name patterns of the fields to be included in the result. Seperate patterns with a comma.", example = "*")
    public String includeFields = null;
    
    @ApiModelProperty(value = "List the name patterns of the fields to be excluded in the result. Seperate patterns with a comma.", example = "fieldname")
    public String excludeFields = null;

    public CollectionReferenceParameters() {
    }

    public CollectionReferenceParameters(String indexName, String typeName, String idPath, String geometryPath,
            String centroidPath, String timestampPath, String includeFields, String excludeFields) {
        this.indexName = indexName;
        this.typeName = typeName;
        this.idPath = idPath;
        this.geometryPath = geometryPath;
        this.centroidPath = centroidPath;
        this.timestampPath = timestampPath;
        this.includeFields = includeFields;
        this.excludeFields = excludeFields;
    }

    @Override
    public String toString() {
        return "CollectionReferenceParameters [indexName=" + indexName + ", typeName=" + typeName + ", idPath=" + idPath + ", geometryPath=" + geometryPath + ", centroidPath=" + centroidPath
                + ", timestampPath=" + timestampPath + ", includeFields=" + includeFields + ", excludeFields=" + excludeFields + "]";
    }
}
