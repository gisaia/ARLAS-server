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

import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.MultiValueFilter;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.arlas.server.tests.stac.STACFilterModels.StacFilterScenario;
import io.arlas.server.tests.stac.STACFilterModels.RequestTarget;
import io.arlas.server.tests.stac.STACFilterModels.FilterLang;
import io.arlas.server.tests.stac.STACFilterModels.FilterOperator;
import io.arlas.server.tests.stac.STACFilterModels.FilterClause;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class STACServiceHeaderFilterIT extends AbstractSTACServiceTest {

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("partitionFilterCases")
    public void testPartitionFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
        String path = "features.properties.params.country";
        response.body(path, everyItem(equalTo("Angola")));
    }

    static Stream<Arguments> partitionFilterCases() {
        Filter partitionFilter = new Filter();
        partitionFilter.f = List.of(new MultiValueFilter<>(new Expression("params.country", OperatorEnum.eq, "Angola")));
        return Stream.of(
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.EQ, 0),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1000000)),
                        600, 0
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.GTE, 0),
                                new FilterClause("params.startdate", FilterOperator.LTE, 1000000)),
                        600, 0
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Chemist"),
                                new FilterClause("params.age", FilterOperator.EQ, 600),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1050600)),
                        600, 0
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Coder"),
                                new FilterClause("params.age", FilterOperator.EQ, 1000),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1071000)),
                        600, 0
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Brain Scientist"),
                                new FilterClause("params.age", FilterOperator.EQ, 400),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1040000)),
                        600, 1
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.NE, "Actor"),
                                new FilterClause("params.age", FilterOperator.LT, 7000),
                                new FilterClause("params.startdate", FilterOperator.GT, 950000)),
                        600, 36
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.GTE, "Chemist"),
                                new FilterClause("params.age", FilterOperator.LTE, 2600),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1020000)),
                        600, 5
                ).withPartitionFilter(partitionFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 34
                ).withPartitionFilter(partitionFilter))
        ).flatMap(s -> s);
    }


    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("authorizeColumnFilterCases")
    public void testColumnFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
        String path = "features.properties.params";
        JsonPath jsonPath = response.extract().jsonPath();
        List<Map<String, Object>> params = jsonPath.getList(path);
        params.forEach(param -> {
            assertThat(param, hasKey("job"));
            assertThat(param, hasKey("startdate"));
            assertThat(param, hasKey("age"));
            assertThat(param.size(), equalTo(3));
        });

    }

    static Stream<Arguments> authorizeColumnFilterCases() {
        String columnFilter = "geodata:params.job,geodata:params.age,geodata:params.startdate";
        return Stream.of(
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 424

                ).withColumnFilter(columnFilter))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("forbiddenCollectionFilterCases")
    public void testunknowCollectionFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        response.assertThat().statusCode(403);
    }

    static Stream<Arguments> forbiddenCollectionFilterCases() {
        String columnFilter = "unknowCollection:params.job,unknowCollection:params.age,unknowCollection:params.startdate";
        return Stream.of(
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 424

                ).withColumnFilter(columnFilter))
        ).flatMap(s -> s);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("partitionAndColumnFilterCases")
    public void testPartitionAndColumnFilter(
            String label,
            StacFilterScenario scenario,
            RequestTarget target,
            FilterLang lang) throws Exception {
        ValidatableResponse response = executeRequest(scenario, target, lang);
        assertCommonResponse(response, scenario, target);
        assertReturnedValues(response, scenario);
        String path = "features.properties.params.country";
        response.body(path, everyItem(equalTo("Angola")));
        String pathParams = "features.properties.params";
        JsonPath jsonPath = response.extract().jsonPath();
        List<Map<String, Object>> params = jsonPath.getList(pathParams);
        params.forEach(param -> {
            assertThat(param, hasKey("job"));
            assertThat(param, hasKey("startdate"));
            assertThat(param, hasKey("age"));
            assertThat(param, hasKey("country"));
            assertThat(param.size(), equalTo(4));
        });
    }

    static Stream<Arguments> partitionAndColumnFilterCases() {
        Filter partitionFilter = new Filter();
        partitionFilter.f = List.of(new MultiValueFilter<>(new Expression("params.country", OperatorEnum.eq, "Angola")));
        String columnFilter = "geodata:params.job,params.age,params.startdate,params.country";
        return Stream.of(
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.EQ, 0),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1000000)),
                        600, 0
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Actor"),
                                new FilterClause("params.age", FilterOperator.GTE, 0),
                                new FilterClause("params.startdate", FilterOperator.LTE, 1000000)),
                        600, 0
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Chemist"),
                                new FilterClause("params.age", FilterOperator.EQ, 600),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1050600)),
                        600, 0
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Coder"),
                                new FilterClause("params.age", FilterOperator.EQ, 1000),
                                new FilterClause("params.startdate", FilterOperator.EQ, 1071000)),
                        600, 0
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.EQ, "Brain Scientist"),
                                new FilterClause("params.age", FilterOperator.EQ, 400),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1040000)),
                        600, 1
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.NE, "Actor"),
                                new FilterClause("params.age", FilterOperator.LT, 7000),
                                new FilterClause("params.startdate", FilterOperator.GT, 950000)),
                        600, 36
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.GTE, "Chemist"),
                                new FilterClause("params.age", FilterOperator.LTE, 2600),
                                new FilterClause("params.startdate", FilterOperator.GTE, 1020000)),
                        600, 5
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter)),
                scenarioCases(StacFilterScenario.and(
                        List.of(
                                new FilterClause("params.job", FilterOperator.LTE, "Dancer"),
                                new FilterClause("params.age", FilterOperator.GT, 0),
                                new FilterClause("params.startdate", FilterOperator.LT, 1100000)),
                        600, 34
                ).withPartitionFilter(partitionFilter).withColumnFilter(columnFilter))
        ).flatMap(s -> s);
    }
}
