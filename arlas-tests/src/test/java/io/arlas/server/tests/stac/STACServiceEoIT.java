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

import java.util.stream.Stream;

public class STACServiceEoIT extends AbstractSTACServiceEOTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterEoCloudCoverCases")
    public void testEoCloudCoverFilter(
            String label,
            STACFilterModels.StacFilterScenario scenario,
            STACFilterModels.RequestTarget target,
            STACFilterModels.FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario,target);
        assertReturnedValues(response, scenario,true);
    }
    static Stream<Arguments> filterEoCloudCoverCases() {
        return Stream.of("eo:cloud_cover","properties.eo:cloud_cover").flatMap(property -> Stream.of(
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.EQ, 83.309135F, 100, 1)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.NE, 83.309135F, 100, 59)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.GT, 50F, 100, 29)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.GTE, 50F, 100, 29)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.GTE, 0F, 100, 60)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.LT, 50F, 100, 31)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.LTE, 50F, 100, 31)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple(property, STACFilterModels.FilterOperator.BETWEEN, new STACFilterModels.BetweenValue(0,25), 100, 16))
        )).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("filterGeometryCases")
    public void testGeometryFilter(
            String label,
            STACFilterModels.StacFilterScenario scenario,
            STACFilterModels.RequestTarget target,
            STACFilterModels.FilterLang lang
    ) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario,target);
        assertReturnedValues(response, scenario,true);
    }
    static Stream<Arguments> filterGeometryCases() {
        return Stream.of(
                scenarioCases(STACFilterModels.StacFilterScenario.simple("geometry", STACFilterModels.FilterOperator.ST_WITHIN, "POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", 100, 0)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple("geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, "POLYGON((-20 -20, 20 -20, 20 20, -20 20, -20 -20))", 100, 4)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple("geometry", STACFilterModels.FilterOperator.BBOX, new STACFilterModels.BboxValue(-20,-20,20,20), 100, 4)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple("geometry", STACFilterModels.FilterOperator.BBOX, new STACFilterModels.BboxValue(-50,-50,50,50), 100, 25)),
                scenarioCases(STACFilterModels.StacFilterScenario.simple("geometry", STACFilterModels.FilterOperator.BBOX, new STACFilterModels.BboxValue(-20,-20,20,20), 100, 4).withBbox("-50,-50,50,50"))
                ).flatMap(s -> s);
    }
}
