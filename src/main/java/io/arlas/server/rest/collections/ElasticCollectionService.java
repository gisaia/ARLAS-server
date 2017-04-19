package io.arlas.server.rest.collections;

import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;

public class ElasticCollectionService extends CollectionService {
    
    public ElasticCollectionService(TransportClient client) {
	super();
	this.dao = new ElasticCollectionReferenceDaoImpl(client);
    }
    
}
