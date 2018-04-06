/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.rest.explore;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.AggregationMetric;
import io.arlas.server.model.response.AggregationResponse;
import io.arlas.server.rest.ResponseCacheManager;
import io.arlas.server.rest.explore.enumerations.MetricAggregationEnum;
import io.arlas.server.utils.CheckParams;
import org.apache.lucene.geo.Rectangle;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.geobounds.GeoBounds;
import org.elasticsearch.search.aggregations.metrics.geocentroid.GeoCentroid;
import org.geojson.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExploreServices {
    private Client client;
    private CollectionReferenceDao daoCollectionReference;
    private ResponseCacheManager responseCacheManager = null;

    public ExploreServices(Client client, ArlasServerConfiguration configuration) {
        this.client = client;
        this.daoCollectionReference = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
        this.responseCacheManager = new ResponseCacheManager(configuration.arlasrestcachetimeout);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
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

    public SearchHits count(MixedRequest request, CollectionReference collectionReference) throws ArlasException, IOException {
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        return fluidSearch.exec().getHits();
    }

    public SearchHits search(MixedRequest request, CollectionReference collectionReference) throws ArlasException, IOException {
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        applySize(((Search) request.basicRequest).size, fluidSearch);
        applySort(((Search) request.basicRequest).sort, fluidSearch);
        applyProjection(((Search) request.basicRequest).projection, fluidSearch);
        return fluidSearch.exec().getHits();
    }

    public SearchResponse aggregate(MixedRequest request, CollectionReference collectionReference, Boolean isGeoAggregation) throws ArlasException, IOException {
        CheckParams.checkAggregationRequest(request.basicRequest);
        FluidSearch fluidSearch = new FluidSearch(client);
        fluidSearch.setCollectionReference(collectionReference);
        applyFilter(request.basicRequest.filter, fluidSearch);
        applyFilter(request.headerRequest.filter, fluidSearch);
        applyAggregation(((AggregationsRequest) request.basicRequest).aggregations, fluidSearch, isGeoAggregation);
        return fluidSearch.exec();
    }

    protected void applyAggregation(List<Aggregation> aggregations, FluidSearch fluidSearch, Boolean isGeoAggregation) throws ArlasException {
        if (aggregations != null && aggregations != null && !aggregations.isEmpty()) {
            fluidSearch = fluidSearch.aggregate(aggregations, isGeoAggregation);
        }
    }


    public void applyFilter(Filter filter, FluidSearch fluidSearch) throws ArlasException, IOException {
        if (filter != null) {
            CheckParams.checkFilter(filter);
            if (filter.f != null && !filter.f.isEmpty()) {
                for (MultiValueFilter<Expression> f : filter.f) {
                    fluidSearch = fluidSearch.filter(f);
                }
            }
            if (filter.q != null && !filter.q.isEmpty()) {
                for (MultiValueFilter<String> q : filter.q) {
                    fluidSearch = fluidSearch.filterQ(q);
                }
            }
            if (filter.pwithin != null && !filter.pwithin.isEmpty()) {
                for (MultiValueFilter<String> pw : filter.pwithin) {
                    fluidSearch = fluidSearch.filterPWithin(pw);
                }
            }
            if (filter.gwithin != null && !filter.gwithin.isEmpty()) {
                for (MultiValueFilter<String> gw : filter.gwithin) {
                    fluidSearch = fluidSearch.filterGWithin(gw);
                }
            }
            if (filter.gintersect != null && !filter.gintersect.isEmpty()) {
                for (MultiValueFilter<String> gi : filter.gintersect) {
                    fluidSearch = fluidSearch.filterGIntersect(gi);
                }
            }
            if (filter.notpwithin != null && !filter.notpwithin.isEmpty()) {
                for (MultiValueFilter<String> npw : filter.notpwithin) {
                    fluidSearch = fluidSearch.filterNotPWithin(npw);
                }
            }
            if (filter.notgwithin != null && !filter.notgwithin.isEmpty()) {
                for (MultiValueFilter<String> ngw : filter.notgwithin) {
                    fluidSearch = fluidSearch.filterNotGWithin(ngw);
                }
            }
            if (filter.notgintersect != null && !filter.notgintersect.isEmpty()) {
                for (MultiValueFilter<String> ngi : filter.notgintersect) {
                    fluidSearch = fluidSearch.filterNotGIntersect(ngi);
                }
            }
        }
    }

    protected void applySize(Size size, FluidSearch fluidSearch) throws ArlasException, IOException {
        if (size != null) {
            CheckParams.checkSize(size);
            if (size.size != null && size.from != null) {
                fluidSearch = fluidSearch.filterSize(size.size, size.from);
            }
        }
    }

    protected void applySort(Sort sort, FluidSearch fluidSearch) throws ArlasException, IOException {
        if (sort != null && sort.sort != null) {
            fluidSearch = fluidSearch.sort(sort.sort);
        }
    }

    protected void applyProjection(Projection projection, FluidSearch fluidSearch) {
        if (projection != null && !Strings.isNullOrEmpty(projection.includes)) {
            fluidSearch = fluidSearch.include(projection.includes);
        }
        if (projection != null && !Strings.isNullOrEmpty(projection.excludes)) {
            fluidSearch = fluidSearch.exclude(projection.excludes);
        }
    }

    public AggregationResponse formatAggregationResult(MultiBucketsAggregation aggregation, AggregationResponse aggregationResponse) {
        aggregationResponse.name = aggregation.getName();
        if (aggregationResponse.name.equals(FluidSearch.TERM_AGG)) {
            aggregationResponse.sumotherdoccounts = ((Terms) aggregation).getSumOfOtherDocCounts();
        }

        aggregationResponse.elements = new ArrayList<AggregationResponse>();
        List<MultiBucketsAggregation.Bucket> buckets = (List<MultiBucketsAggregation.Bucket>) aggregation.getBuckets();
        buckets.forEach(bucket -> {
            AggregationResponse element = new AggregationResponse();
            element.keyAsString = bucket.getKeyAsString();
            if (aggregationResponse.name.equals(FluidSearch.GEOHASH_AGG)) {
                element.key = getGeohashCentre(element.keyAsString.toString());
            } else {
                element.key = bucket.getKey();
            }
            element.count = bucket.getDocCount();
            element.elements = new ArrayList<AggregationResponse>();
            if (bucket.getAggregations().asList().size() == 0) {
                element.elements = null;
                aggregationResponse.elements.add(element);
            } else {
                bucket.getAggregations().forEach(subAggregation -> {
                    AggregationResponse subAggregationResponse = new AggregationResponse();
                    subAggregationResponse.name = subAggregation.getName();
                    if (subAggregationResponse.name.equals(FluidSearch.TERM_AGG)) {
                        subAggregationResponse.sumotherdoccounts = ((Terms) subAggregation).getSumOfOtherDocCounts();
                    }
                    if (subAggregation.getName().equals(FluidSearch.DATEHISTOGRAM_AGG) || subAggregation.getName().equals(FluidSearch.GEOHASH_AGG) || subAggregation.getName().equals(FluidSearch.HISTOGRAM_AGG) || subAggregation.getName().equals(FluidSearch.TERM_AGG)) {
                        subAggregationResponse = formatAggregationResult(((MultiBucketsAggregation) subAggregation), subAggregationResponse);
                    } else {
                        subAggregationResponse.elements = null;
                        AggregationMetric aggregationMetric = new AggregationMetric();
                        aggregationMetric.type = subAggregation.getName();
                        if (!aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase()) && !aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase())
                                && !aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase() + "-bucket") && !aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase() + "-bucket")) {
                            aggregationMetric.value = (((InternalAggregation) subAggregation).getProperty("value"));
                        } else {
                            FeatureCollection fc = new FeatureCollection();
                            Feature feature = new Feature();
                            if (aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase()) || aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase() + "-bucket")) {
                                Polygon box = createBox((GeoBounds) subAggregation);
                                GeoJsonObject g = box;
                                if (aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase() + "-bucket")) {
                                    element.BBOX = box;
                                }
                                feature.setGeometry(g);
                                fc.add(feature);
                            } else if (aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase()) || aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase() + "-bucket")) {
                                GeoPoint centroid = ((GeoCentroid) subAggregation).centroid();
                                GeoJsonObject g = new Point(centroid.getLon(), centroid.getLat());
                                if (aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase() + "-bucket")) {
                                    element.centroid = (Point) g;
                                }
                                feature.setGeometry(g);
                                fc.add(feature);
                            }
                            aggregationMetric.value = fc;
                        }
                        // No need to add the geocentroid or the geobox as metric if withGeoCentroid or withGeoBBox is true (respectively)
                        if (!aggregationMetric.type.equals(MetricAggregationEnum.GEOBBOX.name().toLowerCase() + "-bucket") && !aggregationMetric.type.equals(MetricAggregationEnum.GEOCENTROID.name().toLowerCase() + "-bucket")) {
                            subAggregationResponse.metric = aggregationMetric;
                        } else {
                            subAggregationResponse = null;
                        }
                    }
                    if (subAggregationResponse != null) {
                        element.elements.add(subAggregationResponse);
                    }
                });
                aggregationResponse.elements.add(element);
            }
        });
        return aggregationResponse;
    }

    private GeoPoint getGeohashCentre(String geohash) {
        Rectangle bbox = GeoHashUtils.bbox(geohash);

        Double maxLon = bbox.maxLon;
        Double minLon = bbox.minLon;
        Double lon = (maxLon + minLon) / 2;

        Double maxLat = bbox.maxLat;
        Double minLat = bbox.minLat;
        Double lat = (maxLat + minLat) / 2;

        return new GeoPoint(lat, lon);
    }

    private Polygon createBox(GeoBounds subAggregation) {
        Polygon box = new Polygon();
        GeoPoint topLeft = subAggregation.topLeft();
        GeoPoint bottomRight = subAggregation.bottomRight();
        List<LngLatAlt> bounds = new ArrayList<>();
        bounds.add(new LngLatAlt(topLeft.getLon(), topLeft.getLat()));
        bounds.add(new LngLatAlt(bottomRight.getLon(), topLeft.getLat()));
        bounds.add(new LngLatAlt(bottomRight.getLon(), bottomRight.getLat()));
        bounds.add(new LngLatAlt(topLeft.getLon(), bottomRight.getLat()));
        box.add(bounds);

        return box;
    }
}
