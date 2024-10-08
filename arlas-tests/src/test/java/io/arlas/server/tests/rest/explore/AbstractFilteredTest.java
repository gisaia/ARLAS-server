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

package io.arlas.server.tests.rest.explore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.MultiValueFilter;
import io.arlas.server.core.model.request.Request;
import io.arlas.server.tests.AbstractTestWithCollection;
import io.arlas.server.tests.DataSetTool;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.commons.rest.utils.ServerConstants.PARTITION_FILTER;
import static org.hamcrest.Matchers.*;

public abstract class AbstractFilteredTest extends AbstractTestWithCollection {

    private static ObjectMapper objectMapper = new ObjectMapper();
    @Before
    public void setUpFilter() {
        request = new Request();
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testFieldFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])));//("job:eq:" + DataSetTool.jobs[0]);
        handleFieldFilter(post(request), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 59, "Actor");
        handleFieldFilter(header(request.filter), 59, "Actor");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1])));//"job:eq:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleFieldFilter(post(request), 117, "Actor", "Announcers");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 117, "Actor", "Announcers");
        handleFieldFilter(header(request.filter), 117, "Actor", "Announcers");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "cto")));//"job:like:" + "cto");
        handleFieldFilter(post(request), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 59, "Actor");
        handleFieldFilter(header(request.filter), 59, "Actor");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("text_search", OperatorEnum.like, "the word")));//"text_search:like:the word");
        handleTextFieldLikeFilter(post(request), 595, "Search the word");
        handleTextFieldLikeFilter(get("f", request.filter.f.get(0).get(0).toString()), 595, "Search the word");
        handleTextFieldLikeFilter(header(request.filter), 595, "Search the word");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("text_search", OperatorEnum.like, "Ice")));//"text_search:like:Ice");
        handleTextFieldLikeFilter(post(request), 117, "Search the word Ice");
        handleTextFieldLikeFilter(get("f", request.filter.f.get(0).get(0).toString()), 117, "Search the word Ice");
        handleTextFieldLikeFilter(header(request.filter), 117, "Search the word Ice");

        /** same test above but to show the 'like' operation is NOT case-sensitive **/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("text_search", OperatorEnum.like, "ice")));//"text_search:like:ice");
        handleTextFieldLikeFilter(post(request), 117, "Search the word Ice");
        handleTextFieldLikeFilter(get("f", request.filter.f.get(0).get(0).toString()), 117, "Search the word Ice");
        handleTextFieldLikeFilter(header(request.filter), 117, "Search the word Ice");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.ne, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1])));//"job:ne:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleFieldFilter(post(request), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");
        handleFieldFilter(header(request.filter), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "UnknownJob")));//"job:eq:UnknownJob");
        handleUnknownFieldFilter(post(request));
        handleUnknownFieldFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleUnknownFieldFilter(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Actor")), new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Announcers")));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()), new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))));
        handleNotMatchingRequest(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Actor:Announcers")));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()))));
        handleNotMatchingRequest(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Actor"), new Expression("params.job", OperatorEnum.eq, "Announcers"))));
        handleFieldFilter(post(request), 117, "Actor", "Announcers");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()), 117, "Actor", "Announcers");
        handleFieldFilter(header(request.filter), 117, "Actor", "Announcers");

        // TIMESTAMP LTE, LT, GTE, GT
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "775000")));
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "775000||/s")));
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.gt, "1250000")));
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "now-1M/y")));
        handleFieldFilter(post(request), 595);
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 595);
        handleFieldFilter(header(request.filter), 595);

        request.filter.f = null;

    }

    @Test
    public void testQueryFilter() throws Exception {

        request.filter.q = Arrays.asList(new MultiValueFilter<>("My name is"));
        handleMatchingQueryFilter(post(request), 595);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0)), 595);
        handleMatchingQueryFilter(header(request.filter), 595);

        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name:is"));
        handleNotMatchingQueryFilter(post(request));
        handleNotMatchingQueryFilter(get("q", request.filter.q.get(0).get(0)));
        handleNotMatchingQueryFilter(header(request.filter));
        handleUnavailableCollection(post(request, COLUMN_FILTER, ""));
        handleUnavailableCollection(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, ""));
        handleUnavailableCollection(header(request.filter, COLUMN_FILTER, ""));

        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name is"));
        handleMatchingQueryFilter(post(request, COLUMN_FILTER, "fullname*"), 595);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "fullname*"), 595);
        handleMatchingQueryFilter(header(request.filter, COLUMN_FILTER, "id"), 595);//header is not column filtered

        request.filter.q = Arrays.asList(new MultiValueFilter<>("UnknownQuery"));
        handleNotMatchingQueryFilter(post(request));
        handleNotMatchingQueryFilter(get("q", request.filter.q.get(0).get(0)));
        handleNotMatchingQueryFilter(header(request.filter));

        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name is"), new MultiValueFilter<>("foo"));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(get(Arrays.asList(new ImmutablePair<>("q", request.filter.q.get(0).get(0)),
                new ImmutablePair<>("q", request.filter.q.get(1).get(0)))));
        handleNotMatchingRequest(header(request.filter));

        request.filter.q = Arrays.asList(new MultiValueFilter<>(Arrays.asList("400", "600", "800", "1000")));
        handleMatchingQueryFilter(post(request), 68);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0) + ";" + request.filter.q.get(0).get(1) + ";" + request.filter.q.get(0).get(2) + ";" + request.filter.q.get(0).get(3)), (68));
        handleMatchingQueryFilter(header(request.filter), 68);

        request.filter.q = null;
    }

    @Test
    public void testRangeFilter() throws Exception {

        // TIMESTAMP RANGE
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000]")));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        //ALIAS
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[0<775000]")));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        //ALIAS
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[0<775000||-3s/s[")));
        handleMatchingTimestampRangeFilter(post(request), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 772000, 2);

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[770000<775000]")));
        handleMatchingTimestampRangeFilter(post(request), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 770000, 775000, 2);

        //MULTIRANGE
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[0<765000],[770000<775000]")));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1270000<1283600]")));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[now<now+2M/d]")));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[765000<770000],[1270000<1283600]")));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        // TEXT RANGE
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.range, "[Ac<An]")));
        handleMatchingStringRangeFilter(post(request), "Ac", "An", 59);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), "Ac", "An", 59);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "An", 59);

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.range, "[Coa<Cod]")));
        handleMatchingStringRangeFilter(post(request), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(header(request.filter), "Coa", "Cod", 58);

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.range, "[Coa<Cod],[Ac<An]")));
        handleMatchingStringRangeFilter(post(request), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).get(0).toString()), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "Cod", 117);

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.range, "[X<Z]")));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = null;
    }

    @Test
    public void testPwithinFilter() throws Exception {
        /** west < east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-5,-5,5,5")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        handleUnavailableCollection(post(request, COLUMN_FILTER, ""));
        handleUnavailableCollection(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, ""));

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -5, -5 5, 5 5, 5 -5, -5 -5))")));
        handleMatchingGeometryFilter(post(request, COLUMN_FILTER, "fullname"), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "fullname"), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        /** counter clock-wise WKT with right_hand true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -5, 5 -5, 5 5, -5 5, -5 -5))")));
        request.filter.righthand = true;
        handleMatchingGeometryFilter(post(request, COLUMN_FILTER, "fullname"), 1, everyItem(equalTo("0,0")));
        ValidatableResponse getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .header(COLUMN_FILTER, "fullname")
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        request.filter.righthand = false;

        /** west > east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "5,-5,-5,5")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -5, 5 -5, 5 5, -5 5, -5 -5))")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        /** clock-wise WKT with right_hand true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -5, -5 5, 5 5, 5 -5, -5 -5))")));
        request.filter.righthand = true;
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** west > east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "180,0,-165,5")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,-170")));

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((180 5, -165 5, -165 -5, 180 -5, 180 5))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,-170")));
        /** clock-wise WKT with righthand =true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((180 5, 180 -5, -165 -5, -165 5, 180 5))")));
        request.filter.righthand = true;
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,-170")));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,-170")));
        request.filter.righthand = false;


        /** west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "175,85,180,90")));
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingPwithinFilter(header(request.filter));

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((175 85, 175 90, 180 90, 180 85, 175 85))")));
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingPwithinFilter(header(request.filter));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((175 85, 180 85, 180 90, 175 90, 175 85))")));
        request.filter.righthand = true;
        handleNotMatchingPwithinFilter(post(request));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingPwithinFilter(getWithRightHand);
        handleNotMatchingPwithinFilter(header(request.filter));
        request.filter.righthand = false;

        /** west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-5,-50,180,50")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-180,-50,5,50")));
        handleMatchingGeometryFilter(post(request), 11, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                11, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 11, everyItem(endsWith("0")));

        /** clock-wise WKT with west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -50, -5 50, 180 50, 180 -50, -5 -50))")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-180,-50,5,50")));
        handleMatchingGeometryFilter(post(request), 11, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                11, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 11, everyItem(endsWith("0")));
        /** counter clock-wise WKT with righthand=true AND west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "POLYGON((-5 -50, 180 -50, 180 50, -5 50, -5 -50))")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-180,-50,5,50")));
        request.filter.righthand = true;
        handleMatchingGeometryFilter(post(request), 11, everyItem(endsWith("0")));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(
                getWithRightHand,
                11, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 11, everyItem(endsWith("0")));
        request.filter.righthand = false;



        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.centroid", OperatorEnum.within, "-5,-5,5,5"),
                new Expression("geo_params.centroid", OperatorEnum.within, "5,5,15,15"))));
        handleMatchingGeometryFilter(post(request), 2, everyItem(isOneOf("0,0", "10,10")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()), 2, everyItem(isOneOf("0,0", "10,10")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(isOneOf("0,0", "10,10")));

        /** --- NOT WITHIN --- **/

        /** west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-170,-85,175,85")));
        handleMatchingGeometryFilter(post(request), 0, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(header(request.filter), 0, everyItem(endsWith("170")));

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-170 -85, -170 85, 175 85, 175 -85, -170 -85))")));
        handleMatchingGeometryFilter(post(request), 0, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 0, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(header(request.filter), 0, everyItem(endsWith("170")));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-170 -85, 175 -85, 175 85, -170 85, -170 -85))")));
        request.filter.righthand = true;
        handleMatchingGeometryFilter(post(request), 0, everyItem(endsWith("170")));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 0, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(header(request.filter), 0, everyItem(endsWith("170")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-2 -2, 2 -2, 2 2,-2 2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        /** clock-wise WKT with righthand =true*/
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-175,-85,175,85")));
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingNotPwithinFilter(header(request.filter));

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-175 -85, -175 85, 175 85, 175 -85, -175 -85))")));
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingNotPwithinFilter(header(request.filter));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-175 -85, 175 -85, 175 85, -175 85, -175 -85))")));
        request.filter.righthand = true;
        handleNotMatchingNotPwithinFilter(post(request));
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingNotPwithinFilter(getWithRightHand);
        handleNotMatchingNotPwithinFilter(header(request.filter));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((160 -2, 160 2, -160 2, -160 -2, 160 -2))")));
        handleMatchingGeometryFilter(post(request), 591, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 591, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 591, everyItem(notNullValue()));
        /** clock-wise WKT with righthand=true */
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((160 -2,  -160 -2, -160 2, 160 2, 160 -2))")));
        handleMatchingGeometryFilter(post(request), 591, everyItem(notNullValue()));
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand, 591, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 591, everyItem(notNullValue()));
        request.filter.righthand = false;


        /** clock-wise multi polygon **/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "MULTIPOLYGON (((-180 -90, -180 90, -5 90, -5 -90, -180 -90)), ((5 -90, 5 90, 180 90, 180 -90, 5 -90)))")));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));
        /** counter clock-wise multi polygon with righthand=true**/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "MULTIPOLYGON (((-180 -90, -5 -90, -5 90, -180 90, -180 -90)), ((5 -90, 180 -90, 180 90, 5 90, 5 -90)))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(getWithRightHand, 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));
        request.filter.righthand = false;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-180,-90,-5,90")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90")));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));

        /** clock wise with BBOX*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-180 -90, -180 90, -5 90, -5 -90, -180 -90))")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90")));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));
        /** counter clock wise with righthand=true && BBOX*/
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-180 -90, -5 -90, -5 90, -180 90, -180 -90))")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90")));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        getWithRightHand = this.getExtraParamsRequest().param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(getWithRightHand,
                17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));
        request.filter.righthand = false;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-180,-90,-5,90"),
                new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90"))));
        handleMatchingGeometryFilter(post(request), 595, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()), 595, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 595, everyItem(notNullValue()));

        /** clock wise OR BBOX*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-180 -90, -180 90, -5 90, -5 -90, -180 -90))"),
                new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90"))));
        handleMatchingGeometryFilter(post(request), 595, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()), 595, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 595, everyItem(notNullValue()));
        /** counter clock wise (with righthand=true) OR BBOX*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.centroid", OperatorEnum.notwithin, "POLYGON((-180 -90, -5 -90, -5 90, -180 90, -180 -90))"),
                new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,-90,180,90"))));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 595, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand, 595, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 595, everyItem(notNullValue()));
        request.filter.righthand = false;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-11,-11,11,11")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-5,-5,5,5")));
        handleMatchingGeometryFilter(post(request), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .when().get(getUrlPath("geodata"))
                        .then(), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(header(request.filter), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-6,-6,6,6")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-5,-5,5,5")));
        handleNotMatchingPwithinComboFilter(post(request));
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingPwithinComboFilter(header(request.filter));

        //column filter allows other geometry fields
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.other_geopoint", OperatorEnum.within, "-5,-5,5,5")));
        handleMatchingGeometryFilter(post(request, COLUMN_FILTER, "geo_params"), 1, everyItem(equalTo("-10,-10")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "geo_params"), 1, everyItem(equalTo("-10,-10")));

        request.filter.f = null;
    }

    @Test
    public void testGwithinFilter() throws Exception {
        /** west < east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-2,-2,2,2")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-170,-2,170,2")));
        handleMatchingGeometryFilter(post(request), 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 33, everyItem(startsWith("0,")));

        /** west > east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "2,-2,-2,2")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "170,-2,-170,2")));
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingGwithinFilter(header(request.filter));

        /** clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))")));
        request.filter.righthand = true;
        ValidatableResponse getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        request.filter.righthand = false;

        /** clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-170 -2, -170 2, 170 2, 170 -2, -170 -2))")));
        handleMatchingGeometryFilter(post(request), 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 33, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-170 -2, 170 -2, 170 2, -170 2, -170 -2))")));
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 33, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 33, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** counter clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-170 -2, 170 -2, 170 2, -170 2, -170 -2))")));
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingGwithinFilter(header(request.filter));
        /** clock-wise WKT with righthand=true*/
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-170 -2, -170 2, 170 2, 170 -2, -170 -2))")));
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(getWithRightHand);
        handleNotMatchingGwithinFilter(header(request.filter));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-330 -2, -330 -1, -290 -1, -290 2, -15 2, -15 1, -40 1, -40 -2, -330 -2))")));
        handleMatchingGeometryFilter(post(request), 23, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 23, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 23, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-330 -2, -40 -2, -40 1, -15 1, -15 2, -290 2, -290 -1, -330 -1, -330 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 23, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 23, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 23, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-330 -2, -40 -2, -40 1, -15 1, -15 2, -290 2, -290 -1, -330 -1, -330 -2))")));
        handleMatchingGeometryFilter(post(request), 10, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 10, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 10, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-330 -2, -330 -1, -290 -1, -290 2, -15 2, -15 1, -40 1, -40 -2, -330 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 10, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 10, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 10, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((10 -2, 10 2, 350 2, 350 0, 300 0, 350 -2, 10 -2))")));
        handleMatchingGeometryFilter(post(request), 27, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 27, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 27, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((10 -2, 350 -2, 300 0, 350 0, 350 2, 10 2, 10 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 27, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 27, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 27, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((10 -2, 350 -2, 300 0, 350 0, 350 2, 10 2, 10 -2))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((10 -2, 10 2, 350 2, 350 0, 300 0, 350 -2, 10 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** clock-wise WKT with a point longitude > 180*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-130 -2, -130 2, 220 2, 220 -2, -130 -2))")));
        handleMatchingGeometryFilter(post(request), 32, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 32, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 32, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with a point longitude > 180 with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-130 -2, 220 -2, 220 2, -130 2,  -130 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 32, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 32, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 32, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** counter clock-wise WKT with a point longitude > 180*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-130 -2, 220 -2, 220 2, -130 2,  -130 -2))")));
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingGwithinFilter(header(request.filter));
        /** clock-wise WKT with a point longitude > 180 with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((-130 -2, -130 2, 220 2, 220 -2, -130 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(getWithRightHand);
        handleNotMatchingGwithinFilter(header(request.filter));
        request.filter.righthand = false;


        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, 1 2, 2 2, 2 1, 1 1))")));
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingGwithinFilter(header(request.filter));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, 2 1, 2 2, 1 2, 1 1))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(getWithRightHand);
        handleNotMatchingGwithinFilter(header(request.filter));
        request.filter.righthand = false;


        /** west < east bbox along with a clock-wise WKT (AND OP)*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, 10 -20, -10 -20, -10 20, 1 1))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-2,-2,2,2")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        /** west < east bbox along with a counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, -10 20, -10 -20, 10 -20, 1 1))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-2,-2,2,2")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                getWithRightHand,
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        request.filter.righthand = false;


        /** west < east bbox along with a clock-wise WKT (OR OP)*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.within, "-2,-2,2,2"),
                new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, 10 -20, -10 -20, -10 20, 1 1))"))));
        handleMatchingGeometryFilter(post(request), 2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(isOneOf("-10,0", "0,0")));
        /** west < east bbox along with a counter clock-wise WKT with righthand=true (OR OP)*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.within, "-2,-2,2,2"),
                new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1 1, -10 20, -10 -20, 10 -20, 1 1))"))));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(
                getWithRightHand,
                2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(isOneOf("-10,0", "0,0")));
        request.filter.righthand = false;


        /** --- NOTWITHIN --- */

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, 180 -70, 160 -70, 160 -90, -180 -90, -180 90, 180 90))")));
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(getWithRightHand, 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand, 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        request.filter.righthand = false;


        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, 180 -90, -180 -90, -180 90, 180 90))")));
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingNotGwithinFilter(header(request.filter));
        /** counter clock-wise WKT with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -90, 180 -90, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(getWithRightHand);
        handleNotMatchingNotGwithinFilter(header(request.filter));
        request.filter.righthand = false;


        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((160 -2, 160 2, -160 2, -160 -2, 160 -2))")));
        handleMatchingGeometryFilter(post(request), 593, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 593, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 593, everyItem(notNullValue()));
        /** clock-wise WKT with righthand =true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((160 -2, -160 -2, -160 2, 160 2, 160 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 593, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand, 593, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 593, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** clock-wise WKTs */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90,180 -70,100 -70,100 -80,-180 -80,-180 90,180 90))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, 180 -70, 160 -70, 160 -90,-180 -90,-180 90,180 90))")));
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        /** counter clock-wise WKTs with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -80, 100 -80, 100 -70, 180 -70, 180 90))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(getWithRightHand,
                4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        request.filter.righthand = false;

        /** clock-wise WKTs (OR OP)*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, 180 -68, 99 -68, 99 -80, -180 -80, -180 90, 180 90))"),
                new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, 180 -70, 160 -70, 160 -90, -180 -90, -180 90, 180 90))"))));
        handleMatchingGeometryFilter(post(request), 43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 43, everyItem(notNullValue()));
        /** counter clock-wise WKTs with righthand=true (OR OP)*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -80, 99 -80, 99 -68, 180 -68, 180 90))"),
                new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))"))));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand,
                43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 43, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** west < east bbox along with a clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((12 12, 12 -12, -12 -12, -12 12, 12 12))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "-8,-8,8,8")));
        handleMatchingGeometryFilter(post(request), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(
                this.getExtraParamsRequest()
                        .param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then(), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(header(request.filter), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        /** west < east bbox along with a counter clock-wise WKT with righthand*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((12 12, -12 12, -12 -12, 12 -12, 12 12))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "-8,-8,8,8")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(
                getWithRightHand, 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(header(request.filter), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        request.filter.righthand = false;

        /** west < east bbox*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-12,-12,12,12")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "-11,-11,11,11")));
        handleNotMatchingGwithinComboFilter(post(request));
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingGwithinComboFilter(header(request.filter));
        request.filter.f = null;

    }

    @Test
    public void testGintersectFilter() throws Exception {
        /** west < east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-2,-2,2,2")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        /** west < east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-170,-2,170,2")));
        handleMatchingGeometryFilter(post(request), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 35, everyItem(startsWith("0,")));

        /** west > east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "2,-2,-2,2")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));

        /** west > east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "170,-2,-170,2")));
        handleMatchingGeometryFilter(post(request), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(startsWith("0,")));

        /** clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        /** counter clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))")));
        request.filter.righthand = true;
        ValidatableResponse getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        request.filter.righthand = false;


        /** clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-170 -2, -170 2, 170 2, 170 -2, -170 -2))")));
        handleMatchingGeometryFilter(post(request), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 35, everyItem(startsWith("0,")));

        /** counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-170 -2, 170 -2, 170 2, -170 2, -170 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 35, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** counter clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-170 -2, 170 -2, 170 2, -170 2, -170 -2))")));
        handleMatchingGeometryFilter(post(request), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(startsWith("0,")));

        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-170 -2, -170 2, 170 2, 170 -2, -170 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-2 -2, 2 -2, 2 2,-2 2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-330 -2, -330 -1, -290 -1, -290 2, -15 2, -15 1, -40 1, -40 -2, -330 -2))")));
        handleMatchingGeometryFilter(post(request), 31, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 31, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 31, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-330 -2, -40 -2, -40 1, -15 1, -15 2, -290 2, -290 -1, -330 -1, -330 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 31, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 31, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 31, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-330 -2, -40 -2, -40 1, -15 1, -15 2, -290 2, -290 -1, -330 -1, -330 -2))")));
        handleMatchingGeometryFilter(post(request), 12, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 12, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 12, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-330 -2, -330 -1, -290 -1, -290 2, -15 2, -15 1, -40 1, -40 -2, -330 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 12, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 12, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 12, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 -2, 10 2, 350 2, 350 0, 300 0, 350 -2, 10 -2))")));
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with righthand=true */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 -2, 350 -2, 300 0, 350 0, 350 2, 10 2, 10 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 34, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 34, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 -2, 350 -2, 300 0, 350 0, 350 2, 10 2, 10 -2))")));
        handleMatchingGeometryFilter(post(request), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 8, everyItem(startsWith("0,")));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 -2, 10 2, 350 2, 350 0, 300 0, 350 -2, 10 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 8, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** clock-wise WKT with with a point longitude > 180*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-130 -2, -130 2, 220 2, 220 -2, -130 -2))")));
        handleMatchingGeometryFilter(post(request), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 35, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with a point longitude > 180 with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-130 -2, 220 -2, 220 2, -130 2,  -130 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 35, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 35, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** counter clock-wise WKT with a point longitude > 180*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-130 -2, 220 -2, 220 2, -130 2,  -130 -2))")));
        handleMatchingGeometryFilter(post(request), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(startsWith("0,")));
        /** counter clock-wise WKT with a point longitude > 180*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-130 -2, -130 2, 220 2, 220 -2, -130 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 2, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(startsWith("0,")));
        request.filter.righthand = false;

        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((0 1, 1 1, 1 -1, 0 -1, 0 1))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        /** counter clock-wise WKT with righthand*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((0 1, 0 -1, 1 -1, 1 1, 0 1))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
        request.filter.righthand = false;


        /** clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((2 2, 2 3, 3 3, 3 2, 2 2))")));
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingGintersectFilter(header(request.filter));
        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((2 2, 3 2, 3 3, 2 3, 2 2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(getWithRightHand);
        handleNotMatchingGintersectFilter(header(request.filter));
        request.filter.righthand = false;

        /** WKT Linestring that crosses the dateline*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "LINESTRING(50 0, 340 0)")));
        handleMatchingGeometryFilter(post(request), 29, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 29, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 29, everyItem(startsWith("0,")));
        /** WKT Linestring that crosses the dateline with righthand = true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "LINESTRING(50 0, 340 0)")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 29, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 29, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 29, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** WKT Linestring that doesn't cross the dateline*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "LINESTRING(50 0, -20 0)")));
        handleMatchingGeometryFilter(post(request), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 8, everyItem(startsWith("0,")));
        /** WKT Linestring that doesn't cross the dateline with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "LINESTRING(50 0, -20 0)")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(getWithRightHand, 8, everyItem(startsWith("0,")));
        handleMatchingGeometryFilter(header(request.filter), 8, everyItem(startsWith("0,")));
        request.filter.righthand = false;


        /** west < east bbox */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,12,12")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "0,-1,1,1")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        /** west < east bbox along with a clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,12,12"),
                new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((0 1, 1 1, 1 -1, 0 -1, 0 1))"))));
        handleMatchingGeometryFilter(post(request), 9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 9, everyItem(notNullValue()));
        /** west < east bbox along with a counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,12,12"),
                new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((0 1, 0 -1, 1 -1, 1 1, 0 1))"))));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                getWithRightHand,
                9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 9, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** --- NOTINTERSECTS --- */

        /** clock-wise wkt*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, 180 -70, 160 -70, 160 -90,-180 -90,-180 90,180 90))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));
        /** counter clock-wise wkt with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(getWithRightHand, 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));
        request.filter.righthand = false;

        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-2 -2, 2 -2, 2 2, -2 2, -2 -2))")));
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-2 -2, -2 2, 2 2, 2 -2, -2 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand, 561, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 561, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** clock-wise wkt*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, 180 -90, -180 -90, -180 90, 180 90))")));
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(get("f", request.filter.f.get(0).get(0).toString()));
        handleNotMatchingNotGintersectFilter(header(request.filter));
        /** counter clock-wise wkt with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, -180 90, -180 -90, 180 -90, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(getWithRightHand);
        handleNotMatchingNotGintersectFilter(header(request.filter));
        request.filter.righthand = false;


        /** counter clock-wise WKT */
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((160 -2, 160 2, -160 2, -160 -2, 160 -2))")));
        handleMatchingGeometryFilter(post(request), 591, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("f", request.filter.f.get(0).get(0).toString()), 591, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 591, everyItem(notNullValue()));
        /** clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((160 -2, -160 -2, -160 2, 160 2, 160 -2))")));
        request.filter.righthand = true;
        getWithRightHand = givenFilterableRequestParams()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 591, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand, 591, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 591, everyItem(notNullValue()));
        request.filter.righthand = false;


        /** west < east bbox along with a clock-wise WKT*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "-12,-12,12,12")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, 180 -70, 160 -70, 160 -90, -180 -90, -180 90, 180 90))")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notintersects", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));
        /** west < east bbox along with a counter clock-wise WKT with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "-12,-12,12,12")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(getWithRightHand,
                1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));
        request.filter.righthand = false;


        /** clock-wise wkts*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((12 12, 12 -12, -12 -12, -12 12, 12 12))"),
                new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, 180 -70, 160 -70, 160 -90,-180 -90,-180 90,180 90))"))));
        handleMatchingGeometryFilter(post(request), 586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 586, everyItem(notNullValue()));
        /** counter clock-wise wkts with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((12 12, -12 12, -12 -12, 12 -12, 12 12))"),
                new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((180 90, -180 90, -180 -90, 160 -90, 160 -70, 180 -70, 180 90))"))));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(getWithRightHand,
                586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 586, everyItem(notNullValue()));
        request.filter.righthand = false;

        /** clock-wise wkts*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((10 10,10 -10,0 -10,0 10,10 10))")));
        handleMatchingGeometryFilter(post(request), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams()
                        .param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then(), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(header(request.filter), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        /** counter clock-wise wkts with righthand=true*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 10, -10 10, -10 -10, 10 -10, 10 10))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((10 10,0 10,0 -10,10 -10,10 10))")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleMatchingGeometryFilter(post(request), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(getWithRightHand, 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(header(request.filter), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        request.filter.righthand = false;


        /** clock-wise wkts*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((11 11, 11 -11, -11 -11, -11 11, 11 11))")));
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams()
                        .param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", request.filter.f.get(1).get(0).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingGintersectComboFilter(header(request.filter));
        /** counter clock-wise wkts*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 10, -10 10, -10 -10, 10 -10, 10 10))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((11 11, -11 11, -11 -11, 11 -11, 11 11))")));
        request.filter.righthand = true;
        getWithRightHand = this.getExtraParamsRequest()
                .param("f", request.filter.f.get(0).get(0).toString())
                .param("f", request.filter.f.get(1).get(0).toString())
                .param("righthand", request.filter.righthand)
                .when().get(getUrlPath("geodata"))
                .then();
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(getWithRightHand);
        handleNotMatchingGintersectComboFilter(header(request.filter));
        request.filter.righthand = false;
        request.filter.f = null;
    }

    @Test
    public void testComplexFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));
        handleFieldFilter(post(request), 2, "Architect");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1009799<1009801]")
                        .when().get(getUrlPath("geodata"))
                        .then(),
                2, "Architect");
        handleFieldFilter(header(request.filter), 2, "Architect");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("text_search", OperatorEnum.like, "brown")),//"text_search:like:brown"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[840000<915200]")));
        handleTextFieldLikeFilter(post(request), 13, "Brownie");
        handleTextFieldLikeFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[840000<915200]")
                        .when().get(getUrlPath("geodata"))
                        .then(),
                13, "Brownie");
        handleTextFieldLikeFilter(header(request.filter), 13, "Brownie");


        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1970/01/01 00:16:49:799<1970/01/01 00:16:49:801]")));
        request.filter.dateformat = "yyyy/MM/dd HH:mm:ss:SSS";
        handleFieldFilter(post(request), 2, "Architect");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1970/01/01 00:16:49:799<1970/01/01 00:16:49:801]")
                        .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then(),
                2, "Architect");
        handleFieldFilter(header(request.filter), 2, "Architect");

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "01-01-1970 00:12:55")));
        request.filter.dateformat = "dd-MM-yyyy HH:mm:ss";
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("dateformat", request.filter.dateformat)
                        .when().get(getUrlPath("geodata")).then(),
                3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        // DATEFORMAT : check that date operations (||/s) works when specifying dates with custom format
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "01-01-1970 00:12:55||/s")));
        request.filter.dateformat = "dd-MM-yyyy HH:mm:ss";
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("dateformat", request.filter.dateformat)
                        .when().get(getUrlPath("geodata")).then(),
                3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        // DATEFORMAT : check that dateformat works when we specify an alias ($timestamp) in range operation
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[01-01-1970 00:00:00<01-01-1970 00:12:55||-3s/s[")));
        request.filter.dateformat = "dd-MM-yyyy HH:mm:ss";
        handleMatchingTimestampRangeFilter(post(request), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("dateformat", request.filter.dateformat)
                .when().get(getUrlPath("geodata")).then(), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 772000, 2);
        request.filter.dateformat = null;
        request.filter.righthand = false;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-50,-50,50,50")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "20,-50,60,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))")));
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1009799<1009801]")
                        .param("f", request.filter.f.get(2).get(0).toString())
                        .param("f", request.filter.f.get(3).get(0).toString())
                        .param("f", request.filter.f.get(4).get(0).toString())
                        .param("f", request.filter.f.get(5).get(0).toString())
                        .param("f", request.filter.f.get(6).get(0).toString())
                        .param("f", request.filter.f.get(7).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(header(request.filter));

        /** counter clock-wise with righthand=true */
        request.filter.righthand = true;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-50,-50,50,50")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "20,-50,60,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((30 30,-30 30,-30 -30,30 -30,30 30))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-50 50, -50 -50, -20 -50, -20 50, -50 50))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-20 20, -20 -20, 20 -20, 20 20, -20 20))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-30 -10, -30 -30, 30 -30, 30 10, -30 -10))")));
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams()
                        .param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1009799<1009801]")
                        .param("f", request.filter.f.get(2).get(0).toString())
                        .param("f", request.filter.f.get(3).get(0).toString())
                        .param("f", request.filter.f.get(4).get(0).toString())
                        .param("f", request.filter.f.get(5).get(0).toString())
                        .param("f", request.filter.f.get(6).get(0).toString())
                        .param("f", request.filter.f.get(7).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(header(request.filter));
        request.filter = new Filter();
        request.filter.righthand = false;

    }

    @Test
    public void testMultiPartitionFilter() throws Exception {
        List<MultiValueFilter<Expression>> fGroup1Filter = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-20 20, -20 -20, 20 -20, 20 20, -20 20))")));
        List<MultiValueFilter<Expression>> fGroup2Filter = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((30 20, 30 -20, 70 -20, 70 20, 30 20))")));
        Filter group1Filter = new Filter();
        group1Filter.f = fGroup1Filter;
        Filter group2Filter = new Filter();
        group2Filter.f = fGroup2Filter;
        String serializedFilter =  objectMapper.writeValueAsString(group1Filter) +"," +  objectMapper.writeValueAsString(group2Filter) ;
        handleMultiPartitionFilter(header(serializedFilter));
    }


    @Test
    public void testCollectionFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[1])));//);
        RequestSpecification req = givenFilterableRequestBody();
        handleNotMatchingRequest(
                req.body(handlePostRequest(request))
                        .when().post(getUrlPath("geodata_actor"))
                        .then()
        );
        req = givenFilterableRequestParams();
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.param(extraParam.getKey(), extraParam.getValue());
        }
        handleNotMatchingRequest(
                req.param("f", request.filter.f.get(0).get(0).toString())
                        .when().get(getUrlPath("geodata_actor"))
                        .then()
        );
        request.filter = new Filter();
        request.filter.righthand = false;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])));//("job:eq:" + DataSetTool.jobs[0]);
        req = givenFilterableRequestBody();

        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString()), 59, "Actor");
        handleFieldFilter(header(request.filter), 59, "Actor");


        handleFieldFilter(
                req.body(handlePostRequest(request))
                        .when().post(getUrlPath("geodata_actor"))
                        .then(),59,"Actor"
        );
        req = givenFilterableRequestParams();
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.param(extraParam.getKey(), extraParam.getValue());
        }
        handleFieldFilter(
                req.param("f", request.filter.f.get(0).get(0).toString())
                        .when().get(getUrlPath("geodata_actor"))
                        .then(),59,"Actor"
        );
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testMixedFilter() throws Exception {
        // valid bbox from WFS OGC SPEC = lower longitude , lower latitude , upper longitude  , upper latitude
        // valid bbox for ARLAS classic bbox = lat top,  long left,  lat bottom,  long right
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-50,-50,50,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-30,-30,30,30")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))")));
        request.filter.righthand = false;
        Filter filterHeader = new Filter();
        filterHeader.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<1009801]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "20,-50,60,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))")));
        filterHeader.righthand = false;
        handleComplexFilter(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "params.job,params.city,params.country")
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .param("f", new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]").toString())
                        .param("f", request.filter.f.get(2).get(0).toString())
                        .param("f", request.filter.f.get(3).get(0).toString())
                        .param("f", request.filter.f.get(4).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then());

        handleComplexFilter(
                post(
                        request,
                        PARTITION_FILTER,
                        objectMapper.writeValueAsString(filterHeader),
                        COLUMN_FILTER,
                        "params.job,params.city,params.country"));

        filterHeader.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Actor")));
        handleNotMatchingRequest(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingRequest(
                givenFilterableRequestBody().body(request)
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());
        handleUnavailableCollection(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "")
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleUnavailableCollection(
                givenFilterableRequestBody().body(request)
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "")
                        .when().post(getUrlPath("geodata"))
                        .then());
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testMixedFilterMultiCollectionFormat() throws Exception {
        // valid bbox from WFS OGC SPEC = lower longitude , lower latitude , upper longitude  , upper latitude
        // valid bbox for ARLAS classic bbox = lat top,  long left,  lat bottom,  long right
        request.filter.righthand = false;
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-50,-50,50,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "-30,-30,30,30")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))")));

        Map<String, Filter> filterHeader = new HashMap<String, Filter>();

        Filter filter = new Filter();
        filter.righthand = false;
        filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<1009801]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "20,-50,60,50")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))")),
                new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))")));

        filterHeader.put("geodata", filter);

        handleComplexFilter(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "params.job,params.city,params.country")
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .param("f", new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]").toString())
                        .param("f", request.filter.f.get(2).get(0).toString())
                        .param("f", request.filter.f.get(3).get(0).toString())
                        .param("f", request.filter.f.get(4).get(0).toString())
                        .param("righthand", request.filter.righthand)
                        .when().get(getUrlPath("geodata"))
                        .then());

        handleComplexFilter(
                post(
                        request,
                        PARTITION_FILTER,
                        objectMapper.writeValueAsString(filterHeader),
                        COLUMN_FILTER,
                        "params.job,params.city,params.country"));

        filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Actor")));
        handleNotMatchingRequest(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleUnavailableCollection(
                givenFilterableRequestParams()
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "")
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        filterHeader.put("geodata", filter);
        handleNotMatchingRequest(
                givenFilterableRequestBody().body(request)
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());
        handleUnavailableCollection(
                givenFilterableRequestBody().body(request)
                        .header(PARTITION_FILTER, objectMapper.writeValueAsString(filterHeader))
                        .header(COLUMN_FILTER, "")
                        .when().post(getUrlPath("geodata"))
                        .then());
        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testFieldFilterWithUnavailableColumns() throws Exception {
        request.filter = new Filter();
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Architect")));
        request.filter.righthand = false;
        handleUnavailableColumn(
                givenFilterableRequestParams()
                        .header(COLUMN_FILTER, "params.city")
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .when().get(getUrlPath("geodata"))
                        .then());

        handleUnavailableColumn(
                post(
                        request,
                        COLUMN_FILTER,
                        "params.city"));

        //also filter geometry filters
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.other_geopoint", OperatorEnum.within, "-5,-5,5,5")));
        handleUnavailableColumn(post(request, COLUMN_FILTER, "fullname"));
        handleUnavailableColumn(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "fullname"));

        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testFieldFilterWithCollectionBasedColumnFiltering() throws Exception {

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])));
        handleFieldFilter(post(request, COLUMN_FILTER, "params.job"), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "params.job"), 59, "Actor");

        handleFieldFilter(post(request, COLUMN_FILTER, COLLECTION_NAME + ":params.job"), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, COLLECTION_NAME + ":params.job"), 59, "Actor");

        handleUnavailableColumn(post(request, COLUMN_FILTER, "fullname,notExisting:params.job"));
        handleUnavailableColumn(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "fullname,notExisting:params.job"));

        handleUnavailableCollection(post(request, COLUMN_FILTER, "notExisting:params.job"));
        handleUnavailableCollection(get("f", request.filter.f.get(0).get(0).toString(), COLUMN_FILTER, "notExisting:params.job"));

        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testQueryFilterWithUnavailableColumns() throws Exception {
        request.filter = new Filter();
        request.filter.righthand = false;
        request.filter.q = Arrays.asList(new MultiValueFilter<>("My name is"));
        handleUnavailableColumn(post(request, COLUMN_FILTER, "params.city"));
        handleUnavailableColumn(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "params.city"));

        request.filter.q = Arrays.asList(new MultiValueFilter<>("*ullnam*:My name is"));
        handleUnavailableColumn(post(request, COLUMN_FILTER, "fullname"));
        handleUnavailableColumn(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "fullname"));

        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name:is"));
        handleUnavailableColumn(post(request, COLUMN_FILTER, "params.city"));
        handleUnavailableColumn(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "params.city"));

        //used to return 200 in previous implementation, this is anti-regression
        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name:is"));
        handleUnavailableColumn(post(request, COLUMN_FILTER, "fullname.anything"));
        handleUnavailableColumn(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "fullname.anything"));

        request.filter = new Filter();
        request.filter.righthand = false;
    }

    @Test
    public void testQueryFilterWithCollectionBasedColumnFiltering() throws Exception {
        request.filter = new Filter();
        request.filter.righthand = false;
        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name is"));

        handleMatchingQueryFilter(post(request, COLUMN_FILTER, "fullname*"), 595);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "fullname*"), 595);

        handleMatchingQueryFilter(post(request, COLUMN_FILTER, COLLECTION_NAME + ":fullname*"), 595);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, COLLECTION_NAME + ":fullname*"), 595);

        handleUnavailableColumn(post(request, COLUMN_FILTER, "params,notExisting:fullname*"));
        handleUnavailableColumn(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "params,notExisting:fullname*"));

        handleUnavailableCollection(post(request, COLUMN_FILTER, "notExisting:fullname*"));
        handleUnavailableCollection(get("q", request.filter.q.get(0).get(0), COLUMN_FILTER, "notExisting:fullname*"));
    }

    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])),//"job:eq:" + DataSetTool.jobs[0]
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1000000<2000000]")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "10,10,-10,-10")),
                new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "5,5,-5,-5")));
        handleNotFoundCollection(
                givenFilterableRequestBody().body(request)
                        .when().post(getUrlPath("unknowncollection"))
                        .then());
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", request.filter.f)
                        .when().get(getUrlPath("unknowncollection"))
                        .then());
        request.filter.f = null;
    }

    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("foobar", null, null)));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000],")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.gte, "775000.0")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.gte, "now-")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;



        //PWITHIN
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-5,5,5,-5")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "foo")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "-5,5,5,-5")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.notwithin, "foo")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        //GWITHIN
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((10 10,10 -10,0 -10))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        /** Right oriented polygon that cannot be drawn on the other facet of the globe*/
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "230,10,100,-10")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "foo")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.within, "POLYGON((1000 10000,10 -10,0 -10,1000 10000))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((10 10,10 -10,0 -10))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, ("foo"))));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notwithin, "POLYGON((10 10,10 -10,0 -10))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        //GINTERSECT
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((10 10,10 -10,0 -10))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "foo")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "POLYGON((1000 10000,10 -10,0 -10,1000 10000))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((10 10,10 -10,0 -10))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "foo")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.notintersects, "POLYGON((1000 10000,10 -10,0 -10,1000 10000))")));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));

        // DATEFORMAT : format not matching the given date
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "01-01-1970 00:12:55")));
        request.filter.dateformat = "dd-MM-yyyy";
        handleInvalidParameters(post(request));
        handleInvalidParameters(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then());
        handleInvalidParameters(header(request.filter));

        // DATEFORMAT : format containing `||`
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "01-01||1970 ||-1h")));
        request.filter.dateformat = "dd-MM||yyyy";
        handleInvalidParameters(post(request));
        handleInvalidParameters(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then());
        handleInvalidParameters(header(request.filter));

        // DATEFORMAT : format set when no date field is queried
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")));
        request.filter.dateformat = "dd-MM-yyyy";
        handleInvalidParameters(post(request));
        handleInvalidParameters(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then());
        handleInvalidParameters(header(request.filter));
    }

    @Test
    public void testMultiGeometriesFilter() throws Exception {
        // geometry intersects AND second_geometry within : match found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,0,0")),
                new MultiValueFilter<>(new Expression("geo_params.second_geometry", OperatorEnum.within, "3,3,10,10")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        // geometry intersects AND second_geometry within : match NOT found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,-10,-10")),
                new MultiValueFilter<>(new Expression("geo_params.second_geometry", OperatorEnum.within, "3,3,10,10")));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))));
        handleNotMatchingRequest(header(request.filter));

        // geometry intersects OR second_geometry within : match found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,-10,-10"),
                new Expression("geo_params.second_geometry", OperatorEnum.within, "3,3,10,10"))));
        handleMatchingGeometryFilter(post(request), 2, hasItems("0,0", "-10,-10"));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                2, hasItems("0,0", "-10,-10"));
        handleMatchingGeometryFilter(header(request.filter), 2, hasItems("0,0", "-10,-10"));

        // centroid within AND other_geopoint within : match found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-12,-12,0,0")),
                new MultiValueFilter<>(new Expression("geo_params.other_geopoint", OperatorEnum.within, "3,3,10,10")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        // centroid within AND other_geopoint within : match NOT found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.centroid", OperatorEnum.within, "-12,-12,-10,-10")),
                new MultiValueFilter<>(new Expression("geo_params.other_geopoint", OperatorEnum.within, "3,3,10,10")));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))));
        handleNotMatchingRequest(header(request.filter));

        // centroid within OR other_geopoint within : match found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(Arrays.asList(new Expression("geo_params.centroid", OperatorEnum.within, "-12,-12,-10,-10"),
                new Expression("geo_params.other_geopoint", OperatorEnum.within, "3,3,10,10"))));
        handleMatchingGeometryFilter(post(request), 2, hasItems("0,0", "-10,-10"));
        handleMatchingGeometryFilter(
                get("f", request.filter.f.get(0).get(0).toString() + ";" + request.filter.f.get(0).get(1).toString()),
                2, hasItems("0,0", "-10,-10"));
        handleMatchingGeometryFilter(header(request.filter), 2, hasItems("0,0", "-10,-10"));

        // geometry intersects AND other_geopoint within : match found
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("geo_params.geometry", OperatorEnum.intersects, "-12,-12,0,0")),
                new MultiValueFilter<>(new Expression("geo_params.other_geopoint", OperatorEnum.within, "3,3,10,10")));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).get(0).toString()),
                        new ImmutablePair<>("f", request.filter.f.get(1).get(0).toString()))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));
    }


    //----------------------------------------------------------------
    //----------------------- COMMON BEHAVIORS -----------------------
    //----------------------------------------------------------------
    protected void handleNotFoundCollection(ValidatableResponse then) throws Exception {
        then.statusCode(404);
    }

    protected void handleNotFoundField(ValidatableResponse then) throws Exception {
        then.statusCode(404);
    }

    protected void handleInvalidParameters(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    protected void handleNotImplementedParameters(ValidatableResponse then) throws Exception {
        then.statusCode(501);
    }


    protected abstract void handleNotMatchingRequest(ValidatableResponse then);

    protected void handleUnavailableColumn(ValidatableResponse then) {
        then.statusCode(403).body(stringContainsInOrder(Arrays.asList("column", "available")));
    }

    protected void handleUnavailableCollection(ValidatableResponse then) {
        then.statusCode(403).body(stringContainsInOrder(Arrays.asList("collection", "available")));
    }

    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract RequestSpecification givenFilterableRequestParams();

    protected abstract RequestSpecification givenFilterableRequestBody();

    protected abstract void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception;

    protected abstract void handleTextFieldLikeFilter(ValidatableResponse then, int nbResults, String searchedText) throws Exception;

    protected abstract void handleFieldFilter(ValidatableResponse then, int nbResults) throws Exception;

    protected abstract void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception;

    protected abstract void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end,
                                                               int size) throws Exception;

    protected abstract void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end,
                                                            int size) throws Exception;

    protected abstract void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception;

    protected abstract void handleComplexFilter(ValidatableResponse then) throws Exception;

    protected abstract void handleMultiPartitionFilter(ValidatableResponse then) throws Exception;


    //----------------------------------------------------------------
    //---------------------- NOT MATCHING RESPONSES ------------------
    //----------------------------------------------------------------
    protected void handleUnknownFieldFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingQueryFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingRange(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    protected void handleNotMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        handleNotMatchingRequest(then);
    }

    //----------------------------------------------------------------
    //---------------------- ValidatableResponse ------------------
    //----------------------------------------------------------------

    private RequestSpecification getExtraParamsRequest() {
        RequestSpecification req = givenFilterableRequestParams();
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.param(extraParam.getKey(), extraParam.getValue());
        }
        return req;
    }
    private ValidatableResponse get(String param, Object paramValue) {
        RequestSpecification req = getExtraParamsRequest();
        return req.param(param, paramValue)
                .param("righthand", false)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String param, Object paramValue, String headerkey, String headerValue) {
        RequestSpecification req = getExtraParamsRequest();
        return req.param(param, paramValue)
                .param("righthand", false)
                .header(headerkey, headerValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(List<Pair<String, Object>> params) {
        RequestSpecification req = getExtraParamsRequest();
        for (Pair<String, Object> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }
        return req.param("righthand", false)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse header(String serializedFilter) throws JsonProcessingException {
        return givenFilterableRequestParams().header(PARTITION_FILTER, serializedFilter)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse header(Filter filter) throws JsonProcessingException {
        return givenFilterableRequestParams().header(PARTITION_FILTER, objectMapper.writeValueAsString(filter))
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse header(Filter filter, String headerKey, String headerValue) throws JsonProcessingException {
        return givenFilterableRequestParams()
                .header(PARTITION_FILTER, objectMapper.writeValueAsString(filter))
                .header(headerKey, headerValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }
    protected Request handlePostRequest(Request req) {
        return req;
    }

    private ValidatableResponse post(Request request, String headerkey, String headerValue) {
        return givenFilterableRequestBody().body(handlePostRequest(request))
                .header(headerkey, headerValue)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse post(Request request, String headerkey1, String headerValue1, String headerkey2, String headerValue2) {
        return givenFilterableRequestBody().body(handlePostRequest(request))
                .header(headerkey1, headerValue1)
                .header(headerkey2, headerValue2)
                .when().post(getUrlPath("geodata"))
                .then();
    }

}
