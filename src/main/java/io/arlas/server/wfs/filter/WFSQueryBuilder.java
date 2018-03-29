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

package io.arlas.server.wfs.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.ParamsParser;
import io.arlas.server.wfs.utils.WFSCheckParam;
import io.arlas.server.wfs.utils.WFSConstant;
import io.arlas.server.wfs.utils.WFSRequestType;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.geotools.filter.v2_0.FESConfiguration;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WFSQueryBuilder {

    public BoolQueryBuilder wfsQuery = QueryBuilders.boolQuery();
    public Boolean isStoredQuey = false;
    public String[] includeFields;
    public String[] excludeFields;

    private List<String> include = new ArrayList<>();
    private List<String> exclude = new ArrayList<>();
    private WFSRequestType requestType;
    private String id;
    private String bbox;
    private String filter;
    private String resourceid;
    private String storedquery_id;
    private String partitionFilter;
    private ExploreServices exploreServices;
    private CollectionReferenceDescription collectionReferenceDescription;

    public WFSQueryBuilder(WFSRequestType requestType,
                           String id,
                           String bbox,
                           String filter,
                           String resourceid,
                           String storedquery_id,
                           CollectionReferenceDescription collectionReferenceDescription,
                           String partitionFilter,
                           ExploreServices exploreServices)
            throws ArlasException, IOException, ParserConfigurationException, SAXException {
        this.requestType=requestType;
        this.id=id;
        this.bbox=bbox;
        this.filter =filter;
        this.resourceid=resourceid;
        this.storedquery_id=storedquery_id;
        this.partitionFilter=partitionFilter;
        this.exploreServices=exploreServices;
        this.collectionReferenceDescription=collectionReferenceDescription;

        if (filter != null) {
            buildFilterQuery();
        } else if (bbox != null) {
            buildBboxQuery();
        } else if (resourceid != null) {
            buildRessourceIdQuery();
        } else if (storedquery_id != null) {
            buildStoredQueryIdQuery();
        }
        if(partitionFilter!=null){
            addPartitionFilter();
        }

        //apply include and exclude filters
        if (collectionReferenceDescription.params.includeFields != null && !collectionReferenceDescription.params.includeFields.isEmpty()) {
            include(collectionReferenceDescription.params.includeFields);
        }
        if (collectionReferenceDescription.params.excludeFields != null && !collectionReferenceDescription.params.excludeFields.isEmpty()) {
            exclude(collectionReferenceDescription.params.excludeFields);
        }
        if (collectionReferenceDescription.params.excludeWfsFields != null && !collectionReferenceDescription.params.excludeWfsFields.isEmpty()) {
            exclude(collectionReferenceDescription.params.excludeWfsFields);
        }
        List<String> includeFieldList = new ArrayList<>();
        if(!include.isEmpty()) {
            for (String includeField : include) {
                includeFieldList.add(includeField);
            }
            for (String path : getCollectionPaths()) {
                boolean alreadyIncluded = false;
                for (String includeField : include) {
                    if (includeField.equals("*") || path.startsWith(includeField)) {
                        alreadyIncluded = true;
                    }
                }
                if (!alreadyIncluded) {
                    includeFieldList.add(path);
                }
            }
        }
        includeFields = includeFieldList.toArray(new String[includeFieldList.size()]);
        if(includeFields.length == 0) {
            includeFields = new String[]{"*"};
        }
        excludeFields = exclude.toArray(new String[exclude.size()]);
        if(excludeFields.length == 0) {
            excludeFields = null;
        }
    }

    private void buildFilterQuery() throws WFSException, IOException, ParserConfigurationException, SAXException {
        FESConfiguration configuration = new FESConfiguration();
        Parser parser = new Parser(configuration);
        if (filter.contains("srsName=\"urn:ogc:def:crs:EPSG::4326\"")) {
            // TODO : find a better way to replace EPSG for test suite
            filter = filter.replace("srsName=\"urn:ogc:def:crs:EPSG::4326\"", "srsName=\"http://www.opengis.net/def/crs/epsg/0/4326\"");
        }
        FilterToElastic filterToElastic = new FilterToElastic(collectionReferenceDescription);
        try {
            InputStream stream = new ByteArrayInputStream(filter.getBytes(StandardCharsets.UTF_8));
            org.opengis.filter.Filter openGisFilter = (org.opengis.filter.Filter) parser.parse(stream);
            filterToElastic.encode(openGisFilter);
            ObjectMapper mapper = new ObjectMapper();
            String filterQuery = mapper.writeValueAsString(filterToElastic.getQueryBuilder());
            // TODO : find a better way to remove prefix xml in field name
            wfsQuery.filter(QueryBuilders.wrapperQuery(filterQuery.replace("tns:", "")));
        } catch (RuntimeException e) {
            throw filterToElastic.wfsException;
        }
    }

    private void buildBboxQuery() throws WFSException {
        // valid bbox from WFS OGC SPEC = lower longitude , lower latitude , upper longitude  , upper latitude
        // valid bbox for ARLAS classic bbox = lat top,  long left,  lat bottom,  long right
        double[] tlbr = toDoubles(bbox);
        if (!(WFSCheckParam.isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, FluidSearch.INVALID_BBOX, "bbox");
        }
        wfsQuery.filter(getBBoxBoolQueryBuilder(bbox, collectionReferenceDescription.params.centroidPath));
    }

    private void buildRessourceIdQuery(){
        if (resourceid.contains(",")) {
            BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String resourceIdValue : Arrays.asList(resourceid.split(","))) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(collectionReferenceDescription.params.idPath, resourceIdValue));
            }
            wfsQuery = wfsQuery.filter(orBoolQueryBuilder);
        } else {
            wfsQuery.filter(QueryBuilders.matchQuery(collectionReferenceDescription.params.idPath, resourceid));
        }
    }

    private void buildStoredQueryIdQuery() throws WFSException {
        if (!storedquery_id.equals(WFSConstant.GET_FEATURE_BY_ID_NAME)) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "StoredQuery " + storedquery_id + " not found", "storedquery_id");
        }
        if (requestType.equals(WFSRequestType.GetFeature)) {
            wfsQuery.filter(QueryBuilders.matchQuery(collectionReferenceDescription.params.idPath, id));
            isStoredQuey = true;
        }
    }

    private void addPartitionFilter() throws ArlasException, IOException {
        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(collectionReferenceDescription);
        Filter headerFilter = ParamsParser.getFilter(partitionFilter);
        exploreServices.applyFilter(headerFilter,fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private BoolQueryBuilder getBBoxBoolQueryBuilder(String bbox, String centroidPath) throws WFSException {
        double[] tlbr = toDoubles(bbox);
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

    private double[] toDoubles(String bbox) throws WFSException {
        try {
            return Arrays.stream(bbox.split(",")).limit(4).mapToDouble(Double::parseDouble).toArray();
        } catch (Exception e) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, FluidSearch.INVALID_BBOX, "BBOX");
        }
    }

    private List<String> getCollectionPaths() {
        return Arrays.asList(collectionReferenceDescription.params.idPath,
                collectionReferenceDescription.params.geometryPath,
                collectionReferenceDescription.params.centroidPath,
                collectionReferenceDescription.params.timestampPath);
    }

    private void include(String include) {
        if (include != null) {
            String includeFieldArray[] = include.split(",");
            for(String field : includeFieldArray) {
                this.include.add(field);
            }
        }
    }

    private void exclude(String exclude) {
        if (exclude != null) {
            String excludeFieldArray[] = exclude.split(",");
            for(String field : excludeFieldArray) {
                this.exclude.add(field);
            }
        }
    }
}
