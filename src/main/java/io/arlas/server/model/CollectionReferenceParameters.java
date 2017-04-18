package io.arlas.server.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="CollectionReferenceParameters", description="The description of the elasticsearch index and the way ARLAS API will serve it.")
public class CollectionReferenceParameters{

    private String indexName;
    private String typeName;
    private String idPath = "id";
    private String geometryPath = "geometry";
    private String centroidPath = "centroid";
    private String timestampPath = "timestamp";

    public CollectionReferenceParameters() {
    }

    public CollectionReferenceParameters(String indexName, String typeName, String idPath, String geometryPath, String centroidPath, String timestampPath) {
        this.indexName = indexName;
        this.typeName = typeName;
        this.idPath = idPath;
        this.geometryPath = geometryPath;
        this.centroidPath = centroidPath;
        this.timestampPath = timestampPath;
    }

    @ApiModelProperty(value = "The collection's index name")
    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    @ApiModelProperty(value = "The collection's type name")
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    @ApiModelProperty(value = "Path to the collection's id", example = "id")
    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    @ApiModelProperty(value = "Path to the collection's geometry", example = "geometry")
    public String getGeometryPath() {
        return geometryPath;
    }

    public void setGeometryPath(String geometryPath) {
        this.geometryPath = geometryPath;
    }

    @ApiModelProperty(value = "Path to the collection's centroid", example = "centroid")
    public String getCentroidPath() {
        return centroidPath;
    }

    public void setCentroidPath(String centroidPath) {
        this.centroidPath = centroidPath;
    }

    @ApiModelProperty(value = "Path to the collection's timestamp", example = "timestamp")
    public String getTimestampPath() {
        return timestampPath;
    }

    public void setTimestampPath(String timestampPath) {
        this.timestampPath = timestampPath;
    }
    
    /**
     * @return JSON representation
     */
    public ObjectNode toJson() {	
	ObjectMapper mapper = new ObjectMapper();
	ObjectNode json = mapper.createObjectNode();
	json.put(CollectionReference.INDEX_NAME, this.getIndexName());
	json.put(CollectionReference.TYPE_NAME, this.getTypeName());
	json.put(CollectionReference.ID_PATH, this.getIdPath());
	json.put(CollectionReference.GEOMETRY_PATH, this.getGeometryPath());
	json.put(CollectionReference.CENTROID_PATH, this.getCentroidPath());
	json.put(CollectionReference.TIMESTAMP_PATH, this.getTimestampPath());
	return json;
    }
    
    /**
     * @return JSON String representation
     */
    public String toJsonString() {	
	return toJson().toString();
    }
}

