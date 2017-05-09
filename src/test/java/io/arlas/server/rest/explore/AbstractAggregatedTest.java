package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.arlas.server.rest.DataSetTool;
import io.restassured.response.ValidatableResponse;

public abstract class AbstractAggregatedTest extends AbstractFilteredTest {
    
    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testGeohashAggregate() throws Exception {
        //GEOHASH
        handleMatchingGeohashAggregate(
                given().param("agg", "geohash:centroid:interval-3")
                .when().get(getUrlPath("geodata"))
                .then(), 595, 1);
        handleMatchingGeohashAggregate(
                given().param("agg", "geohash:centroid:interval-1")
                .when().get(getUrlPath("geodata"))
                .then(),32, 16, 25);
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
                .then(),32, 16, 25, "sum", 12641200F, 28305000F);
    }
    
    @Test
    public void testDateHistogramAggregate() throws Exception {        
        //DATEHISTOGRAM
        handleMatchingAggregate(
                given().param("agg", "datehistogram:startdate:interval-1day")
                .when().get(getUrlPath("geodata"))
                .then(), 1, 595);
        handleMatchingAggregate(
                given().param("agg", "datehistogram:startdate:interval-1minute")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104);
        handleMatchingAggregateWithCollect(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-avg")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104, "avg", 769433F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-cardinality")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104, "cardinality", 1F, 72F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-max")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104, "max", 772800F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-min")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104, "min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-sum")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104, "sum", 1263600F, 102986100F);
        handleMatchingAggregate(
                given().param("agg", "datehistogram:startdate:interval-1minute:format-yyyyMMdd")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"19700101");
        handleMatchingAggregateWithOrder(
                given().param("agg", "datehistogram:startdate:interval-1minute:order-asc:on-count")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(
                given().param("agg", "datehistogram:startdate:interval-1minute:order-desc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"1970-01-01-00:21:00");
        handleMatchingAggregateWithOrder(
                given().param("agg", "datehistogram:startdate:interval-1minute:order-asc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"1970-01-01-00:12:00");
        handleMatchingAggregateWithOrder(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-sum:order-desc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"1970-01-01-00:16:00");
        handleMatchingAggregateWithOrder(
                given().param("agg", "datehistogram:startdate:interval-1minute:collect_field-startdate:collect_fct-sum:order-asc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),10, 1, 104,"1970-01-01-00:21:00");
    }
    
    @Test
    public void testHistogramAggregate() throws Exception {        
        //HISTOGRAM
        handleMatchingAggregate(
                given().param("agg", "histogram:startdate:interval-2000000")
                .when().get(getUrlPath("geodata"))
                .then(), 1, 595);
        handleMatchingAggregate(
                given().param("agg", "histogram:startdate:interval-100000")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176);
        handleMatchingAggregateWithCollect(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-avg")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176, "avg", 786078F, 1226267F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-cardinality")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176, "cardinality", 14F, 111F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-max")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176, "max", 799800F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-min")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176, "min", 763600F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-sum")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176, "sum", 11005100F, 170040600F);
        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:startdate:interval-100000:order-asc:on-count")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"700000");
        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:startdate:interval-100000:order-desc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"1200000");
        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:startdate:interval-100000:order-asc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"700000");
        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-sum:order-desc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"1000000");
        handleMatchingAggregateWithOrder(
                given().param("agg", "histogram:startdate:interval-100000:collect_field-startdate:collect_fct-sum:order-asc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),6, 14, 176,"700000");
    }
    
    @Test
    public void testTermAggregate() throws Exception {        
        //TERM
        handleMatchingAggregate(
                given().param("agg", "term:job")
                .when().get(getUrlPath("geodata"))
                .then(), DataSetTool.jobs.length-1, 58, 64);
        handleMatchingAggregateWithCollect(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-avg")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64, "avg", 1000000F, 1000000F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-cardinality")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64, "cardinality", 44F, 49F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-max")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64, "max", 1166400F, 1263600F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-min")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64, "min", 763600F, 840000F);
        handleMatchingAggregateWithCollect(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-sum")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64, "sum", 58000000F, 640000000F);
        handleMatchingAggregateWithOrder(
                given().param("agg", "term:job:order-desc:on-count")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");
        handleMatchingAggregateWithOrder(
                given().param("agg", "term:job:order-desc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64,"Dancer");
        handleMatchingAggregateWithOrder(
                given().param("agg", "term:job:order-asc:on-field")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64,"Actor");
        handleMatchingAggregateWithOrder(
                given().param("agg", "term:job:collect_field-startdate:collect_fct-sum:order-desc:on-result")
                .when().get(getUrlPath("geodata"))
                .then(),DataSetTool.jobs.length-1, 58, 64,"Cost Estimator");
    }
    
    @Test
    public void testMultiAggregate() throws Exception {
        handleMultiMatchingGeohashAggregate(
                given().param("agg", "geohash:centroid:interval-1")
                .param("agg", "term:job")
                .when().get(getUrlPath("geodata"))
                .then(),32);
        handleMultiMatchingAggregate(
                given().param("agg", "histogram:startdate:interval-100000")
                .param("agg", "datehistogram:startdate:interval-1minute")
                .when().get(getUrlPath("geodata"))
                .then(),6);
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
        
        handleInvalidParameters(
                given().param("agg", "datehistogram:job:interval-1day")
                .when().get(getUrlPath("geodata"))
                .then());
        
        handleInvalidParameters(
                given().param("agg", "histogram:startdate:interval-100000:format-yyyyMMdd")
                .when().get(getUrlPath("geodata"))
                .then());
        handleInvalidParameters(
                given().param("agg", "histogram:job")
                .when().get(getUrlPath("geodata"))
                .then());
        
        handleInvalidParameters(
                given().param("agg", "term:job:interval-1")
                .when().get(getUrlPath("geodata"))
                .then());
    }

    @Test
    public void testNotImplementedAggregateParameters() throws Exception {
        //NOT IMPLEMENTED PARAMETER
        handleNotImplementedParameters(
                given().param("agg", "geohash:centroid:interval-1:size-1")
                        .when().get(getUrlPath("geodata"))
                        .then());
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
}
