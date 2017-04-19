package io.arlas.server.rest.explore;

import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.DummyCollectionReferenceDaoImpl;
import io.arlas.server.model.CollectionReference;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

public class ExploreServices {
    private TransportClient client;
    private CollectionReferenceDao daoCollectionReference;

    public ExploreServices(TransportClient client) {
        this.client = client;
        this.daoCollectionReference =  new DummyCollectionReferenceDaoImpl();
    }
    public TransportClient getClient() {
        return client;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }

    public CollectionReferenceDao getDaoCollectionReference() {
        return daoCollectionReference;
    }

    public void setDaoCollectionReference(CollectionReferenceDao daoCollectionReference) {
        this.daoCollectionReference = daoCollectionReference;
    }

    public SearchRequestBuilder init(CollectionReference collection){
        return client.prepareSearch(collection.getParams().getIndexName());
    }
}
