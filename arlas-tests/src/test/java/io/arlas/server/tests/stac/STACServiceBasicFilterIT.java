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
import io.arlas.server.tests.stac.STACFilterModels.BetweenValue;
import java.util.stream.Stream;

public class STACServiceBasicFilterIT extends AbstractSTACServiceTest {


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterStringCases")
    public void testStringBasicFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario,target);
        assertReturnedValues(response, scenario);
    }

    static Stream<Arguments> filterStringCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.EQ, "Actor", 60, 59)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.NE, "Actor", 60, 536)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.GT, "Actor", 600, 536)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.GTE, "Actor", 600, 595)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.LT, "Actor", 60, 0)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.LTE, "Actor", 60, 59)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.LIKE, "cto", 60, 59)),
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.BETWEEN, new BetweenValue("Architect","Dancer"), 600, 420))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterErrorCases")
    public void testLikeErrorFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        response.assertThat().statusCode(400);

    }
    static Stream<Arguments> filterErrorCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.LIKE, "%cto", 60, 59))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterLikeEscapeCases")
    public void testLikeEscapeFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        response.assertThat().statusCode(200);

    }
    static Stream<Arguments> filterLikeEscapeCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("params.job", FilterOperator.LIKE, "\\%cto", 60, 59))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterNumberCases")
    public void testNumberFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
    }

    static Stream<Arguments> filterNumberCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.EQ, 13600, 5, 4)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.NE, 13600, 600, 591)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.GT, 13600, 5, 0)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.GTE, 13600, 5, 4)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.LT, 13600, 600, 591)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.LTE, 13600, 600, 595)),
                scenarioCases(StacFilterScenario.simple("params.age", FilterOperator.BETWEEN, new BetweenValue(10000, 50000), 600, 36))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterDateCases")
    public void testDateBasicFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);

    }

    static Stream<Arguments> filterDateCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.EQ, 1000000, 600, 1)),
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.NE, 1000000, 600, 594)),
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.GT, 1000000, 600, 289)),
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.GTE, 1000000, 600, 290)),
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.LT, 1000000, 600, 305)),
                scenarioCases(StacFilterScenario.simple("params.startdate", FilterOperator.LTE, 1000000, 600, 306))
        ).flatMap(s -> s);
    }
}
