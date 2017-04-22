package io.arlas.server.rest.collections;

import io.arlas.server.dao.DummyCollectionReferenceDaoImpl;

public class DummyCollectionService extends CollectionService {
    
    public DummyCollectionService() {
	super();
	this.dao = new DummyCollectionReferenceDaoImpl();;
    }
}
