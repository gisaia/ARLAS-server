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

package io.arlas.server.ogc.wfs.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.ParamsParser;
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
import java.util.Arrays;

import static io.arlas.server.utils.CheckParams.isBboxLatLonInCorrectRanges;

public class WFSQueryBuilder {

    public BoolQueryBuilder wfsQuery = QueryBuilders.boolQuery();
    public Boolean isStoredQuey = false;
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
        this.requestType = requestType;
        this.id = id;
        this.bbox = bbox;
        this.filter = filter;
        this.resourceid = resourceid;
        this.storedquery_id = storedquery_id;
        this.partitionFilter = partitionFilter;
        this.exploreServices = exploreServices;
        this.collectionReferenceDescription = collectionReferenceDescription;


        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(collectionReferenceDescription);
        addCollectionFilter(fluidSearch);
        if (filter != null) {
            buildFilterQuery();
        } else if (bbox != null) {
            buildBboxQuery();
        } else if (resourceid != null) {
            buildRessourceIdQuery();
        } else if (storedquery_id != null) {
            buildStoredQueryIdQuery();
        }
        if (partitionFilter != null) {
            addPartitionFilter(fluidSearch);
        }
    }

    private void buildFilterQuery() throws OGCException, IOException, ParserConfigurationException, SAXException {
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

    private void buildBboxQuery() throws OGCException {
        double[] tlbr = GeoFormat.toDoubles(bbox,Service.WFS);
        if (!(isBboxLatLonInCorrectRanges(tlbr) && tlbr[3] > tlbr[1]) && tlbr[0] != tlbr[2]) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, FluidSearch.INVALID_BBOX, "bbox", Service.WFS);
        }
        wfsQuery.filter(getBBoxBoolQueryBuilder(bbox, collectionReferenceDescription.params.centroidPath));
    }

    private void buildRessourceIdQuery() {
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

    private void buildStoredQueryIdQuery() throws OGCException {
        if (!storedquery_id.equals(WFSConstant.GET_FEATURE_BY_ID_NAME)) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "StoredQuery " + storedquery_id + " not found", "storedquery_id", Service.WFS);
        }
        if (requestType.equals(WFSRequestType.GetFeature)) {
            wfsQuery.filter(QueryBuilders.matchQuery(collectionReferenceDescription.params.idPath, id));
            isStoredQuey = true;
        }
    }

    private void addPartitionFilter(FluidSearch fluidSearch) throws ArlasException, IOException {
        Filter headerFilter = ParamsParser.getFilter(partitionFilter);
        exploreServices.applyFilter(headerFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private void addCollectionFilter(FluidSearch fluidSearch) throws ArlasException, IOException {
        Filter collectionFilter = collectionReferenceDescription.params.filter;
        exploreServices.applyFilter(collectionFilter, fluidSearch);
        wfsQuery.filter(fluidSearch.getBoolQueryBuilder());
    }

    private BoolQueryBuilder getBBoxBoolQueryBuilder(String bbox, String centroidPath) throws OGCException {
        double[] tlbr = GeoFormat.toDoubles(bbox,Service.WFS);
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
