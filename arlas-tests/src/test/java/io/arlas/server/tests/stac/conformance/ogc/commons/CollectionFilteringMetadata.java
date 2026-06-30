package io.arlas.server.tests.stac.conformance.ogc.commons;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class CollectionFilterMetadata {

    private String collectionId;
    private String itemsPath;
    private String queryablesUri;
    private String sampleQueryable;
    private String spatialQueryable;
    private boolean additionalProperties = true;
    private List<Double> wgs84Bbox = new ArrayList<>();
    private List<String> supportedCrs = new ArrayList<>();

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getItemsPath() {
        return itemsPath;
    }

    public void setItemsPath(String itemsPath) {
        this.itemsPath = itemsPath;
    }

    public String getQueryablesUri() {
        return queryablesUri;
    }

    public void setQueryablesUri(String queryablesUri) {
        this.queryablesUri = queryablesUri;
    }

    public String getSampleQueryable() {
        return sampleQueryable;
    }

    public void setSampleQueryable(String sampleQueryable) {
        this.sampleQueryable = sampleQueryable;
    }

    public String getSpatialQueryable() {
        return spatialQueryable;
    }

    public void setSpatialQueryable(String spatialQueryable) {
        this.spatialQueryable = spatialQueryable;
    }

    public boolean isAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public List<Double> getWgs84Bbox() {
        return wgs84Bbox;
    }

    public void setWgs84Bbox(List<Double> wgs84Bbox) {
        this.wgs84Bbox = wgs84Bbox == null ? new ArrayList<>() : new ArrayList<>(wgs84Bbox);
    }

    public List<String> getSupportedCrs() {
        return supportedCrs;
    }

    public void setSupportedCrs(List<String> supportedCrs) {
        this.supportedCrs = supportedCrs == null ? new ArrayList<>() : new ArrayList<>(supportedCrs);
    }
}
