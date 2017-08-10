package io.arlas.server.model.response;


import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class CollectionReferenceDescriptionProperty {
    public CollectionReferenceDescriptionProperty(String name, ElasticType type, String format) {
        super();
        this.name = name;
        this.type = type;
        this.format = format;
    }

    public String name;
    
    public ElasticType type;

    public String format;

    @Override
    public String toString() {
        return "[name=" + name + ", type=" + type + "]";
    }
}