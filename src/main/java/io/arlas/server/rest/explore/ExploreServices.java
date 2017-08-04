package io.arlas.server.rest.explore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import io.arlas.server.model.request.AggregationRequest;
import io.arlas.server.model.request.Aggregations;
import io.arlas.server.model.request.Count;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.request.Size;
import io.arlas.server.model.request.Sort;
import io.arlas.server.model.response.ArlasAggregation;
import io.arlas.server.model.response.ArlasMetric;
import io.arlas.server.utils.CheckParams;

public class ExploreServices {
    private TransportClient client;
    private CollectionReferenceDao daoCollectionReference;

    public ExploreServices(TransportClient client, ArlasServerConfiguration configuration) {
        this.client = client;
        this.daoCollectionReference = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
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

    public SearchResponse aggregate(AggregationRequest aggregationRequest, CollectionReference collectionReference, Boolean isGeoAggregation) throws ArlasException,IOException{
        CheckParams.checkAggregationRequest(aggregationRequest);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(aggregationRequest.filter,fluidSearch);
        applyAggregation(aggregationRequest.aggregations,fluidSearch,isGeoAggregation);
        return fluidSearch.exec();
    }

    protected void applyAggregation(Aggregations aggregations, FluidSearch fluidSearch, Boolean isGeoAggregation) throws ArlasException{
        if (aggregations != null && aggregations.aggregations !=null && !aggregations.aggregations.isEmpty()){
            fluidSearch = fluidSearch.aggregate(aggregations.aggregations,isGeoAggregation);
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

    public ArlasAggregation formatAggregationResult(MultiBucketsAggregation aggregation, ArlasAggregation arlasAggregation){
        arlasAggregation.name = aggregation.getName();
        arlasAggregation.elements = new ArrayList<ArlasAggregation>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>)aggregation.getBuckets();
        buckets.forEach(bucket -> {
            ArlasAggregation element = new ArlasAggregation();
            element.key = bucket.getKey();
            element.keyAsString = bucket.getKeyAsString();
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<ArlasAggregation>();
            if (bucket.getAggregations().asList().size() == 0){
                element.elements = null;
                arlasAggregation.elements.add(element);
            }
            else {
                bucket.getAggregations().forEach(subAggregation -> {
                    ArlasAggregation subArlasAggregation = new ArlasAggregation();
                    subArlasAggregation.name = subAggregation.getName();
                    if (subAggregation.getName().equals(FluidSearch.DATEHISTOGRAM_AGG) || subAggregation.getName().equals(FluidSearch.GEOHASH_AGG) || subAggregation.getName().equals(FluidSearch.HISTOGRAM_AGG) ||subAggregation.getName().equals(FluidSearch.TERM_AGG)){
                        subArlasAggregation = formatAggregationResult(((MultiBucketsAggregation)subAggregation), subArlasAggregation);
                    } else{
                        subArlasAggregation.elements = null;
                        ArlasMetric arlasMetric = new ArlasMetric();
                        arlasMetric.type = subAggregation.getName();
                        arlasMetric.value = (Double)subAggregation.getProperty("value");
                        subArlasAggregation.metric = arlasMetric;
                    }
                    element.elements.add(subArlasAggregation);
                });
                arlasAggregation.elements.add(element);
            }
        });
        return arlasAggregation;
    }
}
