package io.arlas.server.model;

import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CollectionReference", description = "The reference to ARLAS collection that embed elasticsearch index description.")
@JsonSnakeCase
public class CollectionReference {

    public static final String COLLECTION_NAME = "collection_name";
    public static final String INDEX_NAME = "index_name";
    public static final String TYPE_NAME = "type_name";
    public static final String ID_PATH = "id_path";
    public static final String GEOMETRY_PATH = "geometry_path";
    public static final String CENTROID_PATH = "centroid_path";
    public static final String TIMESTAMP_PATH = "timestamp_path";
    public static final String INCLUDE_FIELDS = "include_fields";
    public static final String EXCLUDE_FIELDS = "exclude_fields";

    @ApiModelProperty(value = "The collection name")
    public String collectionName;

    @ApiModelProperty(value = "The collection parameters")
    public CollectionReferenceParameters params;

    public CollectionReference() {
    }

    public CollectionReference(String collectionName, CollectionReferenceParameters params) {
        this.collectionName = collectionName;
        this.params = params;
    }

    public CollectionReference(String collectionName) {
        this.collectionName = collectionName;
    }
}
