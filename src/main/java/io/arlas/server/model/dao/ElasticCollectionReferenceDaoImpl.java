package io.arlas.server.model.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;

public class ElasticCollectionReferenceDaoImpl implements CollectionReferenceDao {
    
    TransportClient client = null;
    
    public ElasticCollectionReferenceDaoImpl(TransportClient client) {
	super();
	this.client = client;
    }
    
    private CollectionReferenceParameters getCollectionReferenceParameters(Map<String,Object> source) {
	CollectionReferenceParameters params = new CollectionReferenceParameters();
	for(String field : source.keySet()) {
	    switch(field) {
	    	case CollectionReference.INDEX_NAME:
	    	    params.setIndexName(source.get(field).toString());
	    	    break;
	    	case CollectionReference.TYPE_NAME:
	    	    params.setTypeName(source.get(field).toString());
	    	    break;
	    	case CollectionReference.ID_PATH:
	    	    params.setIdPath(source.get(field).toString());
	    	    break;
	    	case CollectionReference.GEOMETRY_PATH:
	    	    params.setGeometryPath(source.get(field).toString());
	    	    break;
	    	case CollectionReference.CENTROID_PATH:
	    	    params.setCentroidPath(source.get(field).toString());
	    	    break;
	    	case CollectionReference.TIMESTAMP_PATH:
	    	    params.setTimestampPath(source.get(field).toString());
	    	    break;
	    }
	}
	return params;
    }

    @Override
    public CollectionReference getCollectionReference(String ref) {
	CollectionReference collection = null;
	GetResponse response = client.prepareGet("arlas_collections", "collection", ref).get();
	Map<String,Object> source = response.getSource();
	if(source != null) {
	    collection = new CollectionReference(ref);
	    collection.setParams(getCollectionReferenceParameters(source));
	}
	return collection;
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() {
	List<CollectionReference> collections = new ArrayList<CollectionReference>();
	
	QueryBuilder qb = QueryBuilders.matchAllQuery();
	SearchResponse scrollResp = client.prepareSearch("arlas_collections")
	        .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
	        .setScroll(new TimeValue(60000))
	        .setQuery(qb)
	        .setSize(100).get(); //max of 100 hits will be returned for each scroll
	
	//Scroll until no hits are returned
	do {
	    for (SearchHit hit : scrollResp.getHits().getHits()) {
		System.out.println(hit + " = " + hit.getSource());
		CollectionReference collection = new CollectionReference(hit.getId());
		collection.setParams(getCollectionReferenceParameters(hit.getSource()));
		collections.add(collection);
	    }
	    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
	} while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
	
	return collections;
    }

    @Override
    public void putCollectionReference(String ref, CollectionReferenceParameters desc) {
	IndexResponse response = client.prepareIndex("arlas_collections", "collection", ref)
	        .setSource(desc.toJsonString())
	        .get();
	if(response.status().getStatus() != RestStatus.OK.getStatus()
		&& response.status().getStatus() != RestStatus.CREATED.getStatus())
	    throw new InternalServerErrorException("Unable to index collection : " + response.status().toString());
    }

    @Override
    public void deleteCollectionReference(String ref) throws NotFoundException {
	DeleteResponse response = client.prepareDelete("arlas_collections", "collection", ref).get();
	if(response.status().equals(RestStatus.NOT_FOUND))
	    throw new NotFoundException("collection " + ref + " not found");
	else if(!response.status().equals(RestStatus.OK))
	    throw new InternalServerErrorException("Unable to delete collection : " + response.status().toString());
    }

}
