package io.arlas.server.core;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.model.CollectionReference;

public class ElasticDocument {

    public TransportClient client;
    
    public ElasticDocument(TransportClient client){
        this.client = client;
    }
    
    public Map<String,Object> getSource (CollectionReference collectionReference, String identifier) throws IOException {
        GetResponse response = client.prepareGet(collectionReference.params.indexName, collectionReference.params.typeName, identifier).get();
        return response.getSource();
    }
}
