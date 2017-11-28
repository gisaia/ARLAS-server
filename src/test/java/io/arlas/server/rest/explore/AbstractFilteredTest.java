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
import io.arlas.server.model.request.Expression;
import io.arlas.server.model.request.Filter;
import io.arlas.server.model.request.OperatorEnum;
import io.arlas.server.model.request.Request;
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
    public void setUpFilter(){
        request = new Request();
        request.filter = new Filter();
    }
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testFieldFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0]));//("job:eq:" + DataSetTool.jobs[0]);
        handleFieldFilter(post(request), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).toString()), 59, "Actor");
        handleFieldFilter(header(request.filter), 59, "Actor");

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]));//"job:eq:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleFieldFilter(post(request), 117,"Actor","Announcers");
        handleFieldFilter(get("f", request.filter.f.get(0).toString()),117,"Actor","Announcers");
        handleFieldFilter(header(request.filter),117,"Actor","Announcers");

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.like, "cto"));//"job:like:" + "cto");
        handleFieldFilter(post(request), 59, "Actor");
        handleFieldFilter(get("f", request.filter.f.get(0).toString()), 59, "Actor");
        handleFieldFilter(header(request.filter), 59, "Actor");

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.ne, DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]));//"job:ne:" + DataSetTool.jobs[0] + "," + DataSetTool.jobs[1]);
        handleFieldFilter(post(request), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");
        handleFieldFilter(get("f", request.filter.f.get(0).toString()), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");
        handleFieldFilter(header(request.filter), 478, "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter");

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "UnknownJob"));//"job:eq:UnknownJob");
        handleUnknownFieldFilter(post(request));
        handleUnknownFieldFilter(get("f", request.filter.f.get(0).toString()));
        handleUnknownFieldFilter(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Actor"),new Expression("params.job", OperatorEnum.eq, "Announcers"));
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(
                get(Arrays.asList(new ImmutablePair<>("f", request.filter.f.get(0).toString()),new ImmutablePair<>("f", request.filter.f.get(1).toString()))));
        handleNotMatchingRequest(header(request.filter));

        request.filter.f = null;

    }
    
    @Test
    public void testQueryFilter() throws Exception {

        request.filter.q = Arrays.asList("My name is");
        handleMatchingQueryFilter(post(request));
        handleMatchingQueryFilter(get("q", request.filter.q.get(0)));
        handleMatchingQueryFilter(header(request.filter));

        request.filter.q = Arrays.asList("fullname:My name is");
        handleMatchingQueryFilter(post(request));
        handleMatchingQueryFilter(get("q", request.filter.q.get(0)));
        handleMatchingQueryFilter(header(request.filter));

        request.filter.q = Arrays.asList("UnknownQuery");
        handleNotMatchingQueryFilter(post(request));
        handleNotMatchingQueryFilter(get("q", request.filter.q.get(0)));
        handleNotMatchingQueryFilter(header(request.filter));

        request.filter.q = Arrays.asList("fullname:My name is","foo");
        handleNotMatchingRequest(post(request));
        handleNotMatchingRequest(get(Arrays.asList(new ImmutablePair<>("q", request.filter.q.get(0)),
                new ImmutablePair<>("q", request.filter.q.get(1)))));
        handleNotMatchingRequest(header(request.filter));

        request.filter.q = null;
    }

    @Test
    public void testRangeFilter() throws Exception {

        // TIMESTAMP RANGE
        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        //ALIAS
        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[0;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[770000;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 770000, 775000, 2);
        handleMatchingTimestampRangeFilter(header(request.filter), 770000, 775000, 2);

        //MULTIRANGE
        request.filter.f = Arrays.asList(new Expression("$timestamp", OperatorEnum.range, "[0;765000],[770000;775000]"));
        handleMatchingTimestampRangeFilter(post(request), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(get("f", request.filter.f.get(0).toString()), 0, 775000, 3);
        handleMatchingTimestampRangeFilter(header(request.filter), 0, 775000, 3);

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[1270000;1283600]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[765000;770000],[1270000;1283600]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        // TEXT RANGE
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Ac;An]"));
        handleMatchingStringRangeFilter(post(request), "Ac", "An", 59);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Ac", "An", 59);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "An", 59);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Coa;Cod]"));
        handleMatchingStringRangeFilter(post(request), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Coa", "Cod", 58);
        handleMatchingStringRangeFilter(header(request.filter), "Coa", "Cod", 58);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[Coa;Cod],[Ac;An]"));
        handleMatchingStringRangeFilter(post(request), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(get("f", request.filter.f.get(0).toString()), "Ac", "Cod", 117);
        handleMatchingStringRangeFilter(header(request.filter), "Ac", "Cod", 117);

        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.range, "[X;Z]"));
        handleNotMatchingRange(post(request));
        handleNotMatchingRange(get("f", request.filter.f.get(0).toString()));
        handleNotMatchingRange(header(request.filter));

        request.filter.f = null;
    }
    
    @Test
    public void testPwithinFilter() throws Exception {
        request.filter.pwithin = Arrays.asList("5,-5,-5,5");
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("pwithin",request.filter.pwithin.get(0)), 1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo("0,0")));

        request.filter.pwithin = Arrays.asList("5,180,0,-165");
        handleMatchingGeometryFilter(post(request), 1, everyItem(equalTo( "0,-170")));
        handleMatchingGeometryFilter(get("pwithin",request.filter.pwithin.get(0)), 1, everyItem(equalTo( "0,-170")));
        handleMatchingGeometryFilter(header(request.filter), 1, everyItem(equalTo( "0,-170")));

        request.filter.pwithin = Arrays.asList("90,175,85,180");
        handleNotMatchingPwithinFilter(post(request));
        handleNotMatchingPwithinFilter(get("pwithin",request.filter.pwithin.get(0)));
        handleNotMatchingPwithinFilter(header(request.filter));

        request.filter.pwithin = Arrays.asList("50,-5,-50,180", "50,-180,-50,5");
        handleMatchingGeometryFilter(post(request),10, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("pwithin", request.filter.pwithin.get(0)),
                        new ImmutablePair<>("pwithin", request.filter.pwithin.get(1)))),
                10, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter),10, everyItem(endsWith("0")));

        request.filter.pwithin = null;
        request.filter.notpwithin = Arrays.asList("85,-170,-85,175");
        handleMatchingGeometryFilter(post(request), 17, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(get("notpwithin",request.filter.notpwithin.get(0)), 17, everyItem(endsWith("170")));
        handleMatchingGeometryFilter(header(request.filter), 17, everyItem(endsWith("170")));

        request.filter.notpwithin = Arrays.asList("85,-175,-85,175");
        handleNotMatchingNotPwithinFilter(post(request));
        handleNotMatchingNotPwithinFilter(get("notpwithin",request.filter.notpwithin.get(0)));
        handleNotMatchingNotPwithinFilter(header(request.filter));

        request.filter.notpwithin = Arrays.asList("90,-180,-90,-5", "90,5,-90,180");
        handleMatchingGeometryFilter(post(request),17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notpwithin", request.filter.notpwithin.get(0)),
                        new ImmutablePair<>("notpwithin", request.filter.notpwithin.get(1)))),
                17, everyItem(endsWith("0")));
        handleMatchingGeometryFilter(header(request.filter),17, everyItem(endsWith("0")));

        //TODO support correct 10,-10,-10,10 bounding box
        request.filter.pwithin = Arrays.asList("11,-11,-11,11");
        request.filter.notpwithin = Arrays.asList("5,-5,-5,5");
        handleMatchingGeometryFilter(post(request),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin.get(0))
                    .param("notpwithin", request.filter.notpwithin.get(0))
                .when().get(getUrlPath("geodata"))
                .then(),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
        handleMatchingGeometryFilter(header(request.filter),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));

        request.filter.pwithin = Arrays.asList("6,-6,-6,6");
        request.filter.notpwithin = Arrays.asList("5,-5,-5,5");
        handleNotMatchingPwithinComboFilter(post(request));
        handleNotMatchingPwithinComboFilter(
                givenFilterableRequestParams().param("pwithin", request.filter.pwithin.get(0))
                    .param("notpwithin", request.filter.notpwithin.get(0))
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingPwithinComboFilter(header(request.filter));

        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testGwithinFilter() throws Exception {
        request.filter.gwithin = Arrays.asList("POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))");
        handleMatchingGeometryFilter(post(request),1,everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("gwithin",request.filter.gwithin.get(0)),1,everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter),1,everyItem(equalTo("0,0")));

        request.filter.gwithin = Arrays.asList("POLYGON((1 1,2 1,2 2,1 2,1 1))");
        handleNotMatchingGwithinFilter(post(request));
        handleNotMatchingGwithinFilter(get("gwithin",request.filter.gwithin.get(0)));
        handleNotMatchingGwithinFilter(header(request.filter));

        request.filter.gwithin = Arrays.asList("POLYGON((1 1,10 -20,-10 -20,-10 20,1 1))","POLYGON((2 2,2 -2,-2 -2,-2 2,2 2))");
        handleMatchingGeometryFilter(post(request),1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("gwithin", request.filter.gwithin.get(0)),
                        new ImmutablePair<>("gwithin", request.filter.gwithin.get(1)))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter),1, everyItem(equalTo("0,0")));
        request.filter.gwithin = null;

        request.filter.notgwithin = Arrays.asList("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))");
        handleMatchingGeometryFilter(post(request),4,hasItems("-70,170","-80,170","-70,160","-80,160"));
        handleMatchingGeometryFilter(get("notgwithin",request.filter.notgwithin.get(0)),4,hasItems("-70,170","-80,170","-70,160","-80,160"));
        handleMatchingGeometryFilter(header(request.filter),4,hasItems("-70,170","-80,170","-70,160","-80,160"));

        request.filter.notgwithin = Arrays.asList("POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))");
        handleNotMatchingNotGwithinFilter(post(request));
        handleNotMatchingNotGwithinFilter(get("notgwithin",request.filter.notgwithin.get(0)));
        handleNotMatchingNotGwithinFilter(header(request.filter));

        request.filter.notgwithin = Arrays.asList("POLYGON((180 90,-180 90,-180 -80,100 -80,100 -70,180 -70,180 90))","POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))");
        handleMatchingGeometryFilter(post(request),4,hasItems("-70,170","-80,170","-70,160","-80,160"));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notgwithin", request.filter.notgwithin.get(0)),
                        new ImmutablePair<>("notgwithin", request.filter.notgwithin.get(1)))),
                4,hasItems("-70,170","-80,170","-70,160","-80,160"));
        handleMatchingGeometryFilter(header(request.filter),4,hasItems("-70,170","-80,170","-70,160","-80,160"));

        request.filter.gwithin = Arrays.asList("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))");
        request.filter.notgwithin = Arrays.asList("POLYGON((8 8,8 -8,-8 -8,-8 8,8 8))");
        handleMatchingGeometryFilter(post(request),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin.get(0))
                    .param("notgwithin", request.filter.notgwithin.get(0))
                .when().get(getUrlPath("geodata"))
                .then(),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));
        handleMatchingGeometryFilter(header(request.filter),8,hasItems("10,0","10,-10","10,10","10,10","10,0","10,-10","0,10","0,-10"));

        request.filter.gwithin = Arrays.asList("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))");
        request.filter.notgwithin = Arrays.asList("POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))");
        handleNotMatchingGwithinComboFilter(post(request));
        handleNotMatchingGwithinComboFilter(
                givenFilterableRequestParams().param("gwithin", request.filter.gwithin.get(0))
                .param("notgwithin", request.filter.notgwithin.get(0))
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGwithinComboFilter(header(request.filter));
        request.filter.gwithin = null;
        request.filter.notgwithin = null;
    }
    
    @Test
    public void testGintersectFilter() throws Exception {
        request.filter.gintersect = Arrays.asList("POLYGON((0 1,1 1,1 -1,0 -1,0 1))");
        handleMatchingGeometryFilter(post(request),1,everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(get("gintersect",request.filter.gintersect.get(0)),1,everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter),1,everyItem(equalTo("0,0")));

        request.filter.gintersect = Arrays.asList("POLYGON((2 2,3 2,3 3,2 3,2 2))");
        handleNotMatchingGintersectFilter(post(request));
        handleNotMatchingGintersectFilter(get("gintersect", request.filter.gintersect.get(0)));
        handleNotMatchingGintersectFilter(header(request.filter));

        request.filter.gintersect = Arrays.asList("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))","POLYGON((0 1,1 1,1 -1,0 -1,0 1))");
        handleMatchingGeometryFilter(post(request),1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("gintersect", request.filter.gintersect.get(0)),
                        new ImmutablePair<>("gintersect", request.filter.gintersect.get(1)))),
                1, everyItem(equalTo("0,0")));
        handleMatchingGeometryFilter(header(request.filter),1, everyItem(equalTo("0,0")));
        request.filter.gintersect = null;

        request.filter.notgintersect = Arrays.asList("POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))");
        handleMatchingGeometryFilter(post(request),1,everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(get("notgintersect", request.filter.notgintersect.get(0)),1,everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter),1,everyItem(equalTo("-80,170")));

        request.filter.notgintersect = Arrays.asList("POLYGON((180 90,-180 90,-180 -90,180 -90,180 90))");
        handleNotMatchingNotGintersectFilter(post(request));
        handleNotMatchingNotGintersectFilter(get("notgintersect", request.filter.notgintersect.get(0)));
        handleNotMatchingNotGintersectFilter(header(request.filter));

        request.filter.notgintersect = Arrays.asList("POLYGON((12 12,12 -12,-12 -12,-12 12,12 12))","POLYGON((180 90,-180 90,-180 -90,160 -90,160 -70,180 -70,180 90))");
        handleMatchingGeometryFilter(post(request),1,everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(
                get(Arrays.asList(new ImmutablePair<>("notgintersect", request.filter.notgintersect.get(0)),
                        new ImmutablePair<>("notgintersect", request.filter.notgintersect.get(1)))),
                1,everyItem(equalTo("-80,170")));
        handleMatchingGeometryFilter(header(request.filter),1,everyItem(equalTo("-80,170")));

        request.filter.gintersect = Arrays.asList("POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))");
        request.filter.notgintersect = Arrays.asList("POLYGON((10 10,10 -10,0 -10,0 10,10 10))");
        handleMatchingGeometryFilter(post(request),3,hasItems("10,-10","0,-10","-10,-10"));
        handleMatchingGeometryFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect.get(0))
                    .param("notgintersect", request.filter.notgintersect.get(0))
                .when().get(getUrlPath("geodata"))
                .then(),3,hasItems("10,-10","0,-10","-10,-10"));
        handleMatchingGeometryFilter(header(request.filter),3,hasItems("10,-10","0,-10","-10,-10"));

        request.filter.gintersect = Arrays.asList("POLYGON((10 10,10 -10,-10 -10,-10 10,10 10))");
        request.filter.notgintersect = Arrays.asList("POLYGON((11 11,11 -11,-11 -11,-11 11,11 11))");
        handleNotMatchingGintersectComboFilter(post(request));
        handleNotMatchingGintersectComboFilter(
                givenFilterableRequestParams().param("gintersect", request.filter.gintersect.get(0))
                .param("notgintersect", request.filter.notgintersect.get(0))
                .when().get(getUrlPath("geodata"))
                .then());
        handleNotMatchingGintersectComboFilter(header(request.filter));
        request.filter.gintersect = null;
        request.filter.notgintersect = null;

    }
    
    @Test
    public void testComplexFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.like, "Architect"),//"job:eq:Architect"
                new Expression("params.startdate", OperatorEnum.range, "[1009799;1009801]"));
        request.filter.pwithin = Arrays.asList("50,-50,-50,50");
        request.filter.notpwithin = Arrays.asList("50,20,-50,60");
        request.filter.gwithin = Arrays.asList("POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))");
        request.filter.notgwithin = Arrays.asList("POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))");
        request.filter.gintersect = Arrays.asList("POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))");
        request.filter.notgintersect = Arrays.asList("POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))");
        handleComplexFilter(post(request));
        handleComplexFilter(
                givenFilterableRequestParams().param("f", request.filter.f.get(0).toString())
                    .param("f","params.startdate:range:[1009799;1009801]")
                    .param("pwithin", request.filter.pwithin.get(0))
                    .param("notpwithin", request.filter.notpwithin.get(0))
                    .param("gwithin", request.filter.gwithin.get(0))
                    .param("notgwithin", request.filter.notgwithin.get(0))
                    .param("gintersect", request.filter.gintersect.get(0))
                    .param("notgintersect", request.filter.notgintersect.get(0))
                .when().get(getUrlPath("geodata"))
                .then());
        handleComplexFilter(header(request.filter));
        request.filter = new Filter();
    }

    @Test
    public void testMixedFilter() throws Exception {
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Architect"),//"job:eq:Architect"
                new Expression("params.startdate", OperatorEnum.range, "[1009799;2000000]"));
        request.filter.pwithin = Arrays.asList("50,-50,-50,50");
        request.filter.gwithin = Arrays.asList("POLYGON((30 30,30 -30,-30 -30,-30 30,30 30))");
        request.filter.gintersect = Arrays.asList("POLYGON((-20 20, 20 20, 20 -20, -20 -20, -20 20))");

        Filter filterHeader = new Filter();
        filterHeader.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;1009801]"));
        filterHeader.notpwithin = Arrays.asList("50,20,-50,60");
        filterHeader.notgwithin = Arrays.asList("POLYGON((-50 50,-20 50, -20 -50, -50 -50,-50 50))");
        filterHeader.notgintersect = Arrays.asList("POLYGON((-30 -10,30 10, 30 -30, -30 -30,-30 -10))");
        handleComplexFilter(
                givenFilterableRequestParams()
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .param("f", new Expression("params.job", OperatorEnum.eq, "Architect").toString())
                        .param("f", new Expression("params.startdate", OperatorEnum.range, "[1009799;2000000]").toString())
                        .param("pwithin", request.filter.pwithin.get(0))
                        .param("gwithin", request.filter.gwithin.get(0))
                        .param("gintersect", request.filter.gintersect.get(0))
                        .when().get(getUrlPath("geodata"))
                        .then());
        handleComplexFilter(
                givenFilterableRequestBody().body(request)
                        .header("partition-filter", objectMapper.writeValueAsString(filterHeader))
                        .when().post(getUrlPath("geodata"))
                        .then());

        filterHeader.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, "Actor"));
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
        request.filter.f = Arrays.asList(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0]),//"job:eq:" + DataSetTool.jobs[0]
                new Expression("params.startdate", OperatorEnum.range, "[1000000;2000000]"));
        request.filter.pwithin = Arrays.asList("10,10,-10,-10");
        request.filter.notpwithin = Arrays.asList("5,5,-5,-5");
        handleNotFoundCollection(
                givenFilterableRequestBody().body(request)
                .when().post(getUrlPath("unknowncollection"))
                .then());
        handleNotFoundCollection(
                givenFilterableRequestParams().param("f", request.filter.f)
                    .param("pwithin",  request.filter.pwithin.get(0))
                    .param("notpwithin",  request.filter.notpwithin.get(0))
                .when().get(getUrlPath("unknowncollection"))
                .then());
        request.filter.f = null;
        request.filter.pwithin = null;
        request.filter.notpwithin = null;
    }
    
    @Test
    public void testInvalidFilterParameters() throws Exception {
        //FIELD
        request.filter.f = Arrays.asList(new Expression("foobar", null, null));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        request.filter.f = Arrays.asList(new Expression("params.startdate", OperatorEnum.range, "[0;775000],"));//);
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("f", request.filter.f.get(0).toString()));
        handleInvalidParameters(header(request.filter));
        request.filter.f = null;

        //Q
        request.filter.q = Arrays.asList("fullname:My:name");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("q", request.filter.q.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.q = null;
        
        //PWITHIN
        request.filter.pwithin = Arrays.asList("-5,-5,5,5");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));
        handleInvalidParameters(header(request.filter));

        request.filter.pwithin = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("pwithin",request.filter.pwithin));
        handleInvalidParameters(header(request.filter));
        request.filter.pwithin = null;

        request.filter.notpwithin = Arrays.asList("-5,-5,5,5");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin.get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notpwithin = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notpwithin",request.filter.notpwithin.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notpwithin = null;
        
        //GWITHIN
        request.filter.gwithin = Arrays.asList("POLYGON((10 10,10 -10,0 -10))");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin.get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.gwithin = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gwithin",request.filter.gwithin.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gwithin = null;

        request.filter.notgwithin = Arrays.asList("POLYGON((10 10,10 -10,0 -10))");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin.get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notgwithin = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgwithin",request.filter.notgwithin.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgwithin = null;

        //GINTERSECT
        request.filter.gintersect = Arrays.asList("POLYGON((10 10,10 -10,0 -10))");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect.get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.gintersect = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("gintersect",request.filter.gintersect.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.gintersect = null;

        request.filter.notgintersect = Arrays.asList("POLYGON((10 10,10 -10,0 -10))");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect.get(0)));
        handleInvalidParameters(header(request.filter));

        request.filter.notgintersect = Arrays.asList("foo");
        handleInvalidParameters(post(request));
        handleInvalidParameters(get("notgintersect",request.filter.notgintersect.get(0)));
        handleInvalidParameters(header(request.filter));
        request.filter.notgintersect = null;

    }
    
    
    //----------------------------------------------------------------
    //----------------------- COMMON BEHAVIORS -----------------------
    //----------------------------------------------------------------
    protected void handleNotFoundCollection(ValidatableResponse then) throws Exception {
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

    protected abstract void handleMatchingQueryFilter(ValidatableResponse then) throws Exception;

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

    private ValidatableResponse post(Request request){
        return givenFilterableRequestBody().body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(String param,Object paramValue){
        return givenFilterableRequestParams().param(param, paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(List<Pair<String,String>> params){
        RequestSpecification req = givenFilterableRequestParams();
        for(Pair<String,String> param : params) {
            req = req.param(param.getKey(),param.getValue());
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
}
