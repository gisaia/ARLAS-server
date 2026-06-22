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

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import io.arlas.server.tests.stac.STACFilterModels.StacFilterScenario;
import io.arlas.server.tests.stac.STACFilterModels.RequestTarget;
import io.arlas.server.tests.stac.STACFilterModels.FilterLang;
import io.arlas.server.tests.stac.STACFilterModels.FilterOperator;
import io.arlas.server.tests.stac.STACFilterModels.FilterClause;

import java.util.List;
import java.util.stream.Stream;

public class STACServiceFullFilterIT extends AbstractSTACServiceTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("fullFilterCases")
    public void testFullFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
    }

    static Stream<Arguments> fullFilterCases() {
        return Stream.of(
                scenarioCasesForGetID(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.EQ, 0),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1000000)),
                        600, 0
                ).withIds(List.of("FAKE_ID"))),
                scenarioCasesForGetID(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.GTE, 0),
                                new FilterClause("params.startdate", FilterOperator.LTE, 1000000)),
                        600, 2

                ).withIds(List.of("ID__170_30DI","ID__160__40DI"))),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Chemist"),
                                new FilterClause("params.age", FilterOperator.EQ, 600),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1050600)),
                        600, 2
                ).withBbox("-50,-50,50,50")),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 0

                ).withDatetime("2018-02-12T00:00:00Z/2018-03-18T12:31:12Z")),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 54

                ).withDatetime("1970-01-01T00:12:43.600Z/1970-01-01T00:14:06.600Z"))
        ).flatMap(s -> s);
    }

    // OGC-Feature Get with ID is not supported by specification, so we only test STAC with ID
    public static Stream<Arguments> scenarioCasesForGetID(StacFilterScenario scenario) {
        return Stream.of(
                Arguments.of(label(scenario, RequestTarget.stacGet(), FilterLang.CQL2_TEXT), scenario, RequestTarget.stacGet(), FilterLang.CQL2_TEXT),
                Arguments.of(label(scenario, RequestTarget.stacGet(), FilterLang.CQL2_JSON), scenario, RequestTarget.stacGet(), FilterLang.CQL2_JSON),
                Arguments.of(label(scenario, RequestTarget.post(), FilterLang.CQL2_TEXT), scenario, RequestTarget.post(), FilterLang.CQL2_TEXT),
                Arguments.of(label(scenario, RequestTarget.post(), FilterLang.CQL2_JSON), scenario, RequestTarget.post(), FilterLang.CQL2_JSON)
        );
    }
}
