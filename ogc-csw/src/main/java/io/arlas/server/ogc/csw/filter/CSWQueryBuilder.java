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

package io.arlas.server.ogc.csw.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.requestfilter.FilterToElastic;
import io.arlas.server.ogc.common.utils.OGCQueryBuilder;
import io.arlas.server.utils.BoundingBox;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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

public class CSWQueryBuilder extends OGCQueryBuilder {
    private BoundingBox bbox;
    private String[] ids;
    private String query;
    private CollectionReferenceDescription metaCollectionDescription;
    public List<CollectionReference> collections = new ArrayList<>();

    public CSWQueryBuilder(String[] ids, String query, BoundingBox bbox, String constraint, CollectionReferenceDescription metaCollectionDescription)
            throws ArlasException, IOException, ParserConfigurationException, SAXException  {
        this.ids = ids;
        this.query = query;
        this.bbox = bbox;
        this.filter = constraint;
        this.metaCollectionDescription = metaCollectionDescription;

        buildCollectionQuery(ids,query,bbox);
        if (filter != null) {
            buildFilterQuery(metaCollectionDescription);
        }
    }

    private void buildCollectionQuery(String[] ids, String q, BoundingBox boundingBox) throws IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        int minimumShouldMatch = 0;
        if (ids != null && ids.length > 0) {
            for (String resourceIdValue : Arrays.asList(ids)) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery("dublin_core_element_name.identifier", resourceIdValue));
            }
            minimumShouldMatch = minimumShouldMatch + 1;
        } else if (q != null && q.length() > 0) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should((QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND).field("internal.fulltext")));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        if (boundingBox != null) {
            CoordinatesBuilder coordinatesBuilder = new CoordinatesBuilder();
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getSouth());
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getNorth());
            coordinatesBuilder.coordinate(boundingBox.getWest(), boundingBox.getNorth());
            coordinatesBuilder.coordinate(boundingBox.getWest(), boundingBox.getSouth());
            coordinatesBuilder.coordinate(boundingBox.getEast(), boundingBox.getSouth());
            PolygonBuilder polygonBuilder = new PolygonBuilder(coordinatesBuilder, ShapeBuilder.Orientation.LEFT);
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoIntersectionQuery("dublin_core_element_name.coverage", polygonBuilder));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        orBoolQueryBuilder.minimumShouldMatch(minimumShouldMatch);
        ogcQuery.filter(orBoolQueryBuilder);
    }
 }
