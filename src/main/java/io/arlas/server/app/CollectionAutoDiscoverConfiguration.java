package io.arlas.server.app;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionAutoDiscoverConfiguration {
    
    @JsonProperty("preferred-id-field-name")
    public String preferredIdFieldName;
    
    @JsonProperty("preferred-timestamp-field-name")
    public String preferredTimestampFieldName;
    
    @JsonProperty("preferred-centroid-field-name")
    public String preferredCentroidFieldName;
    
    @JsonProperty("preferred-geometry-field-name")
    public String preferredGeometryFieldName;
    
    @JsonProperty("schedule")
    public int schedule;
    
    private List<String> getFields(String fieldsComaSeparated) {
        return Arrays.asList(fieldsComaSeparated.split(","));
    }
    
    public List<String> getPreferredIdFieldNames() {
        return getFields(preferredIdFieldName);
    }
    
    public List<String> getPreferredTimestampFieldNames() {
        return getFields(preferredTimestampFieldName);
    }
    
    public List<String> getPreferredCentroidFieldNames() {
        return getFields(preferredCentroidFieldName);
    }
    
    public List<String> getPreferredGeometryFieldNames() {
        return getFields(preferredGeometryFieldName);
    }

    @Override
    public String toString() {
        return "CollectionAutoDiscoverConfiguration [preferredIdFieldName=" + preferredIdFieldName + ", preferredTimestampFieldName=" + preferredTimestampFieldName + ", preferredCentroidFieldName="
                + preferredCentroidFieldName + ", preferredGeometryFieldName=" + preferredGeometryFieldName + ", schedule=" + schedule + "]";
    }
}
