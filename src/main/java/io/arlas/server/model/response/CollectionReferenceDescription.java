package io.arlas.server.model.response;

import io.arlas.server.model.CollectionReference;
import io.dropwizard.jackson.JsonSnakeCase;

import java.util.Map;

@JsonSnakeCase
public class CollectionReferenceDescription extends CollectionReference {
    public Map<String,CollectionReferenceDescriptionProperty> properties;

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
        return "CollectionReference [collectionName=" + collectionName + ", params=" + params + ", properties=" + properties + "]";
    }
}