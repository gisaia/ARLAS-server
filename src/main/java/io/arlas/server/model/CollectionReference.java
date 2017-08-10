package io.arlas.server.model;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class CollectionReference {

    public static final String COLLECTION_NAME = "collection_name";
    public static final String INDEX_NAME = "index_name";
    public static final String TYPE_NAME = "type_name";
    public static final String ID_PATH = "id_path";
    public static final String GEOMETRY_PATH = "geometry_path";
    public static final String CENTROID_PATH = "centroid_path";
    public static final String TIMESTAMP_PATH = "timestamp_path";
    public static final String TIMESTAMP_FORMAT = "timestamp_format";
    public static final String DEFAULT_TIMESTAMP_FORMAT = "strict_date_optional_time||epoch_millis";
    public static final String INCLUDE_FIELDS = "include_fields";
    public static final String EXCLUDE_FIELDS = "exclude_fields";
    public static final String CUSTOM_PARAMS = "custom_params";

    public String collectionName;

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

    @Override
    public String toString() {
        return "CollectionReference [collectionName=" + collectionName + ", params=" + params + "]";
    }
}
