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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.GeoShapeRelation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.impl.elastic.services.ElasticFluidSearch;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.utils.BoundingBox;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.OpenGISFieldsExtractor;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.geotools.filter.v2_0.FESConfiguration;
import org.geotools.xsd.Parser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElasticFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFilter.class);

    public static BoolQuery.Builder filter(String[] ids, String idFieldName, String q, String fulltextField, BoundingBox boundingBox, String geometryField) throws IOException {


        BoolQuery.Builder orBoolQueryBuilder = new BoolQuery.Builder();
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        int minimumShouldMatch = 0;
        if (ids != null && ids.length > 0) {
            for (String resourceIdValue : ids) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(MatchQuery
                        .of(builder -> builder.field(idFieldName)
                                .query(FieldValue.of(resourceIdValue)))
                        ._toQuery());
            }
            minimumShouldMatch = minimumShouldMatch + 1;
        } else if (!StringUtil.isNullOrEmpty(q)) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(SimpleQueryStringQuery.of(builder -> builder.query(q).defaultOperator(Operator.And).fields(fulltextField))._toQuery());
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        if (boundingBox != null) {
            double west = boundingBox.getWest();
            double east =  boundingBox.getEast();
            double south = boundingBox.getSouth();
            double north = boundingBox.getNorth();
            /** In ARLAS-api west and east are necessarily between -180 and 180**/
            /** In case of west > east, it means the bbox crosses the dateline (antiméridien) => a translation of west or east by 360 is necessary to be
             * correctly interpreted by geoWithinQuery and geoIntersectsQuery*/
            if (west > east) {
                if (west >= 0) {
                    west -= 360;
                } else {
                    /** east is necessarily < 0 */
                    east += 360;
                }
            }
            Polygon polygonGeometry = new Polygon();
            List<LngLatAlt> exteriorRing = new ArrayList<>();
            exteriorRing.add(new LngLatAlt(east, south));
            exteriorRing.add(new LngLatAlt(east, north));
            exteriorRing.add(new LngLatAlt(west, north));
            exteriorRing.add(new LngLatAlt(west, south));
            exteriorRing.add(new LngLatAlt(east, south));
            polygonGeometry.setExteriorRing(exteriorRing);
            JSONObject polygon = new JSONObject();
            JSONArray jsonArayExt = new JSONArray();
            polygonGeometry.getExteriorRing().forEach(lngLatAlt -> {
                JSONArray jsonArayLngLat = new JSONArray();
                jsonArayLngLat.add(0, lngLatAlt.getLongitude());
                jsonArayLngLat.add(1, lngLatAlt.getLatitude());
                jsonArayExt.add(jsonArayLngLat);
            });
            JSONArray jsonAray = new JSONArray();
            jsonAray.add(jsonArayExt);
            polygon.put("type", "Polygon");
            polygon.put("coordinates", jsonAray);

            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoShape()
                            .field(geometryField)
                            .shape(s -> s
                                    .relation(GeoShapeRelation.Intersects)
                                    .shape(JsonData.of(polygon))
                            ).build()._toQuery());
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        orBoolQueryBuilder.minimumShouldMatch(String.valueOf(minimumShouldMatch));
        boolQuery = boolQuery.filter(orBoolQueryBuilder.build()._toQuery());
        return boolQuery;
    }

    public static BoolQuery.Builder filter(String constraint, CollectionReferenceDescription collectionDescription, Service service) throws IOException, ArlasException {
        return ElasticFilter.filter(constraint, collectionDescription, service, Optional.empty());
    }

    public static BoolQuery.Builder filter(String constraint, CollectionReferenceDescription collectionDescription, Service service, Optional<String> columnFilter) throws IOException, ArlasException {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
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

                if (ColumnFilterUtil.isValidColumnFilterPresent(columnFilter)) {
                    ColumnFilterUtil.assertOpenGisFilterAllowed(columnFilter, collectionDescription, OpenGISFieldsExtractor.extract(openGisFilter));
                }

                filterToElastic.encode(openGisFilter);
                ObjectMapper mapper = new ObjectMapper();
                String filterQuery = mapper.writeValueAsString(filterToElastic.getQueryBuilder());
                // TODO : find a better way to remove prefix xml in field name
                boolQuery.filter(WrapperQuery.of(builder -> builder.query(filterQuery.replace("tns:", "")))._toQuery());
            } catch (SAXException | ParserConfigurationException | RuntimeException e) {
                if (filterToElastic.ogcException != null) {
                    LOGGER.debug(filterToElastic.ogcException.getMessage());
                    throw filterToElastic.ogcException;
                } else {
                    LOGGER.debug(e.getMessage());
                    throw OGCException.getInternalServerException(e, service, "filter");
                }
            }
        }
        return boolQuery;
    }

}
