package io.arlas.server.model.response;

import io.swagger.annotations.ApiModelProperty;

public class CollectionReferenceDescriptionProperty {
    public CollectionReferenceDescriptionProperty(String name, ElasticType type, String format) {
        super();
        this.name = name;
        this.type = type;
        this.format = format;
    }

    @ApiModelProperty(value = "The collection field name")
    public String name;
    
    @ApiModelProperty(value = "The collection field type")
    public ElasticType type;

    @ApiModelProperty(value = "The collection field format")
    public String format;

    @Override
    public String toString() {
        return "[name=" + name + ", type=" + type + "]";
    }
}