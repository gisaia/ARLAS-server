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

package io.arlas.server.ogc.wfs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.DataSetTool;
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.MultiValueFilter;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WFSServiceIT extends AbstractTestWithCollection {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "ogc/wfs/" + collection;
    }

    @Test
    public void testHeaderFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));
        handleHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")), request.filter)
        );
    }

    @Test
    public void testNoHeaderFilter() throws Exception {
        handleNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")), new Filter())
        );
    }

    @Test
    public void testInspireGetCapabilities() throws Exception {
        handleInspireGetCapabilities(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("REQUEST", "GetCapabilities")), new Filter()));
        handleInspireGetCapabilities(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("REQUEST", "GetCapabilities"),
                new ImmutablePair<>("LANGUAGE", "eng")), new Filter()));
        handleInspireInvalidLanguageGetCapabilities(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("REQUEST", "GetCapabilities"),
                new ImmutablePair<>("LANGUAGE", "english")), new Filter()));
    }

    @Test
    public void testDescribeFeature() throws Exception {
        handleDescribeFeature(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")), new Filter())
        );
    }

    public void handleHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("wfs:FeatureCollection.@numberReturned", equalTo("2"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "job", equalTo("Architect"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "country.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "city.size()", equalTo(0));
    }

    public void handleNoHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("wfs:FeatureCollection.@numberReturned", equalTo("595"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "job", isOneOf(DataSetTool.jobs))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "country.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "city.size()", equalTo(0));
    }

    public void handleInspireGetCapabilities(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("ns5:WFS_Capabilities.ns1:OperationsMetadata.ns1:ExtendedCapabilities.ns4:ExtendedCapabilities", notNullValue());
    }

    public void handleInspireInvalidLanguageGetCapabilities(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    public void handleDescribeFeature(ValidatableResponse then) throws Exception {
        if(!DataSetTool.ALIASED_COLLECTION) {
            then.statusCode(200)
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(8));
        } else {
            then.statusCode(200)
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(10));
        }
    }

    protected RequestSpecification givenFilterableRequestParams() {
        return given().contentType("application/xml");
    }

    private ValidatableResponse get(List<Pair<String, String>> params, Filter headerFilter) throws JsonProcessingException {
        RequestSpecification req = givenFilterableRequestParams().header("Partition-Filter", objectMapper.writeValueAsString(headerFilter));
        for (Pair<String, String> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }
        return req
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
