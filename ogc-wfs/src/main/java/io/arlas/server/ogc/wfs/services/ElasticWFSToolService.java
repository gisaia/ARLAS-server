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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoBounds;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoBoundingBoxQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.GeoShapeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.core.impl.elastic.services.ElasticFluidSearch;
import io.arlas.server.core.impl.elastic.services.ElasticExploreService;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.ElasticFilter;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.MapExplorer;
import io.arlas.server.core.utils.ParamsParser;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ValueCollectionType;
import java.io.IOException;
import java.util.*;

import static io.arlas.server.core.utils.CheckParams.isBboxLatLonInCorrectRanges;

public class ElasticWFSToolService implements WFSToolService {
    ElasticExploreService exploreServices;
    BoolQuery.Builder wfsQuery = new BoolQuery.Builder();


    public ElasticWFSToolService(ElasticExploreService exploreServices) {
        this.exploreServices = exploreServices;
    }

    @Override
    public CollectionReferenceDescription getCollectionReferenceDescription(CollectionReference collectionReference) throws ArlasException {
        return exploreServices.getCollectionReferenceService().describeCollection(collectionReference);
    }

    @Override
    public Map<String, Object> getFeature(String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter, CollectionReference collectionReference, String[] excludes, Optional<String> columnFilter) throws ArlasException, IOException {
        buildWFSQuery(WFSRequestType.GetFeature, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference, columnFilter);
        String[] includes = columnFilterToIncludes(collectionReference, columnFilter);
        SourceFilter.Builder sourceFilterBuilder = new SourceFilter.Builder().excludes(Arrays.asList(excludes));
        if(includes != null){
            sourceFilterBuilder=sourceFilterBuilder.includes(Arrays.asList(includes));
        }
        SourceFilter sourceFilter = sourceFilterBuilder.build();
        SourceConfig sourceConfig = new SourceConfig.Builder().filter(sourceFilter).build();
        SearchRequest request = SearchRequest.of(r -> r
                .index(collectionReference.params.indexName)
                .source(sourceConfig)
                .query(wfsQuery.build()._toQuery()));


        HitsMetadata<Map> hitsGetFeature = exploreServices.getClient()
                .search(request).hits();
        if (hitsGetFeature.hits().size() > 0) {
            return hitsGetFeature.hits().get(0).source();
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
        SourceFilter.Builder sourceFilterBuilder = new SourceFilter.Builder().excludes(Arrays.asList(excludes));
        if(includes != null){
            sourceFilterBuilder=sourceFilterBuilder.includes(Arrays.asList(includes));
        }
        SourceFilter sourceFilter = sourceFilterBuilder.build();
        SourceConfig sourceConfig = new SourceConfig.Builder().filter(sourceFilter).build();
        SearchRequest request = SearchRequest.of(r -> r
                .index(collectionReference.params.indexName)
                .source(sourceConfig)
                .from(startindex)
                .size(count)
                .query(wfsQuery.build()._toQuery()));


        HitsMetadata<Map> hitsGetFeature = exploreServices.getClient()
                .search(request).hits();
        for (int i = 0; i < hitsGetFeature.hits().size(); i++) {
            featureList.add(hitsGetFeature.hits().get(i).source());
        }
        return featureList;
    }

    private String[] columnFilterToIncludes(CollectionReference collectionReference, Optional<String> columnFilter) throws ArlasException {
        // return null if no column filter: it avoids the ES query
        // Can't use lambdas because of the need to throw the exception of getCollectionFields()
        Optional<String> cf = ColumnFilterUtil.cleanColumnFilter(columnFilter);
        if (cf.isPresent()) {
            Set<String> fields = exploreServices.getCollectionReferenceService().getCollectionFields(collectionReference, cf);
            return fields.toArray(new String[0]);
        }
        return null;

    }

    @Override
    public ValueCollectionType getPropertyValue(String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter,
                                                CollectionReference collectionReference, String includes, String[] excludes, Integer startindex, Integer count,
                                                Optional<String> columnFilter) throws ArlasException, IOException {

        buildWFSQuery(WFSRequestType.GetPropertyValue, id, bbox, constraint, resourceid, storedquery_id, partitionFilter, collectionReference, columnFilter);
        ValueCollectionType valueCollectionType = new ValueCollectionType();

        SourceFilter sourceFilter = new SourceFilter.Builder().excludes(Arrays.asList(excludes)).includes(Arrays.asList(includes)).build();
        SourceConfig sourceConfig = new SourceConfig.Builder().filter(sourceFilter).build();
        SearchRequest request = SearchRequest.of(r -> r
                .index(collectionReference.params.indexName)
                .source(sourceConfig)
                .from(startindex)
                .size(count)
                .query(wfsQuery.build()._toQuery()));


        HitsMetadata<Map> hitsGetPropertyValue = exploreServices.getClient()
                .search(request).hits();
        for (int i = 0; i < hitsGetPropertyValue.hits().size(); i++) {
            MemberPropertyType e = new MemberPropertyType();
            e.getContent().add(MapExplorer.getObjectFromPath(includes, hitsGetPropertyValue.hits().get(i).source()));
            valueCollectionType.getMember().add(e);
        }
        return valueCollectionType;
    }


    private void buildWFSQuery(WFSRequestType requestType, String id, String bbox, String constraint, String resourceid, String storedquery_id, String partitionFilter,
                               CollectionReference collectionReference, Optional<String> columnFilter) throws ArlasException, IOException{

        wfsQuery = new BoolQuery.Builder();
        ElasticFluidSearch fluidSearch = (ElasticFluidSearch) exploreServices.getFluidSearch(getCollectionReferenceDescription(collectionReference));
        if (constraint != null) {
            wfsQuery.filter(ElasticFilter.filter(constraint, getCollectionReferenceDescription(collectionReference), Service.WFS, columnFilter).build()._toQuery());
        } else if (bbox != null) {
            buildBboxQuery(bbox, collectionReference);
        } else if (resourceid != null) {
            buildRessourceIdQuery(resourceid, collectionReference);
        } else if (storedquery_id != null) {
            buildStoredQueryIdQuery(id, storedquery_id, requestType, collectionReference);
        }
        // Hack because Object builders can only be used once
        if (partitionFilter != null) {
            addPartitionFilter(collectionReference, fluidSearch, partitionFilter);
        }else{
            addCollectionFilter(fluidSearch, collectionReference);
        }
    }

    private void buildBboxQuery(String bbox, CollectionReference collectionReference) throws OGCException {
        double[] tlbr = GeoFormat.toDoubles(bbox,Service.WFS);
        if (!(isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, ElasticFluidSearch.INVALID_BBOX, "bbox", Service.WFS);
        }
        wfsQuery.filter(getBBoxBoolQueryBuilder(bbox, collectionReference.params.centroidPath).build()._toQuery());
    }

    private void buildRessourceIdQuery(String resourceid,  CollectionReference collectionReference) {
        if (resourceid.contains(",")) {
            BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder();
            for (String resourceIdValue : resourceid.split(",")) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(MatchQuery.of(builder -> builder.query(FieldValue.of(resourceIdValue)).field(collectionReference.params.idPath))._toQuery());
            }
            wfsQuery = wfsQuery.filter(orBoolQueryBuilder.build()._toQuery());
        } else {
            wfsQuery.filter(MatchQuery.of(builder -> builder.query(FieldValue.of(resourceid)).field(collectionReference.params.idPath))._toQuery());
        }
    }

    private void buildStoredQueryIdQuery(String id, String storedquery_id, WFSRequestType requestType, CollectionReference collectionReference) throws OGCException {
        if (!storedquery_id.equals(WFSConstant.GET_FEATURE_BY_ID_NAME)) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "StoredQuery " + storedquery_id + " not found", "storedquery_id", Service.WFS);
        }
        if (requestType !=null && requestType.equals(WFSRequestType.GetFeature)) {
            if (id != null) {
                wfsQuery.filter(MatchQuery.of(builder -> builder.query(FieldValue.of(id)).field(collectionReference.params.idPath))._toQuery());

            } else {
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, "'id' parameter is missing for the StoredQuery : " + storedquery_id, "storedquery_id", Service.WFS);
            }
        }
    }

    private void addPartitionFilter(CollectionReference collectionReference, ElasticFluidSearch fluidSearch, String partitionFilter) throws ArlasException {
        Filter headerFilter = ParamsParser.getFilter(collectionReference, partitionFilter);
        Filter collectionFilter = collectionReference.params.filter;
        Boolean applyFilter = false;
        if(collectionFilter != null){
            exploreServices.applyFilter(collectionFilter, fluidSearch);
            applyFilter = true;
        }
        if(headerFilter.f != null || headerFilter.q != null){
            exploreServices.applyFilter(headerFilter, fluidSearch);
            applyFilter = true;
        }
        if(applyFilter){
            wfsQuery.filter(fluidSearch.getBoolQueryBuilder().build()._toQuery());
        }
    }

    private void addCollectionFilter(ElasticFluidSearch fluidSearch, CollectionReference collectionReference) throws ArlasException {
        Filter collectionFilter = collectionReference.params.filter;
        exploreServices.applyFilter(collectionFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder().build()._toQuery());
    }

    private BoolQuery.Builder getBBoxBoolQueryBuilder(String bbox, String centroidPath) throws OGCException {
        // west, south, east, north
        double[] tlbr = GeoFormat.toDoubles(bbox, Service.WFS);
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        GeoBounds geobounds = GeoBounds.of(builder ->
                builder.tlbr(builder1 ->
                        builder1.topLeft(
                                        builder2 -> builder2.latlon(builder3 -> builder3.lat(tlbr[3]).lon(tlbr[0])))
                                .bottomRight(builder2 -> builder2.latlon(builder3 -> builder3.lat(tlbr[1]).lon(tlbr[2])))
                ));
        BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder();
        orBoolQueryBuilder = orBoolQueryBuilder.should(GeoBoundingBoxQuery.of(builder -> builder.boundingBox(geobounds).field(centroidPath))._toQuery());
        orBoolQueryBuilder = orBoolQueryBuilder.minimumShouldMatch("1");
        boolQueryBuilder = boolQueryBuilder.filter(orBoolQueryBuilder.build()._toQuery());
        return boolQueryBuilder;
    }

}
