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

import static io.restassured.RestAssured.given;

import io.arlas.server.DataSetTool;
import io.arlas.server.model.request.*;
import org.junit.Before;
import org.junit.Test;

import io.restassured.response.ValidatableResponse;

import java.util.ArrayList;

public abstract class AbstractAggregatedTest extends AbstractFilteredTest {
    protected static AggregationsRequest aggregationRequest;
    protected static Aggregation aggregationModel;

    @Before
    public void setUpAggregationRequest(){
        aggregationRequest = new AggregationsRequest();
        aggregationRequest.filter = new Filter();
        aggregationRequest.aggregations = new ArrayList<>();
        aggregationModel = new Aggregation();
        aggregationRequest.aggregations.add(aggregationModel);
        request = aggregationRequest;
    }
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testGeohashAggregate() throws Exception {
        //GEOHASH
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(3, null); //"3";
        handleMatchingGeohashAggregate(post(aggregationRequest), 595, 1);
        handleMatchingGeohashAggregate(get("geohash:geo_params.centroid:interval-3"), 595, 1);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, null); //"1";
        handleMatchingGeohashAggregate(post(aggregationRequest),32, 16, 25);
        handleMatchingGeohashAggregate(get("geohash:geo_params.centroid:interval-1"),32, 16, 25);

        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "avg";
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, "avg", 790075F, 1230075F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-avg"),
                32, 16, 25, "avg", 790075F, 1230075F);

        aggregationRequest.aggregations.get(0).collectFct = "cardinality";
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, "cardinality", 16F, 25F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-cardinality"),
                32, 16, 25, "cardinality", 16F, 25F);

        aggregationRequest.aggregations.get(0).collectFct = "max";
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, "max", 817000F, 1263600F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-max"),
                32, 16, 25, "max", 817000F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "min";
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, "min", 763600F, 1197000F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-min"),
                32, 16, 25, "min", 763600F, 1197000F);

        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, "sum", 12641200F, 28305000F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum"),
                32, 16, 25, "sum", 12641200F, 28305000F);
    }
    
    @Test
    public void testDateHistogramAggregate() throws Exception {        
        //DATEHISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.datehistogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.day); // "1day";
        handleMatchingAggregate(post(aggregationRequest), 1, 595);
        handleMatchingAggregate(get("datehistogram:params.startdate:interval-1day"), 1, 595);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.minute); //"1minute";
        handleMatchingAggregate(post(aggregationRequest), 10, 1, 104);
        handleMatchingAggregate(get("datehistogram:params.startdate:interval-1minute"),
                10, 1, 104);

        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "avg";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, "avg", 769433F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-avg"),
                10, 1, 104, "avg", 769433F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "cardinality";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, "cardinality", 1F, 72F);
        handleMatchingAggregateWithCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-cardinality"),
                10, 1, 104, "cardinality", 1F, 72F);

        aggregationRequest.aggregations.get(0).collectFct = "max";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, "max", 772800F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-max"),
                10, 1, 104, "max", 772800F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "min";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, "min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-min"),
                10, 1, 104, "min", 763600F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, "sum", 1263600F, 102986100F);
        handleMatchingAggregateWithCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-sum"),
                10, 1, 104, "sum", 1263600F, 102986100F);

        aggregationRequest.aggregations.get(0).collectField = null;
        aggregationRequest.aggregations.get(0).collectFct = null;
        aggregationRequest.aggregations.get(0).format = "yyyyMMdd";
        handleMatchingAggregate(post(aggregationRequest),10, 1, 104,"19700101");
        handleMatchingAggregate(get("datehistogram:params.startdate:interval-1minute:format-yyyyMMdd"),
                10, 1, 104,"19700101");

        aggregationRequest.aggregations.get(0).format = null;
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.count;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104,"1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(get("datehistogram:params.startdate:interval-1minute:order-asc:on-count"),
                10, 1, 104,"1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.field;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104,"1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(get("datehistogram:params.startdate:interval-1minute:order-desc:on-field"),
                10, 1, 104,"1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104,"1970-01-01-00:12:00");
        handleMatchingAggregateWithOrder(get("datehistogram:params.startdate:interval-1minute:order-asc:on-field"),
                10, 1, 104,"1970-01-01-00:12:00");

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.result;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104,"1970-01-01-00:16:00");
        handleMatchingAggregateWithOrder(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"),
                10, 1, 104,"1970-01-01-00:16:00");

        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104,"1970-01-01-00:21:00");

        handleMatchingAggregateWithOrder(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-sum:order-asc:on-result"),
                10, 1, 104,"1970-01-01-00:21:00");
    }
    
    @Test
    public void testHistogramAggregate() throws Exception {        
        //HISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";

        aggregationRequest.aggregations.get(0).interval = new Interval(2000000, null);
        handleMatchingAggregate(post(aggregationRequest), 1, 595);
        handleMatchingAggregate(get("histogram:params.startdate:interval-2000000"), 1, 595);

        aggregationRequest.aggregations.get(0).interval = new Interval(100000, null);
        handleMatchingAggregate(post(aggregationRequest),6, 14, 176);
        handleMatchingAggregate(get("histogram:params.startdate:interval-100000"),6, 14, 176);

        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "avg";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, "avg", 786078F, 1226267F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-avg"),
                6, 14, 176, "avg", 786078F, 1226267F);

        aggregationRequest.aggregations.get(0).collectFct = "cardinality";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, "cardinality", 14F, 111F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-cardinality"),
                6, 14, 176, "cardinality", 14F, 111F);

        aggregationRequest.aggregations.get(0).collectFct = "max";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, "max", 799800F, 1263600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-max"),
                6, 14, 176, "max", 799800F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "min";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, "min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-min"),
                6, 14, 176, "min", 763600F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, "sum", 11005100F, 170040600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum"),
                6, 14, 176, "sum", 11005100F, 170040600F);

        aggregationRequest.aggregations.get(0).collectField = null;
        aggregationRequest.aggregations.get(0).collectFct = null;
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.count;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176,"700000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-asc:on-count"),
                6, 14, 176,"700000");

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.field;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176,"1200000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-desc:on-field"),
                6, 14, 176,"1200000");

        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176,"700000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-asc:on-field"),
                6, 14, 176,"700000");

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.result;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingAggregateWithOrder(post(aggregationRequest),6, 14, 176,"1000000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"),
                6, 14, 176,"1000000");

        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum:order-asc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"700000");
    }
    
    @Test
    public void testTermAggregate() throws Exception {        
        //TERM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).field = "params.job";
        handleMatchingAggregate(post(aggregationRequest), DataSetTool.jobs.length-1, 58, 64);
        handleMatchingAggregate(get("term:params.job"), DataSetTool.jobs.length-1, 58, 64);

        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "avg";
        handleMatchingAggregateWithCollect(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64, "avg", 1000000F, 1000000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-avg"),DataSetTool.jobs.length-1, 58, 64, "avg", 1000000F, 1000000F);

        aggregationRequest.aggregations.get(0).collectFct = "cardinality";
        handleMatchingAggregateWithCollect(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64, "cardinality", 44F, 49F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-cardinality"),DataSetTool.jobs.length-1, 58, 64, "cardinality", 44F, 49F);

        aggregationRequest.aggregations.get(0).collectFct = "max";
        handleMatchingAggregateWithCollect(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64, "max", 1166400F, 1263600F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-max"),DataSetTool.jobs.length-1, 58, 64, "max", 1166400F, 1263600F);

        aggregationRequest.aggregations.get(0).collectFct = "min";
        handleMatchingAggregateWithCollect(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64, "min", 763600F, 840000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-min"),DataSetTool.jobs.length-1, 58, 64, "min", 763600F, 840000F);

        aggregationRequest.aggregations.get(0).collectFct = "sum";
        handleMatchingAggregateWithCollect(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64, "sum", 58000000F, 640000000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-sum"),DataSetTool.jobs.length-1, 58, 64, "sum", 58000000F, 640000000F);

        aggregationRequest.aggregations.get(0).collectField = null;
        aggregationRequest.aggregations.get(0).collectFct = null;
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.count;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");
        handleMatchingAggregateWithOrder(get("term:params.job:order-desc:on-count"),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.field;
        handleMatchingAggregateWithOrder(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64,"Dancer");
        handleMatchingAggregateWithOrder(get("term:params.job:order-desc:on-field"),DataSetTool.jobs.length-1, 58, 64,"Dancer");

        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64,"Actor");
        handleMatchingAggregateWithOrder(get("term:params.job:order-asc:on-field"),DataSetTool.jobs.length-1, 58, 64,"Actor");

        aggregationRequest.aggregations.get(0).collectField = "params.startdate";
        aggregationRequest.aggregations.get(0).collectFct = "sum";
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.result;
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");
        handleMatchingAggregateWithOrder(get("term:params.job:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");
    }
    
    @Test
    public void testMultiAggregate() throws Exception {
        Aggregation aggregationModelSub = new Aggregation();
        aggregationRequest.aggregations.add(aggregationModelSub);

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);

        aggregationRequest.aggregations.get(1).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(1).field = "params.job";
        handleMultiMatchingGeohashAggregate(post(aggregationRequest),32);
        handleMultiMatchingGeohashAggregate(
                given().param("agg", "geohash:geo_params.centroid:interval-1")
                .param("agg", "term:params.job")
                .when().get(getUrlPath("geodata"))
                .then(),32);

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).interval.value = 100000;

        aggregationRequest.aggregations.get(1).type = AggregationTypeEnum.datehistogram;
        aggregationRequest.aggregations.get(1).field = "params.startdate";
        aggregationRequest.aggregations.get(1).interval = new Interval(1, UnitEnum.minute); //"1minute";
        handleMultiMatchingAggregate(post(aggregationRequest),6);
        handleMultiMatchingAggregate(
                given().param("agg", "histogram:params.startdate:interval-100000")
                .param("agg", "datehistogram:params.startdate:interval-1minute")
                .when().get(getUrlPath("geodata"))
                .then(),6);
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    
    @Test
    public void testInvalidAggregateParameters() throws Exception {
        //INVALID TYPE
        handleInvalidParameters(get("foobar"));
        handleInvalidParameters(get("foobar:params.job"));
        
        //INVALID GEOHASH
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash"));

        aggregationRequest.aggregations.get(0).field = "params.job";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:params.job"));

        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:params.job:interval-1"));

        aggregationRequest.aggregations.get(0).field = "geo_centroid";
        aggregationRequest.aggregations.get(0).interval  = new Interval(0, null);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-0"));

        aggregationRequest.aggregations.get(0).interval  = new Interval(13, null);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-13"));

        aggregationRequest.aggregations.get(0).interval =  new Interval(1, null);
        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.asc;
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.count;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-1:order-asc:on-count"));

        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.field;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-1:order-asc:on-field"));

        aggregationRequest.aggregations.get(0).order = AggregationOrderEnum.desc;
        aggregationRequest.aggregations.get(0).on = AggregationOnEnum.result;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-1:order-desc:on-result"));

        aggregationRequest.aggregations.get(0).order = null;
        aggregationRequest.aggregations.get(0).on = null;
        aggregationRequest.aggregations.get(0).collectField = "foo";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-1:collect_field-foo"));

        aggregationRequest.aggregations.get(0).collectFct = "bar";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("geohash:geo_params.centroid:interval-1:collect_field-foo:collect_fct-bar"));

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.datehistogram;
        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).interval = new Interval(1,UnitEnum.day);// TODO: was incorrect (iday), was it on purpose?
        aggregationRequest.aggregations.get(0).collectFct = null;
        aggregationRequest.aggregations.get(0).collectField = null;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.job:interval-1day"));

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).interval  = new Interval(100000, null);
        aggregationRequest.aggregations.get(0).format = "yyyyMMdd";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.startdate:interval-100000:format-yyyyMMdd"));

        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).interval = null;
        aggregationRequest.aggregations.get(0).format = null;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.job"));

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("term:params.job:interval-1"));
    }

    @Test
    public void testNotImplementedAggregateParameters() throws Exception {
        //NOT IMPLEMENTED PARAMETER
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        aggregationRequest.aggregations.get(0).field = "geo_centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
        aggregationRequest.aggregations.get(0).size = "1";
        handleNotImplementedParameters(post(aggregationRequest));
        handleNotImplementedParameters(get("geohash:geo_params.centroid:interval-1:size-1"));
    }
    
    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------
    
    protected abstract void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception;
    protected abstract void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;
    protected abstract void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin, float featureCollectMax) throws Exception;
    
    protected abstract void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception;
    protected abstract void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;
    protected abstract void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception;
    protected abstract void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception;
    protected abstract void handleMatchingAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin, float featureCollectMax) throws Exception;
    
    protected abstract void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception;
    protected abstract void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception;

    private ValidatableResponse post(Request request){
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(Object paramValue){
        return given().param("agg", paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }
}
