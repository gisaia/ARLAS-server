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

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

public abstract class AbstractGeohashTiledTest extends AbstractAggregatedTest {

    @Test
    public void testGeohash() throws Exception {
        //GEOHASH
        // precision = geohashLength OR precision < geohashLength  ==> we should have one feature maximum
        handleGeohashTileGreaterThanPrecision(tileGet("geohash:geo_params.centroid:interval-2", "yn"), 2, "yn");
        handleGeohashTileGreaterThanPrecision(tileGet("geohash:geo_params.centroid:interval-1", "ynp"), 1, "ynp");

        // precision > geohashLength  ==> we could have more than one feature
        handleGeohashTileLessThanPrecision(tileGet("geohash:geo_params.centroid:interval-3", "yn"), 2, "yn");

        String pwithin = "geo_params.centroid:within:98,79,101,81";
        handleGeohashTileLessThanPrecision(tilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "yn"), 1, "yn");

        pwithin = "geo_params.centroid:within:98,79,101,81;geo_params.centroid:within:108,79,111,81";
        handleGeohashTileLessThanPrecision(tilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "y"), 2, "yn");

        pwithin = "geo_params.centroid:within:98,79,101,81";
        String pwithin2 = "geo_params.centroid:within:98,79,111,81";
        handleGeohashTileLessThanPrecision(tilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin, pwithin2), "y"), 1, "yn");

        pwithin = "geo_params.centroid:within:180,0,-165,5";
        handleGeohashTileLessThanPrecision(tilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "80"), 1, "80");

        pwithin = "geo_params.centroid:within:-5,0,0,5";
        handleGeohashTileDisjointFromPwithin(tilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "yn"));

    }

    @Test
    public void testGeotile() throws Exception {
        //GEOTILE
        // precision =  '{z}'  ==> we should have one feature maximum
        handleGeotileGreaterThanPrecision(tileGet("geotile:geo_params.centroid:interval-5", "5/4/3"), 1, "5/4/3");

        // precision > '{z}'  ==> we could have more than one feature
        handleGeotileLessThanPrecision(tileGet("geotile:geo_params.centroid:interval-6", "4/3/3"), 2, "4/3/3");

        //GEOTILE 3/1/1 [west=-135.0, south=66.51326044311186, east=-90.0, north=79.17133464081945 ]
        //GEOTILE 4/3/3 [west=-112.5, south=66.51326044311186, east=-90.0, north=74.01954331150228]
        String pwithin = "geo_params.centroid:within:-111,68,-109,71";
        handleGeotileLessThanPrecision(tilePwithinGet("geotile:geo_params.centroid:interval-4", Arrays.asList(pwithin), "3/1/1"), 1, "4/3/3");

        pwithin = "geo_params.centroid:within:-111,68,-109,71;geo_params.centroid:within:-101,68,-99,71"; // OR
        handleGeotileLessThanPrecision(tilePwithinGet("geotile:geo_params.centroid:interval-4", Arrays.asList(pwithin), "3/1/1"), 1, "4/3/3");

        pwithin = "geo_params.centroid:within:-111,68,-109,71";
        String pwithin2 = "geo_params.centroid:within:-111,68,-99,71"; // AND
        handleGeotileLessThanPrecision(tilePwithinGet("geotile:geo_params.centroid:interval-4", Arrays.asList(pwithin, pwithin2), "3/1/1"), 1, "4/3/3");

        pwithin = "geo_params.centroid:within:-5,0,0,5";
        handleGeohashTileDisjointFromPwithin(tilePwithinGet("geotile:geo_params.centroid:interval-4", Arrays.asList(pwithin), "4/3/3"));

    }



    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract void handleGeohashTileGreaterThanPrecision(ValidatableResponse then, int count, String geohash) throws Exception;

    protected abstract void handleGeohashTileLessThanPrecision(ValidatableResponse then, int featuresSize, String geohash) throws Exception;

    protected abstract void handleGeotileGreaterThanPrecision(ValidatableResponse then, int count, String geohash) throws Exception;

    protected abstract void handleGeotileLessThanPrecision(ValidatableResponse then, int featuresSize, String geohash) throws Exception;

    protected abstract void handleGeohashTileDisjointFromPwithin(ValidatableResponse then) throws Exception;

    private ValidatableResponse tileGet(Object paramValue, String tile) {
        return given().param("agg", paramValue)
                .when().get(getTileUrlPath("geodata", tile))
                .then();
    }

    private ValidatableResponse tilePwithinGet(Object paramValue, List<String> pwithinValues, String tile) {
        RequestSpecification req = given().param("agg", paramValue);
        for (String pwithin : pwithinValues) {
            req = req.param("f", pwithin);
        }
        return req.when().get(getTileUrlPath("geodata", tile))
                .then();
    }

    protected abstract String getTileUrlPath(String collection, String tile);

}
