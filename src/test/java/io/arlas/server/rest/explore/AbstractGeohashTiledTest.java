package io.arlas.server.rest.explore;

import io.restassured.response.ValidatableResponse;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public abstract class AbstractGeohashTiledTest extends AbstractAggregatedTest {

    @Test
    public void testGeohashTile() throws Exception {
        //GEOHASH
        // precision = geohashLength OR precision < geohashLength  ==> we should have one feauture maximum
        handleGeohashTileGreaterThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-2", "yn"), 2, "yn");
        handleGeohashTileGreaterThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-1", "ynp"),  1, "ynp");

        // precision > geohashLength  ==> we could have more than one feature
        handleGeohashTileLessThanPrecision(geohashTileGet("geohash:geo_params.centroid:interval-3", "yn"), 2, "yn");

        String pwithin = "81,98,79,101";
        handleGeohashTileLessThanPrecision(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", pwithin,"yn"), 1, "yn");

        pwithin = "5,-5,0,0";
        handleGeohashTileDisjointFromPwithin(geohashTilePwithinGet("geohash:geo_params.centroid:interval-3", pwithin,"yn"));

    }

    @Test
    public void testInvalidGeohashTile() throws Exception {
        handleInvalidGeohashTile(geohashTileGet("geohash:geo_params.centroid:interval-3", "ar"));
    }

    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------

    protected abstract void handleGeohashTileGreaterThanPrecision(ValidatableResponse then, int count, String geohash) throws Exception;
    protected abstract void handleGeohashTileLessThanPrecision(ValidatableResponse then, int featuresSize, String geohash) throws Exception;
    protected abstract void handleGeohashTileDisjointFromPwithin(ValidatableResponse then) throws Exception;
    protected abstract void handleInvalidGeohashTile(ValidatableResponse then) throws Exception;

    private ValidatableResponse geohashTileGet(Object paramValue, String geohash){
        return given().param("agg", paramValue)
                .when().get(getGeohashUrlPath("geodata", geohash))
                .then();
    }
    private ValidatableResponse geohashTilePwithinGet(Object paramValue, String pwithinValue, String geohash){
        return given().param("agg", paramValue).param("pwithin", pwithinValue)
                .when().get(getGeohashUrlPath("geodata", geohash))
                .then();
    }

    protected abstract String getGeohashUrlPath(String collection, String geohash);

}
