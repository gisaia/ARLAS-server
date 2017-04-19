package io.arlas.server.rest.admin;

import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.model.dao.ElasticCollectionReferenceDaoImpl;

public class ElasticCollectionService extends CollectionService {
    
    public ElasticCollectionService(TransportClient client) {
	super();
	this.dao = new ElasticCollectionReferenceDaoImpl(client);
    }
    
}
