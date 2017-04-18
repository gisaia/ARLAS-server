package io.arlas.server.rest.admin;

import io.arlas.server.model.dao.DummyCollectionReferenceDaoImpl;

public class DummyCollectionService extends CollectionService {
    
    public DummyCollectionService() {
	super();
	this.dao = new DummyCollectionReferenceDaoImpl();;
    }
}
