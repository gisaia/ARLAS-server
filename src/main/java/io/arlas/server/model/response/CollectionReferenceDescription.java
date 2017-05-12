package io.arlas.server.model.response;

import io.arlas.server.model.CollectionReference;
import io.dropwizard.jackson.JsonSnakeCase;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "CollectionReferenceDescription", description = "Describe the structure and the content of the given collection.")
@JsonSnakeCase
public class CollectionReferenceDescription extends CollectionReference {
    @ApiModelProperty(value = "The collection fields")
    public Object properties;
}
