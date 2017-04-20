package io.arlas.server.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="CollectionReference", description="The reference to ARLAS collection that embed elasticsearch index description.")
public class CollectionReference{
    
    public static final String COLLECTION_NAME = "collectionName";
    public static final String INDEX_NAME = "indexName";
    public static final String TYPE_NAME = "typeName";
    public static final String ID_PATH = "idPath";
    public static final String GEOMETRY_PATH = "geometryPath";
    public static final String CENTROID_PATH = "centroidPath";
    public static final String TIMESTAMP_PATH = "timestampPath";
    
    private String collectionName;
    private CollectionReferenceParameters params;


    public CollectionReference() {
    }
    
    public CollectionReference(String collectionName, CollectionReferenceParameters params) {
	this.collectionName = collectionName;
	this.params = params;
    }

    public CollectionReference(String collectionName) {
        this.collectionName = collectionName;
    }
    
    @ApiModelProperty(value = "The collection name")
    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
    
    @ApiModelProperty(value = "The collection parameters")
    public CollectionReferenceParameters getParams() {
        return params;
    }

    public void setParams(CollectionReferenceParameters params) {
        this.params = params;
    }

    /**
     * @return JSON representation
     */
    public ObjectNode toJson() {	
	ObjectMapper mapper = new ObjectMapper();
	ObjectNode json = mapper.createObjectNode();
	json.put(COLLECTION_NAME, this.getCollectionName());
	json.put(INDEX_NAME, this.getParams().getIndexName());
	json.put(TYPE_NAME, this.getParams().getTypeName());
	json.put(ID_PATH, this.getParams().getIdPath());
	json.put(GEOMETRY_PATH, this.getParams().getGeometryPath());
	json.put(CENTROID_PATH, this.getParams().getCentroidPath());
	json.put(TIMESTAMP_PATH, this.getParams().getTimestampPath());
	return json;
    }
    
    /**
     * @return JSON String representation
     */
    public String toJsonString() {	
	return toJson().toString();
    }
}
