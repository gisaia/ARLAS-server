package io.arlas.server.rest.explore;

import org.elasticsearch.client.transport.TransportClient;
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


}
