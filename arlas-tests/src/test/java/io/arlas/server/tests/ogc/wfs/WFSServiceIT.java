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

import io.arlas.server.tests.DataSetTool;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.MultiValueFilter;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import java.util.Arrays;
import java.util.Optional;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;

public class WFSServiceIT extends AbstractWFSServiceTest {

    @Test
    public void testGetFeatureHeaderFilter() throws Exception {
        Filter filter = new Filter();
        filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));
        handleGetFeatureHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")), filter)
        );
    }

    @Test
    public void testGetFeatureNoHeaderFilter() throws Exception {
        handleGetFeatureNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")), new Filter())
        );
    }

    @Test
    public void testGetFeatureWithAvailableColumn() throws Exception {
        handleGetFeatureNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.empty()));

        handleGetFeatureNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("params.job,params.country,params.city")));
    }

    @Test
    public void testGetFeatureWithNotReturnedColumn() throws Exception {
        handleGetFeatureColumnFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("id")));
    }

    @Test
    public void testGetFeatureWithCollectionBasedColumnFiltering() throws Exception {
        handleGetFeatureNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("params.job,params.country,params.city")));

        handleGetFeatureNoHeaderFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of(COLLECTION_NAME + ":params.job," + COLLECTION_NAME + ":params.country," + COLLECTION_NAME + "params.city")));

        handleGetFeatureColumnFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("notExisting:params.job,notExisting:params.country,notExisting:params.city,params.startdate")));

        handleUnavailableCollection(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("notExisting:params")));

        handleUnavailableCollection(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("COUNT", "1000"),
                        new ImmutablePair<>("REQUEST", "GetFeature")),
                        new Filter(),
                        Optional.of("")));
    }

    @Test
    public void testGetPropertyValueNoHeaderFilter() throws Exception {
        handleGetPropertyValueNoHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")), new Filter()));
    }

    @Test
    public void testGetPropertyValueHeaderFilter() throws Exception {
        Filter filter = new Filter();
        filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));

        handleGetPropertyValueHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")), filter));
    }

    @Test
    public void testGetPropertyValueWithAvailableColumn() throws Exception {
        handleGetPropertyValueNoHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.empty()));

        handleGetPropertyValueNoHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of("params.job")));
    }

    @Test
    public void testGetPropertyValueWithUnavailableColumn() throws Exception {
        handleUnavailableColumn(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of("id")));
    }

    @Test
    public void testGetPropertyValueWitCollectionBasedColumnFilter() throws Exception {
        handleGetPropertyValueNoHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of("params.job")));

        handleGetPropertyValueNoHeaderFilter(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of(COLLECTION_NAME + ":params.job")));

        handleUnavailableColumn(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of("fullname,notExisting:params.job")));

        handleUnavailableCollection(get(Arrays.asList(
                new ImmutablePair<>("SERVICE", "WFS"),
                new ImmutablePair<>("VERSION", "2.0.0"),
                new ImmutablePair<>("COUNT", "1000"),
                new ImmutablePair<>("REQUEST", "GetPropertyValue"),
                new ImmutablePair<>("valuereference", "params.job")),
                new Filter(),
                Optional.of("notExisting:params.job")));
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

    @Test
    public void testDescribeFeatureWithAvailableColumn() throws Exception {
        handleDescribeFeature(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.empty()));

        handleDescribeFeature(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of("params,fullname,geo_params")));

        handleDescribeFeatureColumnFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of("params")));

        handleDescribeFeatureColumnFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of("par*")));
    }

    @Test
    public void testDescribeFeatureWithCollectionBasedColumnFiltering() throws Exception {
        handleDescribeFeature(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of("params,fullname,geo_params")));

        handleDescribeFeature(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of(COLLECTION_NAME + ":params," + COLLECTION_NAME + ":fullname," + COLLECTION_NAME + ":geo_params")));

        handleDescribeFeatureColumnFilter(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of(COLLECTION_NAME + ":params,notExisting:fullname")));

        handleUnavailableCollection(
                get(Arrays.asList(
                        new ImmutablePair<>("SERVICE", "WFS"),
                        new ImmutablePair<>("VERSION", "2.0.0"),
                        new ImmutablePair<>("REQUEST", "DescribeFeatureType")),
                        new Filter(),
                        Optional.of("notExisting:fullname")));
    }

    public void handleGetFeatureHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("wfs:FeatureCollection.@numberReturned", equalTo("2"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "job", equalTo("Architect"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "country.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "city.size()", equalTo(0));
    }

    public void handleGetFeatureNoHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("wfs:FeatureCollection.@numberReturned", equalTo("595"))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "job", isOneOf(DataSetTool.jobs))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "country.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "city.size()", equalTo(0));
    }

    public void handleGetPropertyValueNoHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("ValueCollection.member.size()", equalTo(595))
                .body("ValueCollection.member[0]", isOneOf(DataSetTool.jobs));;
    }

    public void handleGetPropertyValueHeaderFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
                .body("ValueCollection.member.size()", equalTo(2))
                .body("ValueCollection.member[0]", isOneOf(DataSetTool.jobs));;
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
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(25));
        } else {
            then.statusCode(200)
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(26));
        }
    }

    public void handleDescribeFeatureColumnFilter(ValidatableResponse then) throws Exception {
        if(!DataSetTool.ALIASED_COLLECTION) {
            then.statusCode(200)
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(8));
        } else {
            then.statusCode(200)
                    .body("xs:schema.complexType.complexContent.extension.sequence.element.size()", equalTo(9));
        }
    }

    protected RequestSpecification givenFilterableRequestParams() {
        return given().contentType("application/xml");
    }

    private void handleGetFeatureColumnFilter(ValidatableResponse then) {
        then.statusCode(200)
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "job.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "fullname.size()", equalTo(0))
                .body("wfs:FeatureCollection.member[1].geodata.params" + FLATTEN_CHAR + "age.size()", equalTo(0));
    }

}
