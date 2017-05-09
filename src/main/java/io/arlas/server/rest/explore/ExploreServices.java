package io.arlas.server.rest.explore;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.model.CollectionReference;

public class ExploreServices {
    private TransportClient client;
    private CollectionReferenceDao daoCollectionReference;

    public ExploreServices(TransportClient client, String arlasIndex) {
        this.client = client;
        this.daoCollectionReference = new ElasticCollectionReferenceDaoImpl(client, arlasIndex);
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

    public SearchRequestBuilder init(CollectionReference collection) {
        return client.prepareSearch(collection.params.indexName);
    }
}
