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
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.impl.elastic.services.ElasticFluidSearch;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.utils.BoundingBox;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.OpenGISFieldsExtractor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.geotools.filter.v2_0.FESConfiguration;
import org.geotools.xsd.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ElasticFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticFilter.class);

    public static BoolQueryBuilder filter(String[] ids, String idFieldName, String q, String fulltextField, BoundingBox boundingBox, String geometryField) throws IOException {
        BoolQueryBuilder orBoolQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        int minimumShouldMatch = 0;
        if (ids != null && ids.length > 0) {
            for (String resourceIdValue : ids) {
                orBoolQueryBuilder = orBoolQueryBuilder.should(QueryBuilders.matchQuery(idFieldName, resourceIdValue));
            }
            minimumShouldMatch = minimumShouldMatch + 1;
        } else if (!StringUtil.isNullOrEmpty(q)) {
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should((QueryBuilders.simpleQueryStringQuery(q).defaultOperator(Operator.AND).field(fulltextField)));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        if (boundingBox != null) {
            double[] bbox = new double[] { boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast(), boundingBox.getNorth() };
            orBoolQueryBuilder = orBoolQueryBuilder
                    .should(QueryBuilders.geoIntersectionQuery(geometryField, ElasticFluidSearch.createPolygon(bbox)));
            minimumShouldMatch = minimumShouldMatch + 1;
        }
        orBoolQueryBuilder.minimumShouldMatch(minimumShouldMatch);
        boolQuery = boolQuery.filter(orBoolQueryBuilder);
        return boolQuery;
    }

    public static BoolQueryBuilder filter(String constraint, CollectionReferenceDescription collectionDescription, Service service) throws IOException, ArlasException {
        return ElasticFilter.filter(constraint, collectionDescription, service, Optional.empty());
    }

    public static BoolQueryBuilder filter(String constraint, CollectionReferenceDescription collectionDescription, Service service, Optional<String> columnFilter) throws IOException, ArlasException {
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

                if (ColumnFilterUtil.isValidColumnFilterPresent(columnFilter)) {
                    ColumnFilterUtil.assertOpenGisFilterAllowed(columnFilter, collectionDescription, OpenGISFieldsExtractor.extract(openGisFilter));
                }

                filterToElastic.encode(openGisFilter);
                ObjectMapper mapper = new ObjectMapper();
                String filterQuery = mapper.writeValueAsString(filterToElastic.getQueryBuilder());
                // TODO : find a better way to remove prefix xml in field name
                boolQuery.filter(QueryBuilders.wrapperQuery(filterQuery.replace("tns:", "")));
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
