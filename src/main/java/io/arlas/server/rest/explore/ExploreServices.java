package io.arlas.server.rest.explore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.arlas.server.model.request.*;
import io.arlas.server.rest.ResponseCacheManager;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.model.response.AggregationMetric;
import io.arlas.server.utils.CheckParams;

public class ExploreServices {
    private TransportClient client;
    private CollectionReferenceDao daoCollectionReference;
    private ResponseCacheManager responseCacheManager = null;

    public ExploreServices(TransportClient client, ArlasServerConfiguration configuration) {
        this.client = client;
        this.daoCollectionReference = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
        this.responseCacheManager = new ResponseCacheManager(configuration.arlasrestcachetimeout);
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

    public ResponseCacheManager getResponseCacheManager() {
        return responseCacheManager;
    }

    public SearchRequestBuilder init(CollectionReference collection) {
        return client.prepareSearch(collection.params.indexName);
    }

    public SearchHits count(Count count, CollectionReference collectionReference) throws ArlasException, IOException {
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(count.filter,fluidSearch);
        return fluidSearch.exec().getHits();
    }

    public SearchHits search(Search search, CollectionReference collectionReference) throws ArlasException, IOException{
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(search.filter,fluidSearch);
        applySize(search.size,fluidSearch);
        applySort(search.sort,fluidSearch);
        return fluidSearch.exec().getHits();
    }

    public SearchResponse aggregate(AggregationsRequest aggregationsRequest, CollectionReference collectionReference, Boolean isGeoAggregation) throws ArlasException,IOException{
        CheckParams.checkAggregationRequest(aggregationsRequest);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(aggregationsRequest.filter,fluidSearch);
        applyAggregation(aggregationsRequest.aggregations,fluidSearch,isGeoAggregation);
        return fluidSearch.exec();
    }

    protected void applyAggregation(List<Aggregation> aggregations, FluidSearch fluidSearch, Boolean isGeoAggregation) throws ArlasException{
        if (aggregations != null && aggregations !=null && !aggregations.isEmpty()){
            fluidSearch = fluidSearch.aggregate(aggregations,isGeoAggregation);
        }
    }


    protected void applyFilter (Filter filter, FluidSearch fluidSearch) throws ArlasException, IOException{
        if (filter !=null) {
            CheckParams.checkFilter(filter);
            if (filter.f != null && !filter.f.isEmpty()) {
                fluidSearch = fluidSearch.filter(filter.f);
            }
            if (filter.q != null && !filter.q.isEmpty()) {
                fluidSearch = fluidSearch.filterQ(filter.q);
            }
            if (filter.before != null && filter.after == null) {
                fluidSearch = fluidSearch.filterBefore(filter.before);
            }
            if (filter.after != null && filter.before == null) {
                fluidSearch = fluidSearch.filterAfter(filter.after);
            }
            if(filter.after != null && filter.before != null) {
                fluidSearch = fluidSearch.filterAfterBefore(filter.after, filter.before);
            }
            if (filter.pwithin != null && !filter.pwithin.isEmpty()) {
                double[] tlbr = CheckParams.toDoubles(filter.pwithin);
                fluidSearch = fluidSearch.filterPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            }
            if (filter.gwithin != null && !filter.gwithin.isEmpty()) {
                fluidSearch = fluidSearch.filterGWithin(filter.gwithin);
            }
            if (filter.gintersect != null && !filter.gintersect.isEmpty()) {
                fluidSearch = fluidSearch.filterGIntersect(filter.gintersect);
            }
            if (filter.notpwithin != null && !filter.notpwithin.isEmpty()) {
                double[] tlbr = CheckParams.toDoubles(filter.notpwithin);
                fluidSearch = fluidSearch.filterNotPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            }
            if (filter.notgwithin != null && !filter.notgwithin.isEmpty()) {
                fluidSearch = fluidSearch.filterNotGWithin(filter.notgwithin);
            }
            if (filter.notgintersect != null && !filter.notgintersect.isEmpty()) {
                fluidSearch = fluidSearch.filterNotGIntersect(filter.notgintersect);
            }
        }
    }

    protected void applySize(Size size, FluidSearch fluidSearch) throws ArlasException, IOException{
        if (size != null){
            CheckParams.checkSize(size);
            if (size.size != null && size.from != null){
                fluidSearch = fluidSearch.filterSize(size.size,size.from);
            }
        }
    }

    protected void applySort(Sort sort, FluidSearch fluidSearch) throws ArlasException, IOException{
        if (sort != null && sort.sort != null){
            fluidSearch = fluidSearch.sort(sort.sort);
        }
    }

    public AggregationResponse formatAggregationResult(MultiBucketsAggregation aggregation, AggregationResponse aggregationResponse){
        aggregationResponse.name = aggregation.getName();
        aggregationResponse.elements = new ArrayList<AggregationResponse>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>)aggregation.getBuckets();
        buckets.forEach(bucket -> {
            AggregationResponse element = new AggregationResponse();
            element.key = bucket.getKey();
            element.keyAsString = bucket.getKeyAsString();
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<AggregationResponse>();
            if (bucket.getAggregations().asList().size() == 0){
                element.elements = null;
                aggregationResponse.elements.add(element);
            }
            else {
                bucket.getAggregations().forEach(subAggregation -> {
                    AggregationResponse subAggregationResponse = new AggregationResponse();
                    subAggregationResponse.name = subAggregation.getName();
                    if (subAggregation.getName().equals(FluidSearch.DATEHISTOGRAM_AGG) || subAggregation.getName().equals(FluidSearch.GEOHASH_AGG) || subAggregation.getName().equals(FluidSearch.HISTOGRAM_AGG) ||subAggregation.getName().equals(FluidSearch.TERM_AGG)){
                        subAggregationResponse = formatAggregationResult(((MultiBucketsAggregation)subAggregation), subAggregationResponse);
                    } else{
                        subAggregationResponse.elements = null;
                        AggregationMetric aggregationMetric = new AggregationMetric();
                        aggregationMetric.type = subAggregation.getName();
                        aggregationMetric.value = (Double)subAggregation.getProperty("value");
                        subAggregationResponse.metric = aggregationMetric;
                    }
                    element.elements.add(subAggregationResponse);
                });
                aggregationResponse.elements.add(element);
            }
        });
        return aggregationResponse;
    }
}
