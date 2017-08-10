package io.arlas.server.model.response;

import java.util.List;

import io.arlas.server.model.CollectionReference;
import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class CollectionReferenceDescription extends CollectionReference {
    public List<CollectionReferenceDescriptionProperty> properties;

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if(obj instanceof CollectionReferenceDescription) {
            CollectionReferenceDescription collection = (CollectionReferenceDescription)obj;
            ret = collection.params.indexName.equals(this.params.indexName)
                    && collection.params.typeName.equals(this.params.typeName);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "[properties=" + properties + "]";
    }
}