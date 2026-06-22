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

import io.arlas.server.core.model.request.Filter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class WFSServiceWithOgcFilterIT extends AbstractWFSServiceTest {

    @ParameterizedTest(name = "GetFeature filter [{index}] {0}")
    @MethodSource("filters")
    void testGetFeatureFilter(String testName, String testFilter) throws Exception {
        handleOK(
                get(getFeatureParams(testFilter),
                        new Filter())
        );
    }

    @ParameterizedTest(name = "GetFeature empty column filter [{index}] {0}")
    @MethodSource("filters")
    void testGetFeatureFilterWithEmptyColumnFilter(String testName, String testFilter) throws Exception {
        handleOK(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.empty())
        );
    }

    @ParameterizedTest(name = "GetFeature available columns [{index}] {0}")
    @MethodSource("filters")
    void testGetFeatureFilterWithAvailableColumns(String testName, String testFilter) throws Exception {
        handleOK(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.of("params"))
        );
    }

    @ParameterizedTest(name = "GetFeature unavailable columns [{index}] {0}")
    @MethodSource("filters")
    void testGetFeatureFilterWithUnavailableColumns(String testName, String testFilter) throws Exception {
        handleUnavailableColumn(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.of("fullname"))
        );
    }

    @ParameterizedTest(name = "GetFeature collection based column filtering [{index}] {0}")
    @MethodSource("filters")
    void testGetFeatureFilterWithCollectionBasedColumnFiltering(String testName, String testFilter) throws Exception {
        handleOK(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.of(COLLECTION_NAME + ":params"))
        );

        handleUnavailableColumn(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.of("notExisting:params,fullname"))
        );

        handleUnavailableCollection(
                get(getFeatureParams(testFilter),
                        new Filter(),
                        Optional.of("notExisting:params"))
        );
    }

    @ParameterizedTest(name = "GetPropertyValue filter [{index}] {0}")
    @MethodSource("filters")
    void testGetPropertyValueFilter(String testName, String testFilter) throws Exception {
        handleOK(
                get(getGetPropertyValueParams(testFilter),
                        new Filter())
        );
    }

    @ParameterizedTest(name = "GetPropertyValue empty column filter [{index}] {0}")
    @MethodSource("filters")
    void testGetPropertyValueFilterWithEmptyColumnFilter(String testName, String testFilter) throws Exception {
        handleOK(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.empty())
        );
    }

    @ParameterizedTest(name = "GetPropertyValue available columns [{index}] {0}")
    @MethodSource("filters")
    void testGetPropertyValueFilterWithAvailableColumns(String testName, String testFilter) throws Exception {
        handleOK(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.of("params"))
        );
    }

    @ParameterizedTest(name = "GetPropertyValue unavailable columns [{index}] {0}")
    @MethodSource("filters")
    void testGetPropertyValueFilterWithUnavailableColumns(String testName, String testFilter) throws Exception {
        handleUnavailableColumn(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.of("fullname"))
        );
    }

    @ParameterizedTest(name = "GetPropertyValue collection based column filtering [{index}] {0}")
    @MethodSource("filters")
    void testGetPropertyValueFilterWithCollectionBasedColumnFiltering(String testName, String testFilter) throws Exception {
        handleOK(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.of(COLLECTION_NAME + ":params"))
        );

        handleUnavailableColumn(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.of("notExisting:params,fullname"))
        );

        handleUnavailableCollection(
                get(getGetPropertyValueParams(testFilter),
                        new Filter(),
                        Optional.of("notExisting:params"))
        );
    }

    private List<Pair<String, String>> getFeatureParams(String testFilter) {
        return Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetFeature"),
                new ImmutablePair<>("FILTER", testFilter));
    }

    private List<Pair<String, String>> getGetPropertyValueParams(String testFilter) {
        return Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "id"),
                new ImmutablePair<>("FILTER", testFilter));
    }

    static Stream<Arguments> filters() {
        return Stream.of(
                Arguments.of("PropertyIsEqualTo",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>params.hob</ValueReference><Literal>Architect</Literal></PropertyIsEqualTo></Filter>"),
                Arguments.of("PropertyIsLessThan",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsLessThan><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsLessThan></Filter>"),
                Arguments.of("PropertyIsLessThanOrEqualTo",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsLessThanOrEqualTo><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsLessThanOrEqualTo></Filter>"),
                Arguments.of("PropertyIsGreaterThan",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsGreaterThan><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsGreaterThan></Filter>"),
                Arguments.of("PropertyIsGreaterThanOrEqualTo",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsGreaterThanOrEqualTo><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsGreaterThanOrEqualTo></Filter>"),
                Arguments.of("PropertyIsLessThanOrEqualTo AND PropertyIsEqualTo",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><And><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                                ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo></And></Filter>"),
                Arguments.of("PropertyIsLessThanOrEqualTo OR PropertyIsEqualTo",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                                ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo></Or></Filter>"),
                Arguments.of("PropertyIsEqualTo OR (PropertyIsLessThanOrEqualTo AND PropertyIsGreaterThan)",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><And><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                                ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo><PropertyIsGreaterThan><ValueReference>params.age</ValueReference><Literal>3000</Literal></PropertyIsGreaterThan></And></Or></Filter>"),
                Arguments.of("PropertyIsBetween",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsBetween><ValueReference>params" +
                                ".age</ValueReference><LowerBoundary><Literal>3000</Literal></LowerBoundary><UpperBoundary><Literal>3200</Literal></UpperBoundary></PropertyIsBetween></Filter>"),
                Arguments.of("PropertyIsEqualTo OR PropertyIsBetween",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsBetween><ValueReference>params" +
                                ".age</ValueReference><LowerBoundary><Literal>3000</Literal></LowerBoundary><UpperBoundary><Literal>3200</Literal></UpperBoundary></PropertyIsBetween></Or></Filter>"),
                Arguments.of("Not PropertyIsEqual",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Not><PropertyIsEqualTo matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>params.job</ValueReference><Literal>Architect</Literal></PropertyIsEqualTo></Not></Filter>"),
                Arguments.of("BinaryTemporalOperator",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"><During><ValueReference>params.stopdate</ValueReference><gml:TimePeriod " +
                                "gml:id=\"TP1\"><gml:begin><gml:TimeInstant gml:id=\"TI1\"><gml:timePosition>2005-05-17T00:00:00Z</gml:timePosition></gml:TimeInstant></gml:begin><gml:end><gml:TimeInstant " +
                                "gml:id=\"TI2\"><gml:timePosition>2005-05-23T00:00:00Z</gml:timePosition></gml:TimeInstant></gml:end></gml:TimePeriod></During></Filter>"),
                Arguments.of("PropertyIsNull",
                        "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsNull matchAction=\"Any\" " +
                                "matchCase=\"true\"><ValueReference>params.job</ValueReference><Literal>Architect</Literal></PropertyIsNull></Filter>")
        );
    }
}