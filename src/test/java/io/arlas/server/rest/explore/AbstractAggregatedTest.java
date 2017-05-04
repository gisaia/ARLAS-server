package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.restassured.response.ValidatableResponse;

public abstract class AbstractAggregatedTest extends AbstractFilteredTest {
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testGeohashAggregate() throws Exception {
        handleMatchingGeohashAggregate(
                given().param("agg", "geohash:centroid:interval-3")
                .when().get(getUrlPath("geodata"))
                .then(), 595, 1);
        handleMatchingGeohashAggregate(
                given().param("agg", "geohash:centroid:interval-1")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25);
        
        //COLLECT TEST
        handleMatchingGeohashAggregateWithCollect(
                given().param("agg", "geohash:centroid:interval-1:collect_field-startdate:collect_fct-avg")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25, "avg", 790075F, 1230075F);
        handleMatchingGeohashAggregateWithCollect(
                given().param("agg", "geohash:centroid:interval-1:collect_field-startdate:collect_fct-cardinality")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25, "cardinality", 16F, 25F);
        handleMatchingGeohashAggregateWithCollect(
                given().param("agg", "geohash:centroid:interval-1:collect_field-startdate:collect_fct-max")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25, "max", 817000F, 1263600F);
        handleMatchingGeohashAggregateWithCollect(
                given().param("agg", "geohash:centroid:interval-1:collect_field-startdate:collect_fct-min")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25, "min", 763600F, 1197000F);
        handleMatchingGeohashAggregateWithCollect(
                given().param("agg", "geohash:centroid:interval-1:collect_field-startdate:collect_fct-sum")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25, "sum", 12641200, 28305000);
    }
    
    @Test
    public void testGeoashMultiAggregate() throws Exception {
        
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    
    @Test
    public void testInvalidAggregateParameters() throws Exception {
        //INVALID TYPE
        handleInvalidParameters(
                given().param("agg", "foobar")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "foobar:job")
                .when().get(getUrlPath("geodata"))
                .then());
        
        //INVALID GEOHASH
        handleInvalidParameters(
                given().param("agg", "geohash")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:job")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:job:interval-1")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-0")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-13")
                .when().get(getUrlPath("geodata"))
                .then());
        //TODO NO SUPPORTED
        /*handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:size-1")
                .when().get(getUrlPath("geodata"))
                .then());*/
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:order-asc:on-count")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:order-asc:on-field")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:order-desc:on-result")
                .when().get(getUrlPath("geodata"))
                .then());
        
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:collect_field-foo")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "geohash:centroid:interval-1:collect_field-foo:collect_fct-bar")
                .when().get(getUrlPath("geodata"))
                .then());
    }
    
    //----------------------------------------------------------------
    //---------------------- SPECIFIC BEHAVIORS ----------------------
    //----------------------------------------------------------------
    
    protected abstract void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception;
    protected abstract void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception;
    protected abstract void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin, float featureCollectMax) throws Exception;
}
