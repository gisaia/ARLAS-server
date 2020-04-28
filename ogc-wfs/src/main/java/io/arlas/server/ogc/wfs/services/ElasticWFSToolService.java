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

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.impl.elastic.core.FluidSearch;
import io.arlas.server.impl.elastic.services.ElasticExploreService;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.ElasticFilter;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import io.arlas.server.utils.ColumnFilterUtil;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.ParamsParser;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ValueCollectionType;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;

import static io.arlas.server.utils.CheckParams.isBboxLatLonInCorrectRanges;

public class ElasticWFSToolService implements WFSToolService {
    ElasticExploreService exploreServices;
    BoolQueryBuilder wfsQuery = QueryBuilders.boolQuery();


    public ElasticWFSToolService(ElasticExploreService exploreServices) {
        this.exploreServices = exploreServices;
    }

    @Override
    public CollectionReferenceDescription getCollectionReferenceDescription(CollectionReference collectionReference) throws ArlasException {
        return exploreServices.getDaoCollectionReference().describeCollection(collectionReference);
    }

    @Override
    public Map<String, Object> getFeature(String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter, CollectionReference collectionReference, String[] excludes, Optional<String> columnFilter) throws ArlasException, IOException {
        buildWFSQuery(WFSRequestType.GetFeature, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference, columnFilter);
        String[] includes = columnFilterToIncludes(collectionReference, columnFilter);
        SearchRequest request = new SearchRequest(collectionReference.params.indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(includes, excludes)
                .query(wfsQuery);
        request.source(searchSourceBuilder);
        SearchHits hitsGetFeature = exploreServices.getClient()
                .search(request).getHits();
        if (hitsGetFeature.getHits().length > 0) {
            return hitsGetFeature.getAt(0).getSourceAsMap();
        } else {
            throw new OGCException(OGCExceptionCode.NOT_FOUND, "Data not found", "resourceid", Service.WFS);
        }

    }

    @Override
    public List<Map<String, Object>> getFeatures(String id, String bbox, String constraint, String resourceid, String partitionFilter, CollectionReference collectionReference, String[] excludes, Integer startindex, Integer count,
                                    Optional<String> columnFilter) throws ArlasException, IOException {

        buildWFSQuery(null, id, bbox, constraint, resourceid, null, partitionFilter, collectionReference, columnFilter);
        List<Map<String, Object>> featureList = new ArrayList<>();
        String[] includes = columnFilterToIncludes(collectionReference, columnFilter);
        SearchRequest request = new SearchRequest(collectionReference.params.indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(includes, excludes)
                .query(wfsQuery)
                .from(startindex)
                .size(count);
        request.source(searchSourceBuilder);
        SearchHits hitsGetFeature = exploreServices.getClient()
                .search(request).getHits();
        for (int i = 0; i < hitsGetFeature.getHits().length; i++) {
            featureList.add(hitsGetFeature.getAt(i).getSourceAsMap());
        }
        return featureList;
    }

    private String[] columnFilterToIncludes(CollectionReference collectionReference, Optional<String> columnFilter) throws ArlasException {
        // return null if no column filter: it avoids the ES query
        // Can't use lambdas because of the need to throw the exception of getCollectionFields()
        Optional<String> cf = ColumnFilterUtil.cleanColumnFilter(columnFilter);
        if (cf.isPresent()) {
            Set<String> fields = exploreServices.getDaoCollectionReference().getCollectionFields(collectionReference, cf);
            return fields.toArray(new String[0]);
        }
        return null;

    }

    @Override
    public ValueCollectionType getPropertyValue(String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter,
                                                CollectionReference collectionReference, String include, String[] excludes, Integer startindex, Integer count,
                                                Optional<String> columnFilter) throws ArlasException, IOException {

        buildWFSQuery(WFSRequestType.GetPropertyValue, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference, columnFilter);
        ValueCollectionType valueCollectionType = new ValueCollectionType();
        SearchRequest request = new SearchRequest(collectionReference.params.indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{include}, excludes)
                .query(wfsQuery)
                .from(startindex)
                .size(count);
        request.source(searchSourceBuilder);
        SearchHits hitsGetPropertyValue = exploreServices.getClient()
                .search(request).getHits();
        for (int i = 0; i < hitsGetPropertyValue.getHits().length; i++) {
            MemberPropertyType e = new MemberPropertyType();
            e.getContent().add(MapExplorer.getObjectFromPath(include, hitsGetPropertyValue.getAt(i).getSourceAsMap()));
            valueCollectionType.getMember().add(e);
        }
        return valueCollectionType;
    }


    private void buildWFSQuery(WFSRequestType requestType, String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter,
                               CollectionReference collectionReference, Optional<String> columnFilter) throws ArlasException, IOException{

        wfsQuery = QueryBuilders.boolQuery();
        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(getCollectionReferenceDescription(collectionReference));
        addCollectionFilter(fluidSearch, collectionReference);
        if (constraint != null) {
            wfsQuery.filter(ElasticFilter.filter(constraint, getCollectionReferenceDescription(collectionReference), Service.WFS, columnFilter));
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
            for (String resourceIdValue : resourceid.split(",")) {
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

    private void addPartitionFilter(FluidSearch fluidSearch, String partitionFilter) throws ArlasException {
        Filter headerFilter = ParamsParser.getFilter(partitionFilter);
        exploreServices.applyFilter(headerFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private void addCollectionFilter(FluidSearch fluidSearch, CollectionReference collectionReference) throws ArlasException {
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
