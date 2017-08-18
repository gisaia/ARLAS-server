package io.arlas.server.app;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.arlas.server.exceptions.ArlasConfigurationException;
import org.elasticsearch.common.Strings;

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
    
    private List<String> getFields(String fieldsComaSeparated) throws ArlasConfigurationException {
        if(Strings.isNullOrEmpty(fieldsComaSeparated)) {
            throw new ArlasConfigurationException("Collection auto discover configuration is missing or empty : " + this.toString());
        }
        return Arrays.asList(fieldsComaSeparated.split(","));
    }
    
    public List<String> getPreferredIdFieldNames() throws ArlasConfigurationException {
        return getFields(preferredIdFieldName);
    }
    
    public List<String> getPreferredTimestampFieldNames() throws ArlasConfigurationException {
        return getFields(preferredTimestampFieldName);
    }
    
    public List<String> getPreferredCentroidFieldNames() throws ArlasConfigurationException {
        return getFields(preferredCentroidFieldName);
    }
    
    public List<String> getPreferredGeometryFieldNames() throws ArlasConfigurationException {
        return getFields(preferredGeometryFieldName);
    }

    @Override
    public String toString() {
        return "CollectionAutoDiscoverConfiguration [preferredIdFieldName=" + preferredIdFieldName + ", preferredTimestampFieldName=" + preferredTimestampFieldName + ", preferredCentroidFieldName="
                + preferredCentroidFieldName + ", preferredGeometryFieldName=" + preferredGeometryFieldName + ", schedule=" + schedule + "]";
    }
}
