package io.arlas.server.model.response;


import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Map;

@JsonSnakeCase
public class CollectionReferenceDescriptionProperty {

    public CollectionReferenceDescriptionProperty() {
    }
    
    public ElasticType type;

    public String format;

    public Map<String,CollectionReferenceDescriptionProperty> properties;

    @Override
    public String toString() {
        return "[type=" + type + ", format=" + format + ", properties=" + properties + "]";
    }
}