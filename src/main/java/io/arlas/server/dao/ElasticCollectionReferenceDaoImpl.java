package io.arlas.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;

public class ElasticCollectionReferenceDaoImpl implements CollectionReferenceDao {

    TransportClient client = null;
    String arlasIndex = null;

    public ElasticCollectionReferenceDaoImpl(TransportClient client, String arlasIndex) {
        super();
        this.client = client;
        this.arlasIndex = arlasIndex;
    }

    private CollectionReferenceParameters getCollectionReferenceParameters(Map<String, Object> source) {
        CollectionReferenceParameters params = new CollectionReferenceParameters();
        for (String field : source.keySet()) {
            switch (field) {
            case CollectionReference.INDEX_NAME:
                params.indexName = source.get(field) != null ? source.get(field).toString() : "";
                break;
            case CollectionReference.TYPE_NAME:
                params.typeName = source.get(field) != null ? source.get(field).toString() : "";
                break;
            case CollectionReference.ID_PATH:
                params.idPath = source.get(field) != null ? source.get(field).toString() : "";
                break;
            case CollectionReference.GEOMETRY_PATH:
                params.geometryPath = source.get(field) != null ? source.get(field).toString() : "";
                break;
            case CollectionReference.CENTROID_PATH:
                params.centroidPath = source.get(field) != null ? source.get(field).toString() : "";
                break;
            case CollectionReference.TIMESTAMP_PATH:
                params.timestampPath = source.get(field) != null ? source.get(field).toString() : "";
                break;
            }
        }
        return params;
    }

    @Override
    public CollectionReference getCollectionReference(String ref) throws NotFoundException {
        CollectionReference collection = null;
        GetResponse response = client.prepareGet(arlasIndex, "collection", ref).get();
        Map<String, Object> source = response.getSource();
        if (source != null) {
            collection = new CollectionReference(ref);
            collection.params = getCollectionReferenceParameters(source);
        } else {
            throw new NotFoundException("Collection " + ref + " not found.");
        }
        return collection;
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() throws InternalServerErrorException {
        List<CollectionReference> collections = new ArrayList<CollectionReference>();

        try {
            QueryBuilder qb = QueryBuilders.matchAllQuery();
            SearchResponse scrollResp = client.prepareSearch(arlasIndex)
                    .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC).setScroll(new TimeValue(60000))
                    .setQuery(qb).setSize(100).get(); // max of 100 hits will be
                                                      // returned for each
                                                      // scroll

            // Scroll until no hits are returned
            do {
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    System.out.println(hit + " = " + hit.getSource());
                    CollectionReference collection = new CollectionReference(hit.getId());
                    collection.params = getCollectionReferenceParameters(hit.getSource());
                    collections.add(collection);
                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000))
                        .execute().actionGet();
            } while (scrollResp.getHits().getHits().length != 0); // Zero hits
                                                                  // mark the
                                                                  // end of the
                                                                  // scroll and
                                                                  // the while
                                                                  // loop.
        } catch (IndexNotFoundException e) {
            throw new InternalServerErrorException("Collections not found.");
        }

        return collections;
    }

    @Override
    public CollectionReference putCollectionReference(String ref, CollectionReferenceParameters desc)
            throws InternalServerErrorException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        om.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        IndexResponse response = client.prepareIndex(arlasIndex, "collection", ref)
                .setSource(om.writeValueAsString(desc)).get();
        if (response.status().getStatus() != RestStatus.OK.getStatus()
                && response.status().getStatus() != RestStatus.CREATED.getStatus())
            throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
        else
            return new CollectionReference(ref, desc);
    }

    @Override
    public void deleteCollectionReference(String ref) throws NotFoundException, InternalServerErrorException {
        DeleteResponse response = client.prepareDelete(arlasIndex, "collection", ref).get();
        if (response.status().equals(RestStatus.NOT_FOUND))
            throw new NotFoundException("collection " + ref + " not found.");
        else if (!response.status().equals(RestStatus.OK))
            throw new InternalServerErrorException("Unable to delete collection : " + response.status().toString());
    }

}
