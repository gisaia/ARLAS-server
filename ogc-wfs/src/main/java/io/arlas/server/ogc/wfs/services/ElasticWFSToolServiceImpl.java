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
package io.arlas.server.ogc.wfs.services;

import io.arlas.server.ElasticFilter;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ValueCollectionType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.arlas.server.utils.CheckParams.isBboxLatLonInCorrectRanges;

public class ElasticWFSToolServiceImpl implements WFSToolService {
    ExploreServices exploreServices;
    BoolQueryBuilder wfsQuery = QueryBuilders.boolQuery();


    public ElasticWFSToolServiceImpl(ExploreServices exploreServices) {
        this.exploreServices = exploreServices;
    }

    @Override
    public CollectionReferenceDescription getCollectionReferenceDescription(CollectionReference collectionReference) throws IOException {
        ElasticAdmin elasticAdmin = new ElasticAdmin(exploreServices.getClient());
        return elasticAdmin.describeCollection(collectionReference);
    }

    @Override
    public Object getFeature(String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter, CollectionReference collectionReference, String[] excludes) throws ArlasException, IOException {
        buildWFSQuery(WFSRequestType.GetFeature, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference);
        SearchHit response;
        try {
            SearchHits hitsGetFeature = exploreServices.getClient()
                    .prepareSearch(collectionReference.params.indexName)
                    .setFetchSource(null, excludes)
                    .setQuery(wfsQuery)
                    .execute()
                    .get()
                    .getHits();
            if (hitsGetFeature.getHits().length > 0) {
                response = hitsGetFeature.getAt(0);
            } else {
                throw new OGCException(OGCExceptionCode.NOT_FOUND, "Data not found", "resourceid", Service.WFS);
            }
        } catch (InterruptedException e) {
            throw new InternalServerErrorException("Cannot fetch feature : " + e.getMessage());
        } catch (ExecutionException e) {
            throw new InternalServerErrorException("Cannot fetch feature : " + e.getMessage());
        }
        return response;
    }

    @Override
    public List<Object> getFeatures(String id, String bbox, String constraint, String resourceid, String partitionFilter, CollectionReference collectionReference, String[] excludes, Integer startindex, Integer count) throws ArlasException, IOException {
        buildWFSQuery(null, id, bbox, constraint, resourceid, null, partitionFilter, collectionReference);
        List<Object> featureList = new ArrayList<>();
        try {
            SearchHits hitsGetFeature = exploreServices
                    .getClient()
                    .prepareSearch(collectionReference.params.indexName)
                    .setFetchSource(null, excludes)
                    .setQuery(wfsQuery)
                    .setFrom(startindex)
                    .setSize(count)
                    .execute()
                    .get()
                    .getHits();
            for (int i = 0; i < hitsGetFeature.getHits().length; i++) {
                featureList.add(hitsGetFeature.getAt(i));
            }
        }catch (InterruptedException e) {
            throw new InternalServerErrorException("Cannot fetch features : " + e.getMessage());
        } catch (ExecutionException e) {
            throw new InternalServerErrorException("Cannot fetch features : " + e.getMessage());
        }
        return featureList;
    }

    @Override
    public ValueCollectionType getPropertyValue (String id, String bbox, String constraint, String resourceid, String storedquery_id,
                                                 String partitionFilter, CollectionReference collectionReference, String include, String[] excludes, Integer startindex, Integer count) throws ArlasException, IOException {
        buildWFSQuery(WFSRequestType.GetPropertyValue, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference);
        ValueCollectionType valueCollectionType = new ValueCollectionType();
        try {
            SearchHits hitsGetPropertyValue = exploreServices.getClient()
                    .prepareSearch(collectionReference.params.indexName)
                    .setFetchSource(new String[]{include}, excludes)
                    .setQuery(wfsQuery)
                    .setFrom(startindex)
                    .setSize(count)
                    .execute()
                    .get()
                    .getHits();
            for (int i = 0; i < hitsGetPropertyValue.getHits().length; i++) {
                MemberPropertyType e = new MemberPropertyType();
                e.getContent().add(MapExplorer.getObjectFromPath(include, hitsGetPropertyValue.getAt(i).getSourceAsMap()));
                valueCollectionType.getMember().add(e);
            }
        }catch (InterruptedException e) {
            throw new InternalServerErrorException("Cannot fetch property value : " + e.getMessage() );
        } catch (ExecutionException e) {
            throw new InternalServerErrorException("Cannot fetch property value : " + e.getMessage());
        }
        return valueCollectionType;
    }


    private void buildWFSQuery(WFSRequestType requestType, String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter, CollectionReference collectionReference) throws ArlasException, IOException{
        wfsQuery = QueryBuilders.boolQuery();
        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(getCollectionReferenceDescription(collectionReference));
        addCollectionFilter(fluidSearch, collectionReference);
        if (constraint != null) {
            wfsQuery.filter(ElasticFilter.filter(constraint, getCollectionReferenceDescription(collectionReference), Service.WFS));
        } else if (bbox != null) {
            buildBboxQuery(bbox, collectionReference);
        } else if (resourceid != null) {
            buildRessourceIdQuery(resourceid, collectionReference);
        } else if (storedquery_id != null) {
            buildStoredQueryIdQuery(id, storedquery_id, requestType, collectionReference);
        }
        if (partitionFilter != null) {
            addPartitionFilter(fluidSearch, partitionFilter);
        }
    }

    private void buildBboxQuery(String bbox, CollectionReference collectionReference) throws OGCException {
        double[] tlbr = GeoFormat.toDoubles(bbox,Service.WFS);
        if (!(isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, FluidSearch.INVALID_BBOX, "bbox", Service.WFS);
        }
        wfsQuery.filter(getBBoxBoolQueryBuilder(bbox, collectionReference.params.centroidPath));
    }

    private void buildRessourceIdQuery(String resourceid,  CollectionReference collectionReference) {
        if (resourceid.contains(",")) {
            BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String resourceIdValue : Arrays.asList(resourceid.split(","))) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(collectionReference.params.idPath, resourceIdValue));
            }
            wfsQuery = wfsQuery.filter(orBoolQueryBuilder);
        } else {
            wfsQuery.filter(QueryBuilders.matchQuery(collectionReference.params.idPath, resourceid));
        }
    }

    private void buildStoredQueryIdQuery(String id, String storedquery_id, WFSRequestType requestType, CollectionReference collectionReference) throws OGCException {
        if (!storedquery_id.equals(WFSConstant.GET_FEATURE_BY_ID_NAME)) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "StoredQuery " + storedquery_id + " not found", "storedquery_id", Service.WFS);
        }
        if (requestType !=null && requestType.equals(WFSRequestType.GetFeature)) {
            if (id != null) {
                wfsQuery.filter(QueryBuilders.matchQuery(collectionReference.params.idPath, id));
            } else {
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, "'id' parameter is missing for the StoredQuery : " + storedquery_id, "storedquery_id", Service.WFS);
            }
        }
    }

    private void addPartitionFilter(FluidSearch fluidSearch, String partitionFilter) throws ArlasException, IOException {
        Filter headerFilter = ParamsParser.getFilter(partitionFilter);
        exploreServices.applyFilter(headerFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private void addCollectionFilter(FluidSearch fluidSearch, CollectionReference collectionReference) throws ArlasException, IOException {
        Filter collectionFilter = collectionReference.params.filter;
        exploreServices.applyFilter(collectionFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private BoolQueryBuilder getBBoxBoolQueryBuilder(String bbox, String centroidPath) throws OGCException {
        double[] tlbr = GeoFormat.toDoubles(bbox, Service.WFS);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        GeoPoint topLeft = new GeoPoint(tlbr[3], tlbr[0]);
        GeoPoint bottomRight = new GeoPoint(tlbr[1], tlbr[2]);
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        orBoolQueryBuilder = orBoolQueryBuilder
                .should(QueryBuilders
                        .geoBoundingBoxQuery(centroidPath).setCorners(topLeft, bottomRight));
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch(1);
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder);
        return boolQueryBuilder;
    }

}
