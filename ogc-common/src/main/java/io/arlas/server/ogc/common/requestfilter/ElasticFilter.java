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

package io.arlas.server.ogc.common.requestfilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.utils.BoundingBox;
import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.geotools.filter.v2_0.FESConfiguration;
import org.geotools.xsd.Parser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ElasticFilter {

    public static BoolQueryBuilder filter(String[] ids, String idFieldName, String q, String fulltextField, BoundingBox boundingBox, String geometryField) throws IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        int minimumShouldMatch = 0;
        if (ids != null && ids.length > 0) {
            for (String resourceIdValue : Arrays.asList(ids)) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(idFieldName, resourceIdValue));
            }
            minimumShouldMatch = minimumShouldMatch + 1;
        } else if (q != null && q.length() > 0) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should((QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND).field(fulltextField)));
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
                    .should(QueryBuilders.geoIntersectionQuery(geometryField, polygonBuilder));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        orBoolQueryBuilder.minimumShouldMatch(minimumShouldMatch);
        boolQuery = boolQuery.filter(orBoolQueryBuilder);
        return boolQuery;
    }

    public static BoolQueryBuilder filter(String constraint, CollectionReferenceDescription collectionDescription, Service service) throws IOException, ArlasException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        FESConfiguration configuration = new FESConfiguration();
        Parser parser = new Parser(configuration);
        if (constraint != null) {
            if (constraint.contains("srsName=\"urn:ogc:def:crs:EPSG::4326\"")) {
                // TODO : find a better way to replace EPSG for test suite
                constraint = constraint.replace("srsName=\"urn:ogc:def:crs:EPSG::4326\"", "srsName=\"http://www.opengis.net/def/crs/epsg/0/4326\"");
            }
            FilterToElastic filterToElastic = new FilterToElastic(collectionDescription, service);
            try {
                InputStream stream = new ByteArrayInputStream(constraint.getBytes(StandardCharsets.UTF_8));
                org.opengis.filter.Filter openGisFilter = (org.opengis.filter.Filter) parser.parse(stream);

                filterToElastic.encode(openGisFilter);
                ObjectMapper mapper = new ObjectMapper();
                String filterQuery = mapper.writeValueAsString(filterToElastic.getQueryBuilder());
                // TODO : find a better way to remove prefix xml in field name
                boolQuery.filter(QueryBuilders.wrapperQuery(filterQuery.replace("tns:", "")));
            } catch (SAXException e) {
                throw filterToElastic.ogcException;
            } catch (ParserConfigurationException e) {
                throw filterToElastic.ogcException;
            } catch (RuntimeException e) {
                throw filterToElastic.ogcException;
            }
        }
        return boolQuery;
    }
}
