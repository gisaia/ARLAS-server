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

package io.arlas.server.ogc.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.requestfilter.FilterToElastic;
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

public class OGCQueryBuilder {
    protected String filter;
    public BoolQueryBuilder ogcQuery = QueryBuilders.boolQuery();
    public Boolean isConfigurationQuery = false;
    public Service service;


    protected void buildFilterQuery(CollectionReferenceDescription collectionReferenceDescription) throws OGCException, IOException, ParserConfigurationException, SAXException {
        FESConfiguration configuration = new FESConfiguration();
        Parser parser = new Parser(configuration);
        if (filter.contains("srsName=\"urn:ogc:def:crs:EPSG::4326\"")) {
            // TODO : find a better way to replace EPSG for test suite
            filter = filter.replace("srsName=\"urn:ogc:def:crs:EPSG::4326\"", "srsName=\"http://www.opengis.net/def/crs/epsg/0/4326\"");
        }
        FilterToElastic filterToElastic = new FilterToElastic(collectionReferenceDescription, service);
        try {
            InputStream stream = new ByteArrayInputStream(filter.getBytes(StandardCharsets.UTF_8));
            org.opengis.filter.Filter openGisFilter = (org.opengis.filter.Filter) parser.parse(stream);

            filterToElastic.encode(openGisFilter);
            ObjectMapper mapper = new ObjectMapper();
            String filterQuery = mapper.writeValueAsString(filterToElastic.getQueryBuilder());
            // TODO : find a better way to remove prefix xml in field name
            ogcQuery.filter(QueryBuilders.wrapperQuery(filterQuery.replace("tns:", "")));
            isConfigurationQuery = filterToElastic.isConfigurationQuery;
        } catch (RuntimeException e) {
            throw filterToElastic.ogcException;
        }
    }
}
