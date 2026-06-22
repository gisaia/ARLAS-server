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

package io.arlas.server.tests.stac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.stac.model.SearchBody;
import io.arlas.server.tests.AbstractTestWithCollection;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.provider.Arguments;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.commons.rest.utils.ServerConstants.PARTITION_FILTER;
import static io.restassured.RestAssured.given;
import static io.arlas.server.tests.stac.STACFilterModels.FilterLang;
import static io.arlas.server.tests.stac.STACFilterModels.RequestTarget;
import static io.arlas.server.tests.stac.STACFilterModels.StacFilterScenario;
import static io.arlas.server.tests.stac.STACFilterModels.TypeOfGet;

public class AbstractSTACServiceTest extends AbstractTestWithCollection {

    public static final String COLLECTION = "geodata";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected final STACRequestFactory requestFactory = new STACRequestFactory(COLLECTION);
    protected final STACResponseAssertions responseAssertions = new STACResponseAssertions();

    public final List<String> arlasConformsTo = List.of(
            "https://api.stacspec.org/v1.0.0/core",
            "https://api.stacspec.org/v1.0.0-beta.2/core",
            "https://api.stacspec.org/v1.0.0/item-search",
            "https://api.stacspec.org/v1.0.0/ogcapi-features",
            "https://api.stacspec.org/v1.0.0/collections",
            "https://api.stacspec.org/v1.0.0/item-search#sort",
            "https://api.stacspec.org/v1.0.0/item-search#context",
            "https://api.stacspec.org/v1.0.0/item-search#filter",
            "http://www.opengis.net/spec/ogcapi-features-1/1.0/conf/core",
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/filter",
            "http://www.opengis.net/spec/ogcapi-features-3/1.0/conf/features-filter",
            "http://www.opengis.net/spec/cql2/1.0/conf/cql2-text",
            "http://www.opengis.net/spec/cql2/1.0/conf/cql2-json",
            "http://www.opengis.net/spec/cql2/1.0/conf/basic-cql2",
            "http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-functions",
            "http://www.opengis.net/spec/cql2/1.0/conf/basic-spatial-functions-plus"
    );

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "stac/collections/" + collection + "/items";
    }

    protected String getUrlStacSearchPath() {
        return arlasPath + "stac/search";
    }

    protected String getUrlStacPath() {
        return arlasPath + "stac";
    }

    protected RequestSpecification givenFilterableRequestParams() {
        return given().contentType("application/json;charset=utf-8");
    }

    protected ValidatableResponse get(
            List<Pair<String, String>> params,
            Filter headerFilter,
            String columnFilter,
            TypeOfGet typeOfGet
    ) throws JsonProcessingException {
        RequestSpecification req = givenFilterableRequestParams();

        for (Pair<String, String> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }

        if (headerFilter != null) {
            req = req.header(PARTITION_FILTER, OBJECT_MAPPER.writeValueAsString(headerFilter));
        }

        if (columnFilter != null) {
            req = req.header(COLUMN_FILTER, columnFilter);
        }

        if (typeOfGet == TypeOfGet.OGC_FEATURE) {
            return req.when().get(getUrlPath(COLLECTION)).then();
        }
        return req.when().get(getUrlStacSearchPath()).then();
    }

    protected ValidatableResponse post(
            SearchBody<Object> body,
            Filter headerFilter,
            String columnFilter
    ) throws JsonProcessingException {
        RequestSpecification req = givenFilterableRequestParams();

        if (headerFilter != null) {
            req = req.header(PARTITION_FILTER, OBJECT_MAPPER.writeValueAsString(headerFilter));
        }

        if (columnFilter != null) {
            req = req.header(COLUMN_FILTER, columnFilter);
        }

        return req.body(body).when().post(getUrlStacSearchPath()).then();
    }

    public static Stream<Arguments> scenarioCases(StacFilterScenario scenario) {
        return Stream.of(
                Arguments.of(label(scenario, RequestTarget.stacGet(), FilterLang.CQL2_TEXT), scenario, RequestTarget.stacGet(), FilterLang.CQL2_TEXT),
                Arguments.of(label(scenario, RequestTarget.stacGet(), FilterLang.CQL2_JSON), scenario, RequestTarget.stacGet(), FilterLang.CQL2_JSON),
                Arguments.of(label(scenario, RequestTarget.ogcGet(), FilterLang.CQL2_TEXT), scenario, RequestTarget.ogcGet(), FilterLang.CQL2_TEXT),
                Arguments.of(label(scenario, RequestTarget.ogcGet(), FilterLang.CQL2_JSON), scenario, RequestTarget.ogcGet(), FilterLang.CQL2_JSON),
                Arguments.of(label(scenario, RequestTarget.post(), FilterLang.CQL2_TEXT), scenario, RequestTarget.post(), FilterLang.CQL2_TEXT),
                Arguments.of(label(scenario, RequestTarget.post(), FilterLang.CQL2_JSON), scenario, RequestTarget.post(), FilterLang.CQL2_JSON)
        );
    }

    public static String label(StacFilterScenario scenario, RequestTarget target, FilterLang lang) {
        String filterLabel = scenario.clauses().stream()
                .map(c -> "%s %s %s".formatted(c.property(), c.operator().symbol(), c.value()))
                .reduce((a, b) -> a + " AND " + b)
                .orElse("");

        String extras = "";
        if (scenario.partitionFilter() != null || scenario.columnFilter() != null
                || scenario.ids() != null || scenario.datetime() != null || scenario.bbox() != null) {
            extras = " | partitionFilter=%s | columnFilter=%s | ids=%s | datetime=%s | bbox=%s"
                    .formatted(
                            scenario.partitionFilter() != null ? scenario.partitionFilter().f.toString() : null,
                            scenario.columnFilter(),
                            scenario.ids(),
                            scenario.datetime(),
                            scenario.bbox()
                    );
        }

        return "%s | %s | filter=%s | limit=%d%s"
                .formatted(target, lang.value(), filterLabel, scenario.limit(), extras);
    }

    public ValidatableResponse executeRequest(
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws IOException, InvalidParameterException, ParseException {
        return switch (target.mode()) {
            case GET -> get(
                    requestFactory.buildGetParams(scenario, lang),
                    scenario.partitionFilter(),
                    scenario.columnFilter(),
                    target.typeOfGet()
            );
            case POST -> post(
                    requestFactory.buildSearchBody(scenario, lang),
                    scenario.partitionFilter(),
                    scenario.columnFilter()
            );
        };
    }

    protected void assertCommonResponse(
            ValidatableResponse response,
            StacFilterScenario scenario,
            RequestTarget target
    ) {
        responseAssertions.assertCommonResponse(response, scenario.expectedMatched(), target);
    }

    protected void assertReturnedValues(
            ValidatableResponse response,
            StacFilterScenario scenario
    ) {
        responseAssertions.assertClauses(response, scenario.clauses());
    }
}