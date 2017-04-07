package io.arlas.server.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.geojson.FeatureCollection;

@ApiModel(value="CollectionReference", description="The description of the elasticsearch index and the way ARLAS API will serve it.")
public class CollectionReference extends FeatureCollection{

    private String indexName;
    private String idPath;
    private String geometryPath;
    private String centroidPath;
    private String timestampPath;

    public CollectionReference() {
    }

    public CollectionReference(String indexName) {
        this.indexName = indexName;
    }

    public CollectionReference(String indexName, String idPath, String geometryPath, String centroidPath, String timestampPath) {
        this.indexName = indexName;
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
    @ApiModelProperty(value = "Path to the collection's id")
    public String getIdPath() {
        return idPath;
    }

    public void setIdPath(String idPath) {
        this.idPath = idPath;
    }

    @ApiModelProperty(value = "Path to the collection's geometry")
    public String getGeometryPath() {
        return geometryPath;
    }

    public void setGeometryPath(String geometryPath) {
        this.geometryPath = geometryPath;
    }

    @ApiModelProperty(value = "Path to the collection's centroid")
    public String getCentroidPath() {
        return centroidPath;
    }

    public void setCentroidPath(String centroidPath) {
        this.centroidPath = centroidPath;
    }

    @ApiModelProperty(value = "Path to the collection's timestamp")
    public String getTimestampPath() {
        return timestampPath;
    }

    public void setTimestampPath(String timestampPath) {
        this.timestampPath = timestampPath;
    }
}
