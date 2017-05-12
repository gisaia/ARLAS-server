package io.arlas.server.core;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticAdmin {

    public TransportClient client;
    public ElasticAdmin(TransportClient client){
        this.client = client;
    }
    public CollectionReferenceDescription describeCollection (CollectionReference collectionReference) throws IOException {
        CollectionReferenceDescription collectionReferenceDescription = new CollectionReferenceDescription();
        collectionReferenceDescription.params = collectionReference.params;
        collectionReferenceDescription.collectionName = collectionReference.collectionName;
        GetMappingsResponse response;
        response = client.admin().indices()
                .prepareGetMappings(collectionReferenceDescription.params.indexName).setTypes(collectionReferenceDescription.params.typeName).get();
        collectionReferenceDescription.properties = response.getMappings()
                .get(collectionReferenceDescription.params.indexName).get(collectionReferenceDescription.params.typeName).sourceAsMap().get("properties");
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
