package io.arlas.server.rest.explore;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

public class ExploreServices {
    private TransportClient client;

    public ExploreServices(TransportClient client) {
        this.client = client;
    }
    public TransportClient getClient() {
        return client;
    }

    public void setClient(TransportClient client) {
        this.client = client;
    }

    public SearchRequestBuilder init(String collection){
        return client.prepareSearch(collection);
    }


}
