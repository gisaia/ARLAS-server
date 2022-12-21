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

package io.arlas.server.tests.ogc.wfs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.tests.AbstractTestWithCollection;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.commons.rest.utils.ServerConstants.PARTITION_FILTER;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.stringContainsInOrder;

public abstract class AbstractWFSServiceTest extends AbstractTestWithCollection {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "ogc/wfs/" + collection;
    }

    public static void handleOK(ValidatableResponse then) {
        then.statusCode(200);
    }

    public void handleUnavailableColumn(ValidatableResponse then) throws Exception {
        then.statusCode(403).body(stringContainsInOrder(Arrays.asList("column", "available")));
    }

    public void handleUnavailableCollection(ValidatableResponse then) throws Exception {
        then.statusCode(403).body(stringContainsInOrder(Arrays.asList("collection", "available")));
    }

    protected RequestSpecification givenFilterableRequestParams() {
        return given().contentType("application/xml");
    }

    protected ValidatableResponse get(List<Pair<String, String>> params, Filter headerFilter) throws JsonProcessingException {
        return get(params, headerFilter, Optional.empty());
    }

    protected ValidatableResponse get(List<Pair<String, String>> params, Filter headerFilter, Optional<String> columnFilter) throws JsonProcessingException {
        RequestSpecification req = givenFilterableRequestParams().header(PARTITION_FILTER, objectMapper.writeValueAsString(headerFilter));
        for (Pair<String, String> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }
        if (columnFilter.isPresent()) {
            req = req.header(COLUMN_FILTER, columnFilter.get());
        }
        return req
                .when().get(getUrlPath("geodata"))
                .then();
    }

}
