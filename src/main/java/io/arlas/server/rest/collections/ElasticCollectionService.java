package io.arlas.server.rest.collections;

import org.elasticsearch.client.transport.TransportClient;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;

public class ElasticCollectionService extends CollectionService {

    public ElasticCollectionService(TransportClient client, ArlasServerConfiguration configuration) {
        super();
        this.dao = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
    }

}
