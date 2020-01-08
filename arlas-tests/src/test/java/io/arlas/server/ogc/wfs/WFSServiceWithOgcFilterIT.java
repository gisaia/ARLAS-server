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

import io.arlas.server.model.request.Filter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.*;

@RunWith(Parameterized.class)
public class WFSServiceWithOgcFilterIT extends AbstractWFSServiceTest {

    private String testFilter;

    public WFSServiceWithOgcFilterIT(String testName, String testFilter) {
        this.testFilter = testFilter;
    }

    @Test
    public void testGetFeatureFilter() throws Exception {
        //TODO test the result, not only response code
        handleOK(
                get(getFeatureParams(),
                        new Filter())
        );
    }

    @Test
    public void testGetFeatureFilterWithEmptyColumnFilter() throws Exception {
        handleOK(
                get(getFeatureParams(),
                        new Filter(),
                        Optional.of(" "))
        );
    }

    @Test
    public void testGetFeatureFilterWithAvailableColumns() throws Exception {
        handleOK(
                get(getFeatureParams(),
                        new Filter(),
                        Optional.of("params"))
        );
    }

    @Test
    public void testGetFeatureFilterWithUnavailableColumns() throws Exception {
        handleUnavailableColumn(
                get(getFeatureParams(),
                        new Filter(),
                        Optional.of("fullname"))
        );
    }

    @Test
    public void testGetPropertyValueFilter() throws Exception {
        //TODO test the result, not only response code
        handleOK(
                get(getGetPropertyValueParams(),
                        new Filter())
        );
    }

    @Test
    public void testGetPropertyValueFilterWithEmptyColumnFilter() throws Exception {
        handleOK(
                get(getGetPropertyValueParams(),
                        new Filter(),
                        Optional.of(" "))
        );
    }

    @Test
    public void testGetPropertyValueFilterWithAvailableColumns() throws Exception {
        handleOK(
                get(getGetPropertyValueParams(),
                        new Filter(),
                        Optional.of("params"))
        );
    }

    @Test
    public void testGetPropertyValueFilterWithUnavailableColumns() throws Exception {
        handleUnavailableColumn(
                get(getGetPropertyValueParams(),
                        new Filter(),
                        Optional.of("fullname"))
        );
    }

    private List<Pair<String, String>> getFeatureParams() {
        return Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetFeature"),
                new ImmutablePair<>("FILTER", testFilter));
    }

    private List<Pair<String, String>> getGetPropertyValueParams() {
        return Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "id"),
                new ImmutablePair<>("FILTER", testFilter));
    }

    @Parameterized.Parameters(name = "filter {index}: {0}")
    public static Collection data() {
        return Arrays.asList(new Object[][] {
               {"PropertyIsEqualTo", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsEqualTo matchAction=\"Any\" " +
                       "matchCase=\"true\"><ValueReference>params.hob</ValueReference><Literal>Architect</Literal></PropertyIsEqualTo></Filter>"},
                {"PropertyIsLessThan", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsLessThan><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsLessThan></Filter>"},
                {"PropertyIsLessThanOrEqualTo", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsLessThanOrEqualTo><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsLessThanOrEqualTo></Filter>"},
                {"PropertyIsGreaterThan", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsGreaterThan><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsGreaterThan></Filter>"},
                {"PropertyIsGreaterThanOrEqualTo", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsGreaterThanOrEqualTo><ValueReference>params.age</ValueReference><Literal>3400</Literal></PropertyIsGreaterThanOrEqualTo></Filter>"},
                {"PropertyIsLessThanOrEqualTo  AND PropertyIsEqualTo", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><And><PropertyIsEqualTo matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                        ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo></And></Filter>"},
                {"PropertyIsLessThanOrEqualTo OR PropertyIsEqualTo", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                        ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo></Or></Filter>"},
                {"PropertyIsEqualTo OR (PropertyIsLessThanOrEqualTo AND PropertyIsGreaterThan", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><And><PropertyIsLessThanOrEqualTo><ValueReference>params" +
                        ".age</ValueReference><Literal>3200</Literal></PropertyIsLessThanOrEqualTo><PropertyIsGreaterThan><ValueReference>params.age</ValueReference><Literal>3000</Literal></PropertyIsGreaterThan></And></Or></Filter>"},
                {"PropertyIsBetween", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsBetween><ValueReference>params" +
                        ".age</ValueReference><LowerBoundary><Literal>3000</Literal></LowerBoundary><UpperBoundary><Literal>3200</Literal></UpperBoundary></PropertyIsBetween></Filter>"},
                {"PropertyIsEqualTo OR PropertyIsBetween", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Or><PropertyIsEqualTo matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>id</ValueReference><Literal>ID__170__20DI</Literal></PropertyIsEqualTo><PropertyIsBetween><ValueReference>params" +
                        ".age</ValueReference><LowerBoundary><Literal>3000</Literal></LowerBoundary><UpperBoundary><Literal>3200</Literal></UpperBoundary></PropertyIsBetween></Or></Filter>"},
                {"Not PropertyIsEqual", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><Not><PropertyIsEqualTo matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>params.job</ValueReference><Literal>Architect</Literal></PropertyIsEqualTo></Not></Filter>"},
                {"BinaryTemporalOperator", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"><During><ValueReference>params.stopdate</ValueReference><gml:TimePeriod " +
                        "gml:id=\"TP1\"><gml:begin><gml:TimeInstant gml:id=\"TI1\"><gml:timePosition>2005-05-17T00:00:00Z</gml:timePosition></gml:TimeInstant></gml:begin><gml:end><gml:TimeInstant " +
                        "gml:id=\"TI2\"><gml:timePosition>2005-05-23T00:00:00Z</gml:timePosition></gml:TimeInstant></gml:end></gml:TimePeriod></During></Filter>"},
                { "PropertyIsNull", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"><PropertyIsNull matchAction=\"Any\" " +
                        "matchCase=\"true\"><ValueReference>params.job</ValueReference><Literal>Architect</Literal></PropertyIsNull></Filter>"}
//                {"BBOX - TODO fix BBOX it is not working at all in WFS project", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"><BBOX><ValueReference>geo_params
//                 .geometry</ValueReference><gml:Envelope srsName=\"http://www.opengis" +
//                        ".net/def/crs/epsg/0/4326\"><gml:lowerCorner>13.0983 31.5899</gml:lowerCorner><gml:upperCorner>35.5472 42.8143</gml:upperCorner></gml:Envelope></BBOX></Filter>"},
//                {"within - TODO fix WITHIN it is not working at all in WFS project", "<Filter xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"><Within><ValueReference>geo_params.geometry</ValueReference><gml:Polygon gml:id=\"P1\" " +
//                        "srsName=\"http://www.opengis.net/def/crs/epsg/0/4326\"><gml:exterior><gml:LinearRing><gml:posList>10 10 20 20 30 30 40 40 10 " +
//                        "10</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></Within></Filter>"},

        });
    }

}
