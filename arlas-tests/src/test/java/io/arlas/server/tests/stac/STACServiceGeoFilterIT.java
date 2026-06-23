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
import io.arlas.server.tests.stac.STACFilterModels.StacFilterScenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.stream.Stream;

public class STACServiceGeoFilterIT extends  AbstractSTACServiceTest {

    public final static String POINT = "POINT(-151.00000005215406 80.9999999916181)";
    public final static String LINESTRING = "LINESTRING(-89.123456 12.345678, 45.678901 -34.567890, 120.123456 67.890123)";
    public final static String POLYGON = "POLYGON((-98.345678 15.234567, -98.345678 35.234567, -78.345678 35.234567, -78.345678 15.234567, -98.345678 15.234567))";
    public final static String MULTIPOINT = "MULTIPOINT(-123.456789 -45.678901, 67.890123 78.901234, 10.123456 -10.123456)";
    public final static String MULTILINESTRING = "MULTILINESTRING((-50.123456 20.345678, 30.456789 -15.678901, 90.789012 55.012345), (-70.345678 -25.456789, 15.678901 40.789012, 110.012345 -5.123456))";
    public final static String MULTIPOLYGON = "MULTIPOLYGON(((-30.123456 10.234567, -30.123456 20.234567, -20.123456 20.234567, -20.123456 10.234567, -30.123456 10.234567)), ((50.456789 -30.567890, 50.456789 -25.567890, 55.456789 -25.567890, 55.456789 -30.567890, 50.456789 -30.567890)))";
    public final static String VALID_GEOMETRYCOLLECTION = "GEOMETRYCOLLECTION(POINT(15.678901 -25.345678), POLYGON((-40.123456 15.234567, -40.123456 25.234567, -30.123456 25.234567, -30.123456 15.234567, -40.123456 15.234567)))";
    public final static String INVALID_GEOMETRYCOLLECTION = "GEOMETRYCOLLECTION(POINT(15.678901 -25.345678), LINESTRING(-89.123456 12.345678, 45.678901 -34.567890, 120.123456 67.890123), POLYGON((-40.123456 15.234567, -40.123456 25.234567, -30.123456 25.234567, -30.123456 15.234567, -40.123456 15.234567)))";

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("withinFilterCases")
    public void testGeoFilter(
            String label,
            STACFilterModels.StacFilterScenario scenario,
            STACFilterModels.RequestTarget target,
            STACFilterModels.FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
    }

    static Stream<Arguments> withinFilterCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, "POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))", 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))", 600, 1)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.BBOX, new STACFilterModels.BboxValue(-2,-2,2,2), 600, 1)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.BBOX, new STACFilterModels.BboxValue(-2,-2,2,2), 600, 1).withBbox("-50,-50,50,50")),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, POINT, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, POINT, 600, 1)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, MULTIPOINT, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, MULTIPOINT, 600, 1)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, LINESTRING, 600, 7)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, MULTILINESTRING, 600, 12)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, POLYGON, 600, 64)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, POLYGON, 600, 66)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, MULTIPOLYGON, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, MULTIPOLYGON, 600, 105)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, VALID_GEOMETRYCOLLECTION, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_INTERSECTS, VALID_GEOMETRYCOLLECTION, 600, 2))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("withinFilterErrorCases")
    public void testErrorGeoFilter(
            String label,
            STACFilterModels.StacFilterScenario scenario,
            STACFilterModels.RequestTarget target,
            STACFilterModels.FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        response.assertThat().statusCode(400);
    }

    static Stream<Arguments> withinFilterErrorCases() {
        return Stream.of(
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, LINESTRING, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, MULTILINESTRING, 600, 0)),
                scenarioCases(StacFilterScenario.simple("geo_params.geometry", STACFilterModels.FilterOperator.ST_WITHIN, INVALID_GEOMETRYCOLLECTION, 600, 0))
        ).flatMap(s -> s);
    }



}
