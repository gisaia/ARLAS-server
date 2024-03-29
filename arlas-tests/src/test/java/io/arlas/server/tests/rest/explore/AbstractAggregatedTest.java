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

import io.arlas.server.core.model.enumerations.*;
import io.arlas.server.core.model.request.*;
import io.arlas.server.tests.DataSetTool;
import io.dropwizard.jackson.JsonSnakeCase;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.server.core.utils.CheckParams.GEO_AGGREGATION_TYPE_ENUMS;
import static io.restassured.RestAssured.given;

public abstract class AbstractAggregatedTest extends AbstractFormattedTest {
    protected static AggregationsRequest aggregationRequest;
    protected static Aggregation aggregationModel;

    @Before
    public void setUpAggregationRequest() {
        aggregationRequest = new AggregationsRequest();
        aggregationRequest.filter = new Filter();
        aggregationRequest.filter.righthand = false;
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
        handleMatchingGeohashAggregate(post(aggregationRequest), 595, 1, 1);
        handleMatchingGeohashAggregate(get("geohash:geo_params.centroid:interval-3"), 595, 1, 1);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, null); //"1";
        handleMatchingGeohashAggregate(post(aggregationRequest), 32, 16, 25);
        handleMatchingGeohashAggregate(get("geohash:geo_params.centroid:interval-1"), 32, 16, 25);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 32, 16, 25, -169.453125F, -79.453125F, 169.453125F, 79.453125F);
        handleMatchingGeohashAggregateCenter(get("geohash:geo_params.centroid:interval-1"), 32, 16, 25, -169.453125F, -79.453125F, 169.453125F, 79.453125F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELLCENTER);
        handleMatchingGeohashAggregate(post(aggregationRequest), 32, 16, 25);
        handleMatchingGeohashAggregate(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 32, 16, 25);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 32, 16, 25, -169.453125F, -79.453125F, 169.453125F, 79.453125F);
        handleMatchingGeohashAggregateCenter(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 32, 16, 25, -169.453125F, -79.453125F, 169.453125F, 79.453125F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 32, 16, 25, -171F, -81F, 141F, 51F);
        handleMatchingAggregateWithGeometry(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry"), 32, 16, 25, -171F, -81F, 141F, 51F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 32, 16, 25, -141F, -51F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.startdate)"), 32, 16, 25, -141F, -51F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 32, 16, 25, -161F, -99F, 161F, 71F);
        handleMatchingAggregateWithGeometry(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(params.age)"), 32, 16, 25, -161F, -99F, 161F, 71F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 32, 16, 25, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.age)"), 32, 16, 25, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = null;

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingAggregateWithFetchedHits(post(aggregationRequest), 32, 3, "params.country", "params.startdate");
        handleMatchingAggregateWithFetchedHits(get("geohash:geo_params.centroid:interval-1:fetch_hits-3(params.country,params.startdate)"), 32, 3, "params.country", "params.startdate");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingAggregateWithSortedFetchedDates(post(aggregationRequest), 32, 3, 807500, 1263600, "params.startdate");
        handleMatchingAggregateWithSortedFetchedDates(get("geohash:geo_params.centroid:interval-1:fetch_hits-3(-params.startdate)"), 32, 3, 807500, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 790075F, 1230075F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-avg"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 790075F, 1230075F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY, 40000));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 16F, 25F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-cardinality"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 16F, 25F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "max", 817000F, 1263600F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-max"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 817000F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1197000F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-min"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1197000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 12641200F, 28305000F);
        handleMatchingGeohashAggregateWithCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 12641200F, 28305000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithMultiCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField, "sum", "max", 12641200F, 28305000F, 1600F,13600F);
        handleMatchingGeohashAggregateWithMultiCollect(get("geohash:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 12641200F, 28305000F, 1600F,13600F);


        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeocentroidCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);
        handleMatchingGeohashAggregateWithGeocentroidCollect(get("geohash:geo_params.centroid:interval-1:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxCollect(post(aggregationRequest),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxCollect(get("geohash:geo_params.centroid:interval-1:collect_fct-geobbox:collect_field-geo_params.centroid"),
                32, 16, 25, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CENTROID);
        handleMatchingGeohashAggregateWithGeocentroidBucket(post(aggregationRequest),
                32, 16, 25, 0, -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);
        handleMatchingGeohashAggregateWithGeocentroidBucket(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-centroid"),
                32, 16, 25, 0, -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleMatchingAggregateWithGeometry(post(aggregationRequest),
                32, 16, 25, -180F, -90F, 180F, 90F);
        handleMatchingAggregateWithGeometry(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-cell"),
                32, 16, 25, -180F, -90F, 180F, 90F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                32, 16, 25, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox"),
                32, 16, 25, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                32, 16, 25, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geobbox:collect_field-geo_params.centroid"),
                32, 16, 25, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                32, 16, 25, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                32, 16, 25, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);


    }

    @Test
    @Ignore
    // Not testable without ES premium, so ignored for CI
    public void testGeohexAggregate() throws Exception {
        //GEOHEX
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohex;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(3, null); //"3";
        handleMatchingGeohashAggregate(post(aggregationRequest), 595, 1, 1);
        handleMatchingGeohashAggregate(get("geohex:geo_params.centroid:interval-3"), 595, 1, 1);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, null); //"1";
        handleMatchingGeohashAggregate(post(aggregationRequest), 492, 1, 5);
        handleMatchingGeohashAggregate(get("geohex:geo_params.centroid:interval-1"), 492, 1, 5);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 492, 1, 5, -177.84439F, -83.64732F, 175.02405F, 8365766F);
        handleMatchingGeohashAggregateCenter(get("geohex:geo_params.centroid:interval-1"), 492, 1, 5, -177.84439F, -83.64732F, 175.02405F, 8365766F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELLCENTER);
        handleMatchingGeohashAggregate(post(aggregationRequest), 492, 1, 5);
        handleMatchingGeohashAggregate(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 492, 1, 5);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 492, 1, 5, -177.84439F, -83.64732F, 175.02405F, 8365766F);
        handleMatchingGeohashAggregateCenter(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 492, 1, 5, -177.84439F, -83.64732F, 175.02405F, 8365766F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 492, 1, 5, -171F, -81F, 141F, 81F);
        handleMatchingAggregateWithGeometry(get("geohex:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry"), 492, 1, 5, -171F, -81F, 141F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 492, 1, 5, -141F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geohex:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.startdate)"), 492, 1, 5, -141F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 492, 1, 5, -161F, -99F, 161F, 81F);
        handleMatchingAggregateWithGeometry(get("geohex:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(params.age)"), 492, 1, 5, -161F, -99F, 161F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 492, 1, 5, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geohex:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.age)"), 492, 1, 5, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = null;

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingAggregateWithFetchedHits(post(aggregationRequest), 492, 3, "params.country", "params.startdate");
        handleMatchingAggregateWithFetchedHits(get("geohex:geo_params.centroid:interval-1:fetch_hits-3(params.country,params.startdate)"), 492, 3, "params.country", "params.startdate");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingAggregateWithSortedFetchedDates(post(aggregationRequest), 492, 3, 763600, 1263600, "params.startdate");
        handleMatchingAggregateWithSortedFetchedDates(get("geohex:geo_params.centroid:interval-1:fetch_hits-3(-params.startdate)"), 492, 3, 763600, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 790075F, 1230075F);
        handleMatchingGeohashAggregateWithCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-avg"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 790075F, 1230075F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 5F, 25F);
        handleMatchingGeohashAggregateWithCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-cardinality"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 5F, 25F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "max", 817000F, 1263600F);
        handleMatchingGeohashAggregateWithCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-max"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 817000F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1197000F);
        handleMatchingGeohashAggregateWithCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-min"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1197000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 5076000F, 28305000F);
        handleMatchingGeohashAggregateWithCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 5076000F, 28305000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithMultiCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField, "sum", "max", 12641200F, 28305000F, 1600F,13600F);
        handleMatchingGeohashAggregateWithMultiCollect(get("geohex:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 12641200F, 28305000F, 1600F,13600F);


        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeocentroidCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);
        handleMatchingGeohashAggregateWithGeocentroidCollect(get("geohex:geo_params.centroid:interval-1:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -155.00000031664968F, -65.00000014901161F, 154.99999981373549F, 64.99999981373549F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxCollect(post(aggregationRequest),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxCollect(get("geohex:geo_params.centroid:interval-1:collect_fct-geobbox:collect_field-geo_params.centroid"),
                492, 1, 5, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CENTROID);
        handleMatchingGeohashAggregateWithGeocentroidBucket(post(aggregationRequest),
                492, 1, 5, 0, -150F, -80F, 150F, 80F);
        handleMatchingGeohashAggregateWithGeocentroidBucket(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-centroid"),
                492, 1, 5, 0, -150F, -80F, 150F, 80F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleMatchingAggregateWithGeometry(post(aggregationRequest),
                492, 1, 5, -180F, -90F, 180F, 90F);
        handleMatchingAggregateWithGeometry(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-cell"),
                492, 1, 5, -180F, -90F, 180F, 90F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                492, 1, 5, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-bbox"),
                492, 1, 5, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                492, 1, 5, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geobbox:collect_field-geo_params.centroid"),
                492, 1, 5, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                492, 1, 5, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geohex:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                492, 1, 5, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);


    }

    @Test
    public void testGeotileAggregate() throws Exception {
        //GEOTILE
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geotile;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(6, null); //"6";
        handleMatchingGeohashAggregate(post(aggregationRequest), 595, 1, 1);
        handleMatchingGeohashAggregate(get("geotile:geo_params.centroid:interval-6"), 595, 1, 1);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, null); //"1";
        handleMatchingGeohashAggregate(post(aggregationRequest), 4, 136, 162);
        handleMatchingGeohashAggregate(get("geotile:geo_params.centroid:interval-1"), 4, 136, 162);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 4, 136, 162, -90F, -42.525566F, 90F, 42.525565F);
        handleMatchingGeohashAggregateCenter(get("geotile:geo_params.centroid:interval-1"), 4, 136, 162, -90F, -42.525566F, 90F, 42.525565F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELLCENTER);
        handleMatchingGeohashAggregate(post(aggregationRequest), 4, 136, 162);
        handleMatchingGeohashAggregate(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 4, 136, 162);

        handleMatchingGeohashAggregateCenter(post(aggregationRequest), 4, 136, 162, -90F, -42.525566F, 90F, 42.525565F);
        handleMatchingGeohashAggregateCenter(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-cell_center"), 4, 136, 162, -90F, -42.525566F, 90F, 42.525565F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 4, 136, 162, -171F, -81F, 141F, 51F);
        handleMatchingAggregateWithGeometry(get("geotile:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry"), 4, 136, 162, -171F, -81F, 141F, 51F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 4, 136, 162, -141F, -51F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geotile:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.startdate)"), 4, 136, 162, -141F, -51F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 4, 136, 162, -161F, -99F, 161F, 71F);
        handleMatchingAggregateWithGeometry(get("geotile:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(params.age)"), 4, 136, 162, -161F, -99F, 161F, 71F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 4, 136, 162, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("geotile:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry(-params.age)"), 4, 136, 162, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = null;

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingAggregateWithFetchedHits(post(aggregationRequest), 4, 3, "params.country", "params.startdate");
        handleMatchingAggregateWithFetchedHits(get("geotile:geo_params.centroid:interval-1:fetch_hits-3(params.country,params.startdate)"), 4, 3, "params.country", "params.startdate");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingAggregateWithSortedFetchedDates(post(aggregationRequest), 4, 3, 980000, 1263600, "params.startdate");
        handleMatchingAggregateWithSortedFetchedDates(get("geotile:geo_params.centroid:interval-1:fetch_hits-3(-params.startdate)"), 4, 3, 980000, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 873600F, 1133825F);
        handleMatchingGeohashAggregateWithCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-avg"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 873600F, 1133825F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY, 3000));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 116, 162F);
        handleMatchingGeohashAggregateWithCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-cardinality"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 116, 162F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "max", 990000F, 1263600F);
        handleMatchingGeohashAggregateWithCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-max"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 990000F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1010000F);
        handleMatchingGeohashAggregateWithCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-min"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1010000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingGeohashAggregateWithCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 129329200F, 168739200F);
        handleMatchingGeohashAggregateWithCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 129329200F, 168739200F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingGeohashAggregateWithMultiCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField, "sum", "max", 129329200F, 168739200F, 13600F,13600F);
        handleMatchingGeohashAggregateWithMultiCollect(get("geotile:geo_params.centroid:interval-1:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 129329200F, 168739200F, 13600F,13600F);


        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeocentroidCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -90.00000003944425F, -40.00000001862645F, 84.9999999627471F, 44.99999997904524F);
        handleMatchingGeohashAggregateWithGeocentroidCollect(get("geotile:geo_params.centroid:interval-1:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -90.00000003944425F, -40.00000001862645F, 84.9999999627471F, 44.99999997904524F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxCollect(post(aggregationRequest),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxCollect(get("geotile:geo_params.centroid:interval-1:collect_fct-geobbox:collect_field-geo_params.centroid"),
                4, 136, 162, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CENTROID);
        handleMatchingGeohashAggregateWithGeocentroidBucket(post(aggregationRequest),
                4, 136, 162, 0, -90.00000003944425F, -40.00000001862645F, 84.9999999627471F, 44.99999997904524F);
        handleMatchingGeohashAggregateWithGeocentroidBucket(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-centroid"),
                4, 136, 162, 0, -90.00000003944425F, -40.00000001862645F, 84.9999999627471F, 44.99999997904524F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleMatchingAggregateWithGeometry(post(aggregationRequest),
                4, 136, 162, -180F, -90F, 180F, 90F);
        handleMatchingAggregateWithGeometry(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-cell"),
                4, 136, 162, -180F, -90F, 180F, 90F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                4, 136, 162, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-bbox"),
                4, 136, 162, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                4, 136, 162, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geobbox:collect_field-geo_params.centroid"),
                4, 136, 162, 0, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingGeohashAggregateWithGeoBboxBucket(post(aggregationRequest),
                4, 136, 162, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingGeohashAggregateWithGeoBboxBucket(get("geotile:geo_params.centroid:interval-1:aggregated_geometries-bbox:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                4, 136, 162, 1, -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);


    }

    @Test
    public void testDateHistogramAggregate() throws Exception {
        //DATEHISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.datehistogram;

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.second); // "1sec";

        handleMatchingDateHistogramAggregate(post(aggregationRequest), 501, 0, 8, 763000);
        handleMatchingDateHistogramAggregate(get("datehistogram:interval-1second"), 501, 0, 8, 763000);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.day); // "1day";
        handleSumOtherCountsExistence(post(aggregationRequest), 1, 595, 595);
        handleSumOtherCountsExistence(get("datehistogram:interval-1day"), 1, 595, 595);

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.minute); //"1minute";
        handleSumOtherCountsExistence(post(aggregationRequest), 10, 1, 104);
        handleSumOtherCountsExistence(get("datehistogram:interval-1minute"),
                10, 1, 104);

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingHistogramAggregateWithFetchedHits(post(aggregationRequest), 10, 3, "params.country", "params.startdate");
        handleMatchingHistogramAggregateWithFetchedHits(get("datehistogram:interval-1minute:fetch_hits-3(params.country,params.startdate)"), 10, 3, "params.country", "params.startdate");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingHistogramAggregateWithSortedFetchedDates(post(aggregationRequest), 10, 3, 763600, 1263600, "params.startdate");
        handleMatchingHistogramAggregateWithSortedFetchedDates(get("datehistogram:interval-1minute:fetch_hits-3(-params.startdate)"), 10, 3, 763600, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

        // FETCHGEOMETRY TESTS
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingGeohashAggregate(post(aggregationRequest), 10, 1, 104);
        handleMatchingGeohashAggregate(get("datehistogram:interval-1minute:aggregated_geometries-bbox"),  10, 1, 104);

        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -180F, -90F, 180F, 90F);
        handleMatchingAggregateWithGeometry(get("datehistogram:interval-1minute:aggregated_geometries-bbox"), 10, 1, 104, -180F, -90F, 180F, 90F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("datehistogram:interval-1minute:raw_geometries-geo_params.geometry"), 10, 1, 104, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("datehistogram:interval-1minute:raw_geometries-geo_params.geometry(-params.startdate)"), 10, 1, 104, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "+params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -71F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("datehistogram:interval-1minute:raw_geometries-geo_params.geometry(+params.age)"), 10, 1, 104, -171F, -71F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("datehistogram:interval-1minute:raw_geometries-geo_params.geometry(-params.age)"), 10, 1, 104, -171F, -81F, 171F, 81F);
        aggregationRequest.aggregations.get(0).rawGeometries = null;

        // METRICS TESTS
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 769433F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-avg"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 769433F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 1F, 72F);
        handleMatchingAggregateWithCollect(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-cardinality"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 1F, 72F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 772800F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-max"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 772800F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-min"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "min", 763600F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 1263600F, 102986100F);
        handleMatchingAggregateWithCollect(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-sum"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "sum", 1263600F, 102986100F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithGeocentroidCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -166.66666673496366F, -76.66666673496366F, 169.9999999254942F, 79.9999999254942F);
        handleMatchingAggregateWithGeocentroidCollect(get("datehistogram:interval-1minute:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -166.66666673496366F, -76.66666673496366F, 169.9999999254942F, 79.9999999254942F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingAggregateWithGeoBboxCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingAggregateWithGeoBboxCollect(get("datehistogram:interval-1minute:collect_fct-geobbox:collect_field-geo_params.centroid"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingAggregateWithMultiCollect(post(aggregationRequest),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField, "sum", "max", 1263600F, 102986100F, 8800F,13600F);
        handleMatchingAggregateWithMultiCollect(get("datehistogram:params.startdate:interval-1minute:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                10, 1, 104, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 1263600F, 102986100F, 8800F,13600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).format = "yyyyMMdd";
        handleMatchingAggregate(post(aggregationRequest), 10, 1, 104, "19700101");
        handleMatchingAggregate(get("datehistogram:interval-1minute:format-yyyyMMdd"),
                10, 1, 104, "19700101");

        aggregationRequest.aggregations.get(0).format = null;
        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:order-asc:on-count"),
                10, 1, 104, "1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).on = OrderOn.field;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:order-desc:on-field"),
                10, 1, 104, "1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:12:00");
        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:order-asc:on-field"),
                10, 1, 104, "1970-01-01-00:12:00");

        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.asc;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:order-asc:on-count:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                10, 1, 104, "1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).on = OrderOn.result;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:16:00");
        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"),
                10, 1, 104, "1970-01-01-00:16:00");

        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                10, 1, 104, "1970-01-01-00:21:00");

        handleMatchingAggregateWithOrder(get("datehistogram:interval-1minute:collect_field-params.startdate:collect_fct-sum:order-asc:on-result"),
                10, 1, 104, "1970-01-01-00:21:00");

        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.day); // "1day";
        handleMatchingAggregate(post(aggregationRequest), 1, 595, 595);
        handleMatchingAggregate(get("datehistogram:params.startdate:interval-1day"), 1, 595, 595);

    }

    @Test
    public void testHistogramAggregate() throws Exception {
        //HISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";

        aggregationRequest.aggregations.get(0).interval = new Interval(2000000, null);
        handleSumOtherCountsExistence(post(aggregationRequest), 1, 595, 595);
        handleSumOtherCountsExistence(get("histogram:params.startdate:interval-2000000"), 1, 595, 595);

        aggregationRequest.aggregations.get(0).interval = new Interval(200000.5, null);
        handleSumOtherCountsExistence(post(aggregationRequest), 4, 14, 292);
        handleSumOtherCountsExistence(get("histogram:params.startdate:interval-200000.5"), 4, 14, 292);

        aggregationRequest.aggregations.get(0).interval = new Interval(100000, null);
        handleSumOtherCountsExistence(post(aggregationRequest), 6, 14, 176, -1);
        handleSumOtherCountsExistence(get("histogram:params.startdate:interval-100000"), 6, 14, 176);

        aggregationRequest.aggregations.get(0).interval = new Interval(60000, null); //"1minute";
        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingHistogramAggregateWithFetchedHits(post(aggregationRequest), 10, 3, "params.country", "params.startdate");
        handleMatchingHistogramAggregateWithFetchedHits(get("histogram:params.startdate:interval-60000:fetch_hits-3(params.country,params.startdate)"), 10, 3, "params.country", "params.startdate");


        aggregationRequest.aggregations.get(0).interval = new Interval(60000, null); //"1minute";
        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("geo_params.geometry"));
        handleMatchingHistogramAggregateWithFetchedHits(post(aggregationRequest), 10, 3, "geo_params.geometry");
        handleMatchingHistogramAggregateWithFetchedHits(get("histogram:params.startdate:interval-60000:fetch_hits-3(geo_params.geometry)"), 10, 3, "geo_params.geometry");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingHistogramAggregateWithSortedFetchedDates(post(aggregationRequest), 10, 3, 763600, 1263600, "params.startdate");
        handleMatchingHistogramAggregateWithSortedFetchedDates(get("histogram:params.startdate:interval-60000:fetch_hits-3(-params.startdate)"), 10, 3, 763600, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

        // FETCHGEOMETRY TESTS
        aggregationRequest.aggregations.get(0).interval = new Interval(60000, null);
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingGeohashAggregate(post(aggregationRequest), 10, 1, 104);
        handleMatchingGeohashAggregate(get("histogram:params.startdate:interval-60000:aggregated_geometries-bbox"),  10, 1, 104);

        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -180F, -90F, 180F, 90F);
        handleMatchingAggregateWithGeometry(get("histogram:params.startdate:interval-60000:aggregated_geometries-bbox"), 10, 1, 104, -180F, -90F, 180F, 90F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("histogram:params.startdate:interval-60000:raw_geometries-geo_params.geometry"), 10, 1, 104, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("histogram:params.startdate:interval-60000:raw_geometries-geo_params.geometry(-params.startdate)"), 10, 1, 104, -171F, -81F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "+params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -71F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("histogram:params.startdate:interval-60000:raw_geometries-geo_params.geometry(+params.age)"), 10, 1, 104, -171F, -71F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), 10, 1, 104, -171F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("histogram:params.startdate:interval-60000:raw_geometries-geo_params.geometry(-params.age)"), 10, 1, 104, -171F, -81F, 171F, 81F);
        aggregationRequest.aggregations.get(0).rawGeometries = null;
        aggregationRequest.aggregations.get(0).interval = new Interval(100000, null);

        // METRICS TESTS
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 786078F, 1226267F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-avg"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 786078F, 1226267F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 14F, 111F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-cardinality"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 14F, 111F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 799800F, 1263600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-max"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"max", 799800F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-min"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingAggregateWithCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 11005100F, 170040600F);
        handleMatchingAggregateWithCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 11005100F, 170040600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingAggregateWithMultiCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 11005100F, 170040600F, 8800F,13600F);
        handleMatchingAggregateWithMultiCollect(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 11005100F, 170040600F, 8800F,13600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithGeocentroidCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -158.57142884284258F, -65.71428582072258F, 153.33333296701312F, 63.333333134651184F);
        handleMatchingAggregateWithGeocentroidCollect(get("histogram:params.startdate:interval-100000:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geocentroid", -158.57142884284258F, -65.71428582072258F, 153.33333296701312F, 63.333333134651184F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingAggregateWithGeoBboxCollect(post(aggregationRequest),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);
        handleMatchingAggregateWithGeoBboxCollect(get("histogram:params.startdate:interval-100000:collect_fct-geobbox:collect_field-geo_params.centroid"),
                6, 14, 176, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -170.00000000931323F, -80.00000000931323F, 169.9999999254942F, 79.99999996740371F);



        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176, "700000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-asc:on-count"),
                6, 14, 176, "700000");

        aggregationRequest.aggregations.get(0).on = OrderOn.field;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176, "1200000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-desc:on-field"),
                6, 14, 176, "1200000");

        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176, "700000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-asc:on-field"),
                6, 14, 176, "700000");

        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.asc;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithOrder(post(aggregationRequest),
                6, 14, 176, "700000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:order-asc:on-count:collect_fct-geocentroid:collect_field-geo_params.centroid"),
                6, 14, 176, "700000");


        aggregationRequest.aggregations.get(0).on = OrderOn.result;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingAggregateWithOrder(post(aggregationRequest), 6, 14, 176, "1000000");
        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"),
                6, 14, 176, "1000000");

        handleMatchingAggregateWithOrder(get("histogram:params.startdate:interval-100000:collect_field-params.startdate:collect_fct-sum:order-asc:on-result")
                , 6, 14, 176, "700000");
    }

    @Test
    public void testTermAggregate() throws Exception {
        //TERM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).field = "params.job";
        handleSumOtherCountsExistence(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, 0);
        handleSumOtherCountsExistence(get("term:params.job"), DataSetTool.jobs.length - 1, 58, 64, 0);

        aggregationRequest.aggregations.get(0).size = "5";
        handleSumOtherCountsExistence(post(aggregationRequest), 5, 58, 64, 290);
        handleSumOtherCountsExistence(get("term:params.job:size-5"), 5, 58, 64, 290);
        aggregationRequest.aggregations.get(0).size = null;

        aggregationRequest.aggregations.get(0).include = "A.*";
        handleSumOtherCountsExistence(post(aggregationRequest), 4, 58, 59, 0);
        handleSumOtherCountsExistence(get("term:params.job:include-A.*"), 4, 58, 59, 0);
        aggregationRequest.aggregations.get(0).include = null;

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.AVG));
        handleMatchingAggregateWithCollect(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64,aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "avg", 1000000F, 1000000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-avg"), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"avg", 1000000F, 1000000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.CARDINALITY));
        handleMatchingAggregateWithCollect(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "cardinality", 44F, 49F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-cardinality"), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"cardinality", 44F, 49F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MAX));
        handleMatchingAggregateWithCollect(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "max", 1166400F, 1263600F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-max"), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "max", 1166400F, 1263600F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.MIN));
        handleMatchingAggregateWithCollect(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 840000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-min"), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"min", 763600F, 840000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleMatchingAggregateWithCollect(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "sum", 58000000F, 640000000F);
        handleMatchingAggregateWithCollect(get("term:params.job:collect_field-params.startdate:collect_fct-sum"), DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"sum", 58000000F, 640000000F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        handleMatchingAggregateWithMultiCollect(post(aggregationRequest),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 58000000F, 640000000F, 6400F,13600F);
        handleMatchingAggregateWithMultiCollect(get("term:params.job:collect_field-params.startdate:collect_fct-sum:collect_field-params.age:collect_fct-max"),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, aggregationRequest.aggregations.get(0).metrics.get(1).collectField,"sum", "max", 58000000F, 640000000F, 6400F,13600F);


        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithGeocentroidCollect(post(aggregationRequest),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geocentroid", -1F, -1F, 0F, 0F);
        handleMatchingAggregateWithGeocentroidCollect(get("term:params.job:collect_field-geo_params.centroid:collect_fct-geocentroid"),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geocentroid", -1F, -1F, 0F, 0F);

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOBBOX));
        handleMatchingAggregateWithGeoBboxCollect(post(aggregationRequest),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField, "geobbox", -171F, -81F, 170F, 80F);
        handleMatchingAggregateWithGeoBboxCollect(get("term:params.job:collect_fct-geobbox:collect_field-geo_params.centroid"),
                DataSetTool.jobs.length - 1, 58, 64, aggregationRequest.aggregations.get(0).metrics.get(0).collectField,"geobbox", -171F, -81F, 170F, 80F);


        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");
        handleMatchingAggregateWithOrder(get("term:params.job:order-desc:on-count"), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");

        aggregationRequest.aggregations.get(0).on = OrderOn.field;
        handleMatchingAggregateWithOrder(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, "Dancer");
        handleMatchingAggregateWithOrder(get("term:params.job:order-desc:on-field"), DataSetTool.jobs.length - 1, 58, 64, "Dancer");

        aggregationRequest.aggregations.get(0).order = Order.asc;
        handleMatchingAggregateWithOrder(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, "Actor");
        handleMatchingAggregateWithOrder(get("term:params.job:order-asc:on-field"), DataSetTool.jobs.length - 1, 58, 64, "Actor");

        aggregationRequest.aggregations.get(0).on = OrderOn.count;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithOrder(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");
        handleMatchingAggregateWithOrder(get("term:params.job:order-desc:on-count:collect_fct-geocentroid:collect_field-geo_params.centroid"), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        aggregationRequest.aggregations.get(0).on = OrderOn.result;
        aggregationRequest.aggregations.get(0).order = Order.desc;
        handleMatchingAggregateWithOrder(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");
        handleMatchingAggregateWithOrder(get("term:params.job:collect_field-params.startdate:collect_fct-sum:order-desc:on-result"), DataSetTool.jobs.length - 1, 58, 64, "Cost Estimator");

        // FETCHGEOMETRY TESTS
        aggregationRequest.aggregations.get(0).metrics = null;
        aggregationRequest.aggregations.get(0).order = null;
        aggregationRequest.aggregations.get(0).on = null;

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CENTROID);
        handleMatchingAggregateWithCentroid(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, -1e-6F, -1e-6F, 1e-6F, 1e-6F);
        handleMatchingAggregateWithCentroid(get("term:params.job:aggregated_geometries-centroid"), DataSetTool.jobs.length - 1, 58, 64, -1e-6F, -1e-6F, 1e-6F, 1e-6F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingAggregateWithGeometry(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, -170.1F, -80.1F, 170F, 80F);
        handleMatchingAggregateWithGeometry(get("term:params.job:aggregated_geometries-bbox"), DataSetTool.jobs.length - 1, 58, 64, -170.1F, -80.1F, 170F, 80F);

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        // We need to add a sort in this test to have a constant response between aliases and not aliases
        // Fetch without sort dont give the same result with alias and not alias
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry","+params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, -171F, -81F, -159F, 1F);
        handleMatchingAggregateWithGeometry(get("term:params.job:raw_geometries-geo_params.geometry(+params.startdate)"), DataSetTool.jobs.length - 1, 58, 64, -171F, -81F, -159F, 1F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.startdate"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, 79F, 79F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("term:params.job:raw_geometries-geo_params.geometry(-params.startdate)"), DataSetTool.jobs.length - 1, 58, 64, 79F, 79F, 171F, 81F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, -161F, -81F, 41F, 1F);
        handleMatchingAggregateWithGeometry(get("term:params.job:raw_geometries-geo_params.geometry(params.age)"), DataSetTool.jobs.length - 1, 58, 64, -161F, -81F, 41F, 1F);

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry", "-params.age"));
        handleMatchingAggregateWithGeometry(post(aggregationRequest), DataSetTool.jobs.length - 1, 58, 64, -151F, -81F, 171F, 81F);
        handleMatchingAggregateWithGeometry(get("term:params.job:raw_geometries-geo_params.geometry(-params.age)"), DataSetTool.jobs.length - 1, 58, 64, -151F, -81F, 171F, 81F);
        aggregationRequest.aggregations.get(0).rawGeometries = null;

        //FETCHHITS TESTS
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("params.country", "params.startdate"));
        handleMatchingAggregateWithFetchedHits(post(aggregationRequest), DataSetTool.jobs.length - 1, 3, "params.country", "params.startdate");
        handleMatchingAggregateWithFetchedHits(get("term:params.job:fetch_hits-3(params.country,params.startdate):raw_geometries-geo_params.geometry"), DataSetTool.jobs.length - 1, 3, "params.country", "params.startdate");

        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.startdate"));
        handleMatchingAggregateWithSortedFetchedDates(post(aggregationRequest), DataSetTool.jobs.length - 1, 3, 1144900, 1263600, "params.startdate");
        handleMatchingAggregateWithSortedFetchedDates(get("term:params.job:fetch_hits-3(-params.startdate):raw_geometries-geo_params.geometry"), DataSetTool.jobs.length - 1, 3, 1144900, 1263600, "params.startdate");
        aggregationRequest.aggregations.get(0).fetchHits = null;

    }

    @Test
    public void testReturnedGeometries() throws Exception {
        /** this test aim to verify the response structure of aggregations using `aggregated_geometries` and `raw_geometries` parameters**/

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.geohash;
        aggregationRequest.aggregations.get(0).field = "geo_params.centroid";
        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);


        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX);
        handleMatchingAggregateWithAggregatedGeometries(post(aggregationRequest), 32, 1, AggregatedGeometryEnum.BBOX.value());
        handleMatchingAggregateWithAggregatedGeometries(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox"), 32, 1, AggregatedGeometryEnum.BBOX.value());


        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX, AggregatedGeometryEnum.CENTROID);
        handleMatchingAggregateWithAggregatedGeometries(post(aggregationRequest), 32 ,2, AggregatedGeometryEnum.BBOX.value(), AggregatedGeometryEnum.CENTROID.value());
        handleMatchingAggregateWithAggregatedGeometries(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox,centroid"), 32, 2, AggregatedGeometryEnum.BBOX.value(), AggregatedGeometryEnum.CENTROID.value());

        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.centroid", "-params.startdate"), new RawGeometry("geo_params.centroid"));
        handleMatchingAggregateWithRawGeometriesSorts(post(aggregationRequest), 32, 2, "geo_params.centroid", "params.startdate", "-params.startdate");
        handleMatchingAggregateWithRawGeometriesSorts(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.centroid(-params.startdate);geo_params.centroid"), 32, 2, "geo_params.centroid", "params.startdate", "-params.startdate");

        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"), new RawGeometry("geo_params.centroid"));
        handleMatchingAggregateWithRawGeometries(post(aggregationRequest), 32, 2, "geo_params.geometry", "geo_params.centroid");
        handleMatchingAggregateWithRawGeometries(get("geohash:geo_params.centroid:interval-1:raw_geometries-geo_params.geometry;geo_params.centroid(params.startdate)"), 32, 2, "geo_params.geometry", "geo_params.centroid");

        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.BBOX, AggregatedGeometryEnum.CENTROID);
        handleMatchingAggregateWithMixedGeometries(post(aggregationRequest), 32, 4,  AggregatedGeometryEnum.BBOX.value(), AggregatedGeometryEnum.CENTROID.value(), "geo_params.geometry", "geo_params.centroid");
        handleMatchingAggregateWithMixedGeometries(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox,centroid:raw_geometries-geo_params.geometry;geo_params.centroid"), 32, 4,  AggregatedGeometryEnum.BBOX.value(), AggregatedGeometryEnum.CENTROID.value(), "geo_params.geometry", "geo_params.centroid");

        aggregationRequest.aggregations.get(0).rawGeometries = null;
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));

        handleMatchingAggregateWithGeoMetric(post(aggregationRequest), 0);
        handleMatchingAggregateWithGeoMetric(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox,centroid:collect_field-geo_params.centroid:collect_fct-geocentroid"), 0);

        aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.other_geopoint", CollectionFunction.GEOCENTROID));
        handleMatchingAggregateWithGeoMetric(post(aggregationRequest), 1, "geo_params_other_geopoint");
        handleMatchingAggregateWithGeoMetric(get("geohash:geo_params.centroid:interval-1:aggregated_geometries-bbox,centroid:collect_field-geo_params.centroid:collect_fct-geocentroid:collect_field-geo_params.other_geopoint:collect_fct-geocentroid"), 1, "geo_params_other_geopoint");

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
        handleMultiMatchingGeohashAggregate(post(aggregationRequest), 32);
        handleMultiMatchingGeohashAggregate(
                given().param("agg", "geohash:geo_params.centroid:interval-1")
                        .param("agg", "term:params.job")
                        .when().get(getUrlPath("geodata"))
                        .then(), 32);

        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).interval.value = 100000;

        aggregationRequest.aggregations.get(1).type = AggregationTypeEnum.datehistogram;
        aggregationRequest.aggregations.get(1).field = "params.startdate";
        aggregationRequest.aggregations.get(1).interval = new Interval(1, UnitEnum.minute); //"1minute";
        handleMultiMatchingAggregate(post(aggregationRequest), 6);
        handleMultiMatchingAggregate(
                given().param("agg", "histogram:params.startdate:interval-100000")
                        .param("agg", "datehistogram:interval-1minute")
                        .when().get(getUrlPath("geodata"))
                        .then(), 6);
    }

    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------

    @Test
    public void testInvalidAggregateParameters() throws Exception {
        //INVALID TYPE
        handleInvalidParameters(get("foobar"));
        handleInvalidParameters(get("foobar:params.job"));
        InvalidAggregationsRequest invalidAggregationRequest = new InvalidAggregationsRequest();
        invalidAggregationRequest.filter = new Filter();
        invalidAggregationRequest.filter.righthand = false;
        invalidAggregationRequest.invalidAggregations = new ArrayList<>();
        invalidAggregationRequest.invalidAggregations.add(new InvalidAggregation());
        invalidAggregationRequest.invalidAggregations.get(0).type = "foobar";
        invalidAggregationRequest.invalidAggregations.get(0).field = "params.job";
        handleInvalidParameters(post(invalidAggregationRequest));

        //INVALID GEOHASH
        for (AggregationTypeEnum geoAggTypeEnum : GEO_AGGREGATION_TYPE_ENUMS) {
            String geoAggType = geoAggTypeEnum.name();
            aggregationRequest.aggregations.get(0).type = geoAggTypeEnum;
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType));

            aggregationRequest.aggregations.get(0).field = "params.job";
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":params.job"));

            aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":params.job:interval-1"));

            aggregationRequest.aggregations.get(0).field = "geo_centroid";
            switch(geoAggTypeEnum) {
                case geohash:
                    aggregationRequest.aggregations.get(0).interval = new Interval(0, null);
                    handleInvalidParameters(post(aggregationRequest));
                    handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-0"));

                    aggregationRequest.aggregations.get(0).interval = new Interval(13, null);
                    handleInvalidParameters(post(aggregationRequest));
                    handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-13"));
                    break;
                case geotile:
                    aggregationRequest.aggregations.get(0).interval = new Interval(30, null);
                    handleInvalidParameters(post(aggregationRequest));
                    handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-30"));
                    break;
            }

            aggregationRequest.aggregations.get(0).interval = new Interval(1, null);

            aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("toto.geometry"));
            handleNotFoundField(post(aggregationRequest));
            handleNotFoundField(get(geoAggType+":geo_params.centroid:interval-1:raw_geometries-toto.geometry"));
            aggregationRequest.aggregations.get(0).rawGeometries = null;

            aggregationRequest.aggregations.get(0).order = Order.asc;
            aggregationRequest.aggregations.get(0).on = OrderOn.count;
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:order-asc:on-count"));

            aggregationRequest.aggregations.get(0).on = OrderOn.field;
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:order-asc:on-field"));

            aggregationRequest.aggregations.get(0).order = Order.desc;
            aggregationRequest.aggregations.get(0).on = OrderOn.result;
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:order-desc:on-result"));

            aggregationRequest.aggregations.get(0).order = Order.asc;
            aggregationRequest.aggregations.get(0).on = OrderOn.result;
            aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
            aggregationRequest.aggregations.get(0).metrics.add(new Metric("geo_params.centroid", CollectionFunction.GEOCENTROID));
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:order-asc:on-result:collect_fct-geocentroid:collect_field-geo_params.centroid"));

            aggregationRequest.aggregations.get(0).order = null;
            aggregationRequest.aggregations.get(0).on = null;
            aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
            aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.job", CollectionFunction.GEOCENTROID));
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:collect_field-params.job:collect_fct-geocentroid"));

            aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
            aggregationRequest.aggregations.get(0).metrics.add(new Metric("foo", null));
            handleInvalidParameters(post(aggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:collect_field-foo"));

            aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
            aggregationRequest.aggregations.get(0).include = "foo";
            handleInvalidParameters(post(invalidAggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:include-foo"));

            invalidAggregationRequest.invalidAggregations.get(0).type = geoAggType;
            invalidAggregationRequest.invalidAggregations.get(0).field = "geo_params.centroid";
            invalidAggregationRequest.invalidAggregations.get(0).interval = new Interval(1, null);
            invalidAggregationRequest.invalidAggregations.get(0).metrics = new ArrayList<>();
            invalidAggregationRequest.invalidAggregations.get(0).metrics.add(new InvalidMetric("foo", "bar"));
            handleInvalidParameters(post(invalidAggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:collect_field-foo:collect_fct-bar"));
            invalidAggregationRequest.invalidAggregations.get(0).metrics = null;

            invalidAggregationRequest.invalidAggregations.get(0).aggregatedGeometries = Arrays.asList("boo");
            handleInvalidParameters(post(invalidAggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid:interval-1:aggregated_geometries-boo"));

            aggregationRequest.aggregations.get(0).include = null;
            aggregationRequest.aggregations.get(0).interval = null;
            handleInvalidParameters(post(invalidAggregationRequest));
            handleInvalidParameters(get(geoAggType+":geo_params.centroid"));
        }

        // INVALID DATE HISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.datehistogram;
        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.day);// TODO: was incorrect (iday), was it on purpose?
        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.job:interval-1day"));


        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).include = "foo";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.startdate:interval-1day:include-foo"));

        aggregationRequest.aggregations.get(0).include = null;
        aggregationRequest.aggregations.get(0).interval = null;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.startdate"));


        aggregationRequest.aggregations.get(0).interval = new Interval(1.5, UnitEnum.day); // "1day";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.startdate:interval-1.5day"));

        aggregationRequest.aggregations.get(0).interval = new Interval(null, UnitEnum.day); // "day";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.startdate:interval-day"));

        aggregationRequest.aggregations.get(0).interval = new Interval(1, UnitEnum.day); // "1day";
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("datehistogram:params.startdate:interval-1day:aggregated_geometries-cell"));
        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;

        // INVALID HISTOGRAM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.histogram;
        aggregationRequest.aggregations.get(0).field = "params.startdate";
        aggregationRequest.aggregations.get(0).interval = new Interval(100000, null);
        aggregationRequest.aggregations.get(0).format = "yyyyMMdd";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.startdate:interval-100000:format-yyyyMMdd"));

        aggregationRequest.aggregations.get(0).format = null;
        aggregationRequest.aggregations.get(0).include = "foo";
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.startdate:interval-100000:include-foo"));

        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).format = null;
        aggregationRequest.aggregations.get(0).include = null;
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.job"));

        aggregationRequest.aggregations.get(0).interval = new Interval(100000, null); // "1day";
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("histogram:params.startdate:interval-100000:aggregated_geometries-cell"));
        aggregationRequest.aggregations.get(0).aggregatedGeometries = null;
        aggregationRequest.aggregations.get(0).interval = null;

        // INVALID TERM
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("term:params.job:interval-1"));

        // INVALID AGGREGATED_GEOMETRY
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).interval = null;
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CELL);
        handleInvalidParameters(post(aggregationRequest));
        handleInvalidParameters(get("term:params.job:aggregated_geometries-cell"));

        invalidAggregationRequest.invalidAggregations.get(0).type = "term";
        invalidAggregationRequest.invalidAggregations.get(0).field = "params.job";
        invalidAggregationRequest.invalidAggregations.get(0).interval = null;
        invalidAggregationRequest.invalidAggregations.get(0).aggregatedGeometries = Arrays.asList("boo");
        handleInvalidParameters(post(invalidAggregationRequest));
        handleInvalidParameters(get("term:params.job:aggregated_geometries-boo"));
        invalidAggregationRequest.invalidAggregations.get(0).aggregatedGeometries = null;

        // INVALID FETCHGEOMETRY
        // Non-existing field
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("foo", "params.age"));
        aggregationRequest.aggregations.get(0).aggregatedGeometries = Arrays.asList(AggregatedGeometryEnum.CENTROID);
        handleNotFoundField(post(aggregationRequest));
        handleNotFoundField(get("term:params.job:fetch_hits-3(foo, params.age):aggregated_geometries-centroid"));

        // Invalid syntax of the include part
        invalidAggregationRequest.invalidAggregations.get(0).type = "term";
        invalidAggregationRequest.invalidAggregations.get(0).field = "params.job";
        invalidAggregationRequest.invalidAggregations.get(0).fetchHits = "boo";
        handleInvalidParameters(post(invalidAggregationRequest));
        handleInvalidParameters(get("term:params.job:fetch_hits-3(params.age"));
        handleInvalidParameters(get("term:params.job:fetch_hits-3[params.age]"));
        invalidAggregationRequest.invalidAggregations.get(0).fetchHits = null;

    }

    @Test
    public void testAggregateParametersWithUnavailableColums() throws Exception {

        //aggregation field is filtered
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).field = "params.job";
        handleUnavailableColumn(post(aggregationRequest, "id"));
        handleUnavailableColumn(get("term:params.job", "id"));

        //fetch geometry field is filtered
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry","params.age"));
        handleUnavailableColumn(post(aggregationRequest, "params.job"));
        handleUnavailableColumn(get("term:params.job:raw_geometries-geo_params.geometry(params.age)", "params.job"));

        //metric fields are filtered
        aggregationRequest.aggregations.get(0).type = AggregationTypeEnum.term;
        aggregationRequest.aggregations.get(0).field = "params.job";
        aggregationRequest.aggregations.get(0).rawGeometries = null;

        aggregationRequest.aggregations.get(0).metrics = new ArrayList<>();
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.age", CollectionFunction.MAX));
        aggregationRequest.aggregations.get(0).metrics.add(new Metric("params.startdate", CollectionFunction.SUM));
        handleUnavailableColumn(post(aggregationRequest, "params.job,params.startdate"));
        handleUnavailableColumn(get("term:params.job:collect_field-params.age:collect_fct-max:collect_field-params.startdate:collect_fct-sum", "params.job,params.startdate"));

        //fetch hits are filtered
        aggregationRequest.aggregations.get(0).metrics = null;
        aggregationRequest.aggregations.get(0).rawGeometries = Arrays.asList(new RawGeometry("geo_params.geometry"));
        aggregationRequest.aggregations.get(0).fetchHits = new HitsFetcher(3, Arrays.asList("-params.age", "-params.startdate"));
        handleUnavailableColumn(post(aggregationRequest, "params.job,params.startdate"));
        handleUnavailableColumn(get("term:params.job:fetch_hits-3(-params.age,-params.startdate):raw_geometries-geo_params.geometry", "params.job,params.startdate"));
        aggregationRequest.aggregations.get(0).fetchHits = null;
    }

    @Test
    public void testNotImplementedAggregateParameters() throws Exception {
        //NOT IMPLEMENTED PARAMETER
        for (AggregationTypeEnum geoAggTypeEnum : GEO_AGGREGATION_TYPE_ENUMS) {
            aggregationRequest.aggregations.get(0).type = geoAggTypeEnum;
            aggregationRequest.aggregations.get(0).field = "geo_centroid";
            aggregationRequest.aggregations.get(0).interval = new Interval(1, null);
            aggregationRequest.aggregations.get(0).size = "1";
            handleNotImplementedParameters(post(aggregationRequest));
            handleNotImplementedParameters(get(geoAggTypeEnum.name()+":geo_params.centroid:interval-1:size-1"));
        }
    }

    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float featureCollectMin, float featureCollectMax) throws Exception;

    protected abstract void handleMatchingGeohashAggregateCenter(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception;

    protected abstract void handleMatchingAggregateWithGeometry(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception;

    protected abstract void handleMatchingAggregateWithAggregatedGeometries(ValidatableResponse then, int featuresSize, int nbGeometries, String... geometries);

    protected abstract void handleMatchingAggregateWithRawGeometries(ValidatableResponse then, int featuresSize, int nbGeometries, String... geometries);

    protected abstract void handleMatchingAggregateWithRawGeometriesSorts(ValidatableResponse then, int featuresSize, int nbGeometries, String geometry, String... sorts);

    protected abstract void handleMatchingAggregateWithMixedGeometries(ValidatableResponse then, int featuresSize, int nbGeometries, String... geometries);

    protected abstract void handleMatchingAggregateWithGeoMetric(ValidatableResponse then, int nbMetrics, String... fields);

    protected abstract void handleMatchingAggregateWithFetchedHits(ValidatableResponse then, int featuresSize, int nbhits, String... items) throws Exception;

    protected abstract void handleMatchingHistogramAggregateWithFetchedHits(ValidatableResponse then, int featuresSize, int nbhits, String... items) throws Exception;

    protected abstract void handleMatchingAggregateWithSortedFetchedDates(ValidatableResponse then, int featuresSize, int nbhits, int minDate, int maxDate, String item) throws Exception;

    protected abstract void handleMatchingHistogramAggregateWithSortedFetchedDates(ValidatableResponse then, int featuresSize, int nbhits, int minDate, int maxDate, String item) throws Exception;

    protected abstract void handleMatchingAggregateWithCentroid(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float left, float bottom, float right, float top) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithGeocentroidBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float left, float bottom, float right, float top) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithGeoBboxBucket(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int elementsSize, float left, float bottom, float right, float top) throws Exception;

    protected abstract void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;

    protected abstract void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception;

    protected abstract void handleMatchingDateHistogramAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int firstKey) throws Exception;

    protected abstract void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception;

    protected abstract void handleMatchingAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float featureCollectMin, float featureCollectMax) throws Exception;

    protected abstract void handleMatchingAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField1, String collectField2, String collectFct1, String collecFct2, float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception;

    protected abstract void handleMatchingGeohashAggregateWithMultiCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField1, String collectField2, String collectFct1, String collecFct2, float featureCollectMin1, float featureCollectMax1, float featureCollectMin2, float featureCollectMax2) throws Exception;

    protected abstract void handleMatchingAggregateWithGeocentroidCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float centroidLonMin, float centroidLatMin, float centroidLonMax, float centroidLatMax) throws Exception;


    protected abstract void handleMatchingAggregateWithGeoBboxCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectField, String collectFct, float left, float bottom, float right, float top) throws Exception;

    protected abstract void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;

    protected abstract void handleSumOtherCountsExistence(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, int sumOtherDocCounts) throws Exception;

    protected abstract void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception;

    protected abstract void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception;

    protected ValidatableResponse post(Request request) {
        return given().contentType("application/json;charset=utf-8").body(request)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    protected ValidatableResponse post(Request request, String columnFilter) {
        return given().contentType("application/json;charset=utf-8")
                .body(request)
                .header(COLUMN_FILTER, columnFilter)
                .when().post(getUrlPath("geodata"))
                .then();
    }

    protected RequestSpecification handleGetRequest(RequestSpecification req){return req;}

    private ValidatableResponse get(Object paramValue) {
        RequestSpecification req = given();
        req = handleGetRequest(req);
        return req.param("agg", paramValue)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    private ValidatableResponse get(Object paramValue, String columnFilter) {
        RequestSpecification req = given();
        req = handleGetRequest(req);
        return req.param("agg", paramValue)
                .header(COLUMN_FILTER, columnFilter)
                .when().get(getUrlPath("geodata"))
                .then();
    }

    @JsonSnakeCase
    public class InvalidMetric {
        public String collectField;
        public String collecFct;

        public InvalidMetric(String collectField, String collecFct) {
            this.collecFct = collecFct;
            this.collectField = collectField;
        }
    }

    @JsonSnakeCase
    public class InvalidAggregation {
        public String type;
        public String field;
        public Interval interval;
        public String format;
        public List<InvalidMetric> metrics;
        public Order order;
        public OrderOn on;
        public String size;
        public List<String> aggregatedGeometries;
        public String fetchHits;

        public InvalidAggregation() {
        }
    }

    public class InvalidAggregationsRequest extends Request {
        public List<InvalidAggregation> invalidAggregations;
    }
}
