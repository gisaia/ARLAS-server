package io.arlas.server.model;

import org.hibernate.validator.constraints.NotEmpty;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@JsonSnakeCase
public class CollectionReferenceParameters {

    @NotEmpty
    public String indexName;

    @NotEmpty
    public String typeName;

    public String idPath;

    public String geometryPath;

    public String centroidPath;

    public String timestampPath;
    
    public String includeFields = null;
    
    public String excludeFields = null;

    public Map<String,String> custom_params = null;

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
                + ", timestampPath=" + timestampPath + ", includeFields=" + includeFields + ", excludeFields=" + excludeFields + ", timestampFormat=" + custom_params.get(CollectionReference.TIMESTAMP_FORMAT) +"]";
    }
}
