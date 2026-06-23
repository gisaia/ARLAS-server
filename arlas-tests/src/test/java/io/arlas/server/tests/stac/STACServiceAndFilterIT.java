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

public class STACServiceAndFilterIT extends AbstractSTACServiceTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("andFilterCases")
    public void testAndFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
    }

    static Stream<Arguments> andFilterCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.EQ, 0),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1000000)),
                        600, 1
                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.GTE, 0),
                                new FilterClause("params.startdate", FilterOperator.LTE, 1000000)),
                        600, 31

                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Chemist"),
                                new FilterClause("params.age", FilterOperator.EQ, 600),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1050600)),
                        600, 2
                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Coder"),
                                new FilterClause("params.age", FilterOperator.EQ, 1000),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1071000)),
                        600, 2
                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Brain Scientist"),
                                new FilterClause("params.age", FilterOperator.EQ, 400),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1040000)),
                        600, 1
                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.NE, "Actor"),
                                new FilterClause("params.age", FilterOperator.LT, 7000),
                                new FilterClause("params.startdate", FilterOperator.GT, 950000)),
                        600, 291

                )),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.GTE, "Chemist"),
                                new FilterClause("params.age", FilterOperator.LTE, 2600),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1020000)),
                        600, 70

                )),

                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 424

                ))
        ).flatMap(s -> s);
    }
}
