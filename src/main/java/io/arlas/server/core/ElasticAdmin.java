package io.arlas.server.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;

public class ElasticAdmin {

    public TransportClient client;
    public ElasticAdmin(TransportClient client){
        this.client = client;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CollectionReferenceDescription describeCollection (CollectionReference collectionReference) throws IOException {
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;
        GetMappingsResponse response;
        response = client.admin().indices()
                .prepareGetMappings(collectionReferenceDescription.params.indexName).setTypes(collectionReferenceDescription.params.typeName).get();
        LinkedHashMap fields = (LinkedHashMap)response.getMappings()
                .get(collectionReferenceDescription.params.indexName).get(collectionReferenceDescription.params.typeName).sourceAsMap().get("properties");
        List<CollectionReferenceDescriptionProperty> properties = new ArrayList<CollectionReferenceDescriptionProperty>();
        for(Object field : fields.keySet()) {
            properties.add(new CollectionReferenceDescriptionProperty(field.toString(), 
                    ElasticType.getType(((Map<String,String>)fields.get(field)).get("type"))));
        }
        collectionReferenceDescription.properties = properties;
        return collectionReferenceDescription;
    }

    public List<CollectionReferenceDescription> describeAllCollections (List<CollectionReference> collectionReferenceList) throws IOException, ArlasException {
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = new ArrayList<>();
        for (CollectionReference collectionReference : collectionReferenceList){
            collectionReferenceDescriptionList.add(describeCollection(collectionReference));
        }
        return collectionReferenceDescriptionList;
    }
}
