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

import io.arlas.server.model.enumerations.OperatorEnum;
import io.arlas.server.model.request.Expression;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

public abstract class AbstractGeohashTiledTest extends AbstractAggregatedTest {

    @Test
    public void testGeohashTile() throws Exception {
        //GEOHASH
        // precision = geohashLength OR precision < geohashLength  ==> we should have one feauture maximum
        handleGeohashTileGreaterThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-2", "yn"), 2, "yn");
        handleGeohashTileGreaterThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-1", "ynp"), 1, "ynp");

        // precision > geohashLength  ==> we could have more than one feature
        handleGeohashTileLessThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-3", "yn"), 2, "yn");

        String pwithin = "geo_params.centroid:within:98,79,101,81";
        handleGeohashTileLessThanPrecision(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "yn"), 1, "yn");

        pwithin = "geo_params.centroid:within:98,79,101,81;geo_params.centroid:within:108,79,111,81";
        handleGeohashTileLessThanPrecision(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "y"), 2, "yn");

        pwithin = "geo_params.centroid:within:98,79,101,81";
        String pwithin2 = "geo_params.centroid:within:98,79,111,81";
        handleGeohashTileLessThanPrecision(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin, pwithin2), "y"), 1, "yn");

        pwithin = "geo_params.centroid:within:180,0,-165,5";
        handleGeohashTileLessThanPrecision(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "80"), 1, "80");

        pwithin = "geo_params.centroid:within:-5,0,0,5";
        handleGeohashTileDisjointFromPwithin(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", Arrays.asList(pwithin), "yn"));

    }



    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract void handleGeohashTileGreaterThanPrecision(ValidatableResponse then, int count, String geohash) throws Exception;

    protected abstract void handleGeohashTileLessThanPrecision(ValidatableResponse then, int featuresSize, String geohash) throws Exception;

    protected abstract void handleGeohashTileDisjointFromPwithin(ValidatableResponse then) throws Exception;

    private ValidatableResponse geohashTileGet(Object paramValue, String geohash) {
        return given().param("agg", paramValue)
                .when().get(getGeohashUrlPath("geodata", geohash))
                .then();
    }

    private ValidatableResponse geohashTilePwithinGet(Object paramValue, List<String> pwithinValues, String geohash) {
        RequestSpecification req = given().param("agg", paramValue);
        for (String pwithin : pwithinValues) {
            req = req.param("f", pwithin);
        }
        return req.when().get(getGeohashUrlPath("geodata", geohash))
                .then();
    }

    protected abstract String getGeohashUrlPath(String collection, String geohash);

}
