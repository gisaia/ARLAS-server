package io.arlas.server.model.response;

import io.swagger.annotations.ApiModelProperty;

public class CollectionReferenceDescriptionProperty {
    public CollectionReferenceDescriptionProperty(String name, ElasticType type) {
        super();
        this.name = name;
        this.type = type;
    }

    @ApiModelProperty(value = "The collection field name")
    public String name;
    
    @ApiModelProperty(value = "The collection field type")
    public ElasticType type;

    @Override
    public String toString() {
        return "[name=" + name + ", type=" + type + "]";
    }
}