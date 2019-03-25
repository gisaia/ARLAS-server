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

package io.arlas.server.rest.explore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.DataSetTool;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;

public abstract class AbstractFilteredTest extends AbstractTestWithCollection {

    private static ObjectMapper objectMapper = new ObjectMapper();
    @Before
    public void setUpFilter() {
        request = new Request();
        request.filter = new Filter();
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

        request.filter.q = Arrays.asList(new MultiValueFilter<>("fullname:My name is"));
        handleMatchingQueryFilter(post(request), 595);
        handleMatchingQueryFilter(get("q", request.filter.q.get(0).get(0)), 595);
        handleMatchingQueryFilter(header(request.filter), 595);

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
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-5,-5,5,5"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("pwithin", request.filter.pwithin.get(0).get(0)), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("180,0,-165,5"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(get("pwithin", request.filter.pwithin.get(0).get(0)), 1, everyItem(equalTo("0,-170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,-170")));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("175,85,180,90"));
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("pwithin", request.filter.pwithin.get(0).get(0)));
        handleNotMatchingPwithinFilter(header(request.filter));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-5,-50,180,50"), new MultiValueFilter<>("-180,-50,5,50"));
        handleMatchingGeometryFilter(post(request), 10, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("pwithin", request.filter.pwithin.get(0).get(0)),
                        new ImmutablePair<>("pwithin", request.filter.pwithin.get(1).get(0)))),
                10, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 10, everyItem(endsWith("0")));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-5,-5,5,5", "5,5,15,15")));
        handleMatchingGeometryFilter(post(request), 2, everyItem(isOneOf("0,0", "10,10")));
        handleMatchingGeometryFilter(get("pwithin", request.filter.pwithin.get(0).get(0) + ";" + request.filter.pwithin.get(0).get(1)), 2, everyItem(isOneOf("0,0", "10,10")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(isOneOf("0,0", "10,10")));

        request.filter.pwithin = null;
        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-170,-85,175,85"));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(get("notpwithin", request.filter.notpwithin.get(0).get(0)), 17, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("170")));

        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-175,-85,175,85"));
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("notpwithin", request.filter.notpwithin.get(0).get(0)));
        handleNotMatchingNotPwithinFilter(header(request.filter));

        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-180,-90,-5,90"), new MultiValueFilter<>("5,-90,180,90"));
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notpwithin", request.filter.notpwithin.get(0).get(0)),
                        new ImmutablePair<>("notpwithin", request.filter.notpwithin.get(1).get(0)))),
                17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("0")));

        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-180,-90,-5,90", "5,-90,180,90")));
        handleMatchingGeometryFilter(post(request), 595, everyItem(notNullValue()));
        handleMatchingGeometryFilter(get("notpwithin", request.filter.notpwithin.get(0).get(0) + ";" + request.filter.notpwithin.get(0).get(1)), 595, notNullValue());
        handleMatchingGeometryFilter(header(request.filter), 595, everyItem(notNullValue()));

        //TODO support correct 10,-10,-10,10 bounding box
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-11,-11,11,11"));
        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-5,-5,5,5"));
        handleMatchingGeometryFilter(post(request), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin.get(0).get(0))
                        .param("notpwithin", request.filter.notpwithin.get(0))
                        .when().get(getUrlPath("geodata"))
                        .then(), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(header(request.filter), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-6,-6,6,6"));
        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-5,-5,5,5"));
        handleNotMatchingPwithinComboFilter(post(request));
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin.get(0).get(0))
                        .param("notpwithin", request.filter.notpwithin.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingPwithinComboFilter(header(request.filter));

        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }

    @Test
    public void testGwithinFilter() throws Exception {
        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("-2,-2,2,2"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("gwithin", request.filter.gwithin.get(0).get(0)), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((1 1,2 1,2 2,1 2,1 1))"));
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("gwithin", request.filter.gwithin.get(0).get(0)));
        handleNotMatchingGwithinFilter(header(request.filter));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((1 1,10 -20,-10 -20,-10 20,1 1))"), new MultiValueFilter<>("-2,-2,2,2"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("gwithin", request.filter.gwithin.get(0).get(0)),
                        new ImmutablePair<>("gwithin", request.filter.gwithin.get(1).get(0)))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-2,-2,2,2", "POLYGON((1 1,10 -20,-10 -20,-10 20,1 1))")));
        handleMatchingGeometryFilter(post(request), 2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(
                get("gwithin", request.filter.gwithin.get(0).get(0) + ";" + request.filter.gwithin.get(0).get(1)),
                2, everyItem(isOneOf("-10,0", "0,0")));
        handleMatchingGeometryFilter(header(request.filter), 2, everyItem(isOneOf("-10,0", "0,0")));
        request.filter.gwithin = null;

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))"));
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(get("notgwithin", request.filter.notgwithin.get(0).get(0)), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))"));
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(get("notgwithin", request.filter.notgwithin.get(0).get(0)));
        handleNotMatchingNotGwithinFilter(header(request.filter));

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -80,100 -80,100 -70,180 -70,180 90))"), new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))"));
        handleMatchingGeometryFilter(post(request), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notgwithin", request.filter.notgwithin.get(0).get(0)),
                        new ImmutablePair<>("notgwithin", request.filter.notgwithin.get(1).get(0)))),
                4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));
        handleMatchingGeometryFilter(header(request.filter), 4, hasItems("-70,170", "-80,170", "-70,160", "-80,160"));

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>(Arrays.asList("POLYGON((180 90,-180 90,-180 -80,99 -80,99 -68,180 -68,180 90))", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")));
        handleMatchingGeometryFilter(post(request), 43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("notgwithin", request.filter.notgwithin.get(0).get(0) + ";" + request.filter.notgwithin.get(0).get(1)),
                43, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 43, everyItem(notNullValue()));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))"));
        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("-8,-8,8,8"));
        handleMatchingGeometryFilter(post(request), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin.get(0).get(0))
                        .param("notgwithin", request.filter.notgwithin.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then(), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));
        handleMatchingGeometryFilter(header(request.filter), 8, hasItems("10,0", "10,-10", "10,10", "10,10", "10,0", "10,-10", "0,10", "0,-10"));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("-12,-12,12,12"));
        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("-11,-11,11,11"));
        handleNotMatchingGwithinComboFilter(post(request));
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin.get(0).get(0))
                        .param("notgwithin", request.filter.notgwithin.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingGwithinComboFilter(header(request.filter));
        request.filter.gwithin = null;
        request.filter.notgwithin = null;
    }

    @Test
    public void testGintersectFilter() throws Exception {
        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((0 1,1 1,1 -1,0 -1,0 1))"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("gintersect", request.filter.gintersect.get(0).get(0)), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((2 2,3 2,3 3,2 3,2 2))"));
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(get("gintersect", request.filter.gintersect.get(0).get(0)));
        handleNotMatchingGintersectFilter(header(request.filter));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("-12,-12,12,12"), new MultiValueFilter<>("0,-1,1,1"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("gintersect", request.filter.gintersect.get(0).get(0)),
                        new ImmutablePair<>("gintersect", request.filter.gintersect.get(1).get(0)))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>(Arrays.asList("-12,-12,12,12", "POLYGON((0 1,1 1,1 -1,0 -1,0 1))")));
        handleMatchingGeometryFilter(post(request), 9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("gintersect", request.filter.gintersect.get(0).get(0) + ";" + request.filter.gintersect.get(0).get(1)),
                9, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 9, everyItem(notNullValue()));
        request.filter.gintersect = null;

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(get("notgintersect", request.filter.notgintersect.get(0).get(0)), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))"));
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect.get(0).get(0)));
        handleNotMatchingNotGintersectFilter(header(request.filter));

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("-12,-12,12,12"), new MultiValueFilter<>("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))"));
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notgintersect", request.filter.notgintersect.get(0).get(0)),
                        new ImmutablePair<>("notgintersect", request.filter.notgintersect.get(1).get(0)))),
                1, everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("-80,170")));

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>(Arrays.asList("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))", "POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))")));
        handleMatchingGeometryFilter(post(request), 586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(
                get("notgintersect", request.filter.notgintersect.get(0).get(0) + ";" + request.filter.notgintersect.get(0).get(1)),
                586, everyItem(notNullValue()));
        handleMatchingGeometryFilter(header(request.filter), 586, everyItem(notNullValue()));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))"));
        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10,0 10,10 10))"));
        handleMatchingGeometryFilter(post(request), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect.get(0).get(0))
                        .param("notgintersect", request.filter.notgintersect.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then(), 3, hasItems("10,-10", "0,-10", "-10,-10"));
        handleMatchingGeometryFilter(header(request.filter), 3, hasItems("10,-10", "0,-10", "-10,-10"));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))"));
        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))"));
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect.get(0).get(0))
                        .param("notgintersect", request.filter.notgintersect.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingGintersectComboFilter(header(request.filter));
        request.filter.gintersect = null;
        request.filter.notgintersect = null;

    }

    @Test
    public void testComplexFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));
        handleFieldFilter(post(request), 2, "Architect");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1009799<1009801]").when().get(getUrlPath("geodata"))
                        .then(),
                2, "Architect");
        handleFieldFilter(header(request.filter), 2, "Architect");

        //
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
                .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then(),
                3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        // DATEFORMAT : check that date operations (||/s) works when specifying dates with custom format
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.lte, "01-01-1970 00:12:55||/s")));
        request.filter.dateformat = "dd-MM-yyyy HH:mm:ss";
        handleFieldFilter(post(request), 3, "Chemist", "Brain Scientist");
        handleFieldFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then(),
                3, "Chemist", "Brain Scientist");
        handleFieldFilter(header(request.filter), 3, "Chemist", "Brain Scientist");

        // DATEFORMAT : check that dateformat works when we specify an alias ($timestamp) in range operation
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("$timestamp", OperatorEnum.range, "[01-01-1970 00:00:00<01-01-1970 00:12:55||-3s/s[")));
        request.filter.dateformat = "dd-MM-yyyy HH:mm:ss";
        handleMatchingTimestampRangeFilter(post(request), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                .param("dateformat", request.filter.dateformat).when().get(getUrlPath("geodata")).then(), 0, 772000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 772000, 2);
        request.filter.dateformat = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.like, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<1009801]")));
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-50,-50,50,50"));
        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("20,-50,60,50"));
        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))"));
        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))"));
        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))"));
        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))"));
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).get(0).toString())
                        .param("f", "params.startdate:range:[1009799<1009801]")
                        .param("pwithin", request.filter.pwithin.get(0).get(0))
                        .param("notpwithin", request.filter.notpwithin.get(0).get(0))
                        .param("gwithin", request.filter.gwithin.get(0).get(0))
                        .param("notgwithin", request.filter.notgwithin.get(0).get(0))
                        .param("gintersect", request.filter.gintersect.get(0).get(0))
                        .param("notgintersect", request.filter.notgintersect.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(header(request.filter));
        request.filter = new Filter();
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
    }

    @Test
    public void testMixedFilter() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Architect")),//"job:eq:Architect"
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]")));
        // valid bbox from WFS OGC SPEC = lower longitude , lower latitude , upper longitude  , upper latitude
        // valid bbox for ARLAS classic bbox = lat top,  long left,  lat bottom,  long right
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-50,-50,50,50"));
        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("-30,-30,30,30"));
        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))"));

        Filter filterHeader = new Filter();
        filterHeader.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<1009801]")));
        filterHeader.notpwithin = Arrays.asList(new MultiValueFilter<>("20,-50,60,50"));
        filterHeader.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))"));
        filterHeader.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))"));
        handleComplexFilter(
                givenFilterableRequestParams()
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .param("f", new Expression("params.startdate", OperatorEnum.range, "[1009799<2000000]").toString())
                        .param("pwithin", request.filter.pwithin.get(0).get(0))
                        .param("gwithin", request.filter.gwithin.get(0).get(0))
                        .param("gintersect", request.filter.gintersect.get(0).get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(
                post(request,"partition-filter", objectMapper.writeValueAsString(filterHeader)));

        filterHeader.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, "Actor")));
        handleNotMatchingRequest(
                givenFilterableRequestParams().header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .param("f", (new Expression("params.job", OperatorEnum.eq, "Architect")).toString())
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleNotMatchingRequest(
                givenFilterableRequestBody().body(request)
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());
        request.filter = new Filter();
    }

    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testNotFoundCollection() throws Exception {
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])),//"job:eq:" + DataSetTool.jobs[0]
                new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[1000000<2000000]")));
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("10,10,-10,-10"));
        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("5,5,-5,-5"));
        handleNotFoundCollection(
                givenFilterableRequestBody().body(request)
                        .when().post(getUrlPath("unknowncollection"))
                        .then());
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", request.filter.f)
                        .param("pwithin", request.filter.pwithin.get(0).get(0))
                        .param("notpwithin", request.filter.notpwithin.get(0).get(0))
                        .when().get(getUrlPath("unknowncollection"))
                        .then());
        request.filter.f = null;
        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }

    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("foobar", null, null)));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.range, "[0<775000],")));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.gte, "775000.0")));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new MultiValueFilter<>(new Expression("params.startdate", OperatorEnum.gte, "now-")));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;



        //PWITHIN
        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("-5,5,5,-5"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin", request.filter.pwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.pwithin = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin", request.filter.pwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.pwithin = null;

        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("-5,5,5,-5"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin", request.filter.notpwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notpwithin = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin", request.filter.notpwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notpwithin = null;

        //GWITHIN
        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin", request.filter.gwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));


        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("230,10,100,-10"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin", request.filter.gwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin", request.filter.gwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gwithin = null;

        request.filter.gwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((1000 10000,10 -10,0 -10,1000 10000))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin", request.filter.gwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gwithin = null;

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin", request.filter.notgwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin", request.filter.notgwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgwithin = null;

        request.filter.notgwithin = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin", request.filter.notgwithin.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgwithin = null;

        //GINTERSECT
        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect", request.filter.gintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect", request.filter.gintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gintersect = null;

        request.filter.gintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((1000 10000,10 -10,0 -10,1000 10000))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect", request.filter.gintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gintersect = null;

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((10 10,10 -10,0 -10))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect", request.filter.notgintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("foo"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect", request.filter.notgintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgintersect = null;

        request.filter.notgintersect = Arrays.asList(new MultiValueFilter<>("POLYGON((1000 10000,10 -10,0 -10,1000 10000))"));
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect", request.filter.notgintersect.get(0).get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgintersect = null;

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

    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract RequestSpecification givenFilterableRequestParams();

    protected abstract RequestSpecification givenFilterableRequestBody();

    protected abstract void handleFieldFilter(ValidatableResponse then, int nbResults, String... values) throws Exception;
    protected abstract void handleFieldFilter(ValidatableResponse then, int nbResults) throws Exception;

    protected abstract void handleMatchingQueryFilter(ValidatableResponse then, int nbResults) throws Exception;

    protected abstract void handleMatchingTimestampRangeFilter(ValidatableResponse then, int start, int end,
                                                               int size) throws Exception;

    protected abstract void handleMatchingStringRangeFilter(ValidatableResponse then, String start, String end,
                                                            int size) throws Exception;

    protected abstract void handleMatchingGeometryFilter(ValidatableResponse then, int nbResults, Matcher<?> centroidMatcher) throws Exception;

    protected abstract void handleComplexFilter(ValidatableResponse then) throws Exception;


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

    private ValidatableResponse get(String param, Object paramValue) {
        RequestSpecification req = givenFilterableRequestParams();
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.param(extraParam.getKey(), extraParam.getValue());
        }
        return req.param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(List<Pair<String, String>> params) {
        RequestSpecification req = givenFilterableRequestParams();
        for (Pair<String, String> extraParam : this.extraParams) {
            req = req.param(extraParam.getKey(), extraParam.getValue());
        }
        for (Pair<String, String> param : params) {
            req = req.param(param.getKey(), param.getValue());
        }
        return req
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse header(Filter filter) throws JsonProcessingException {
        return givenFilterableRequestParams().header("Partition-Filter", objectMapper.writeValueAsString(filter))
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse post(Request request) {
        RequestSpecification req = givenFilterableRequestBody();
        return req.body(handlePostRequest(request))
                .when().post(getUrlPath("geodata"))
                .then();
    }
    protected Request handlePostRequest(Request req){return req;}

    private ValidatableResponse post(Request request, String headerkey, String headerValue) {
        return givenFilterableRequestBody().body(handlePostRequest(request))
                .header(headerkey, headerValue)
                .when().post(getUrlPath("geodata"))
                .then();
    }


}
