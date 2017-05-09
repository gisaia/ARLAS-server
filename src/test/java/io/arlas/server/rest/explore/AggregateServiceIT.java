package io.arlas.server.rest.explore;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class AggregateServiceIT extends AbstractAggregatedTest {
    
    @Override
    protected String getUrlPath(String collection) {
        return "/explore/"+collection+"/_aggregate";
    }
    
    //----------------------------------------------------------------
    //----------------------- AGGREGATE PART -------------------------
    //----------------------------------------------------------------
    
    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCount, featureCount);
    }
    
    @Override
    protected void handleMatchingGeohashAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(featuresSize))
        .body("elements.count", everyItem(greaterThanOrEqualTo(featureCountMin)))
        .body("elements.count", everyItem(lessThanOrEqualTo(featureCountMax)));
    }
    
    @Override
    protected void handleMatchingGeohashAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
            float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregate(then,featuresSize,featureCountMin,featureCountMax);
        then
        .body("elements.elements[0].metric.value", everyItem(greaterThanOrEqualTo(featureCollectMin)))
        .body("elements.elements[0].metric.value", everyItem(lessThanOrEqualTo(featureCollectMax)))
        .body("elements.elements[0].name", everyItem(equalTo(collectFct)))
        .body("elements.elements[0].metric.type", everyItem(equalTo(collectFct)));
    }
    
    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCount) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCount, featureCount);
    }

    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax) throws Exception {
        handleMatchingGeohashAggregate(then, featuresSize, featureCountMin, featureCountMax);
    }

    @Override
    protected void handleMatchingAggregateWithCollect(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String collectFct, float featureCollectMin,
            float featureCollectMax) throws Exception {
        handleMatchingGeohashAggregateWithCollect(then, featuresSize, featureCountMin, featureCountMax, collectFct, featureCollectMin, featureCollectMax);
    }
    
    @Override
    protected void handleMatchingAggregate(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String keyAsString) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements.keyAsString", everyItem(equalTo(keyAsString)));
    }

    @Override
    protected void handleMatchingAggregateWithOrder(ValidatableResponse then, int featuresSize, int featureCountMin, int featureCountMax, String firstKey) throws Exception {
        handleMatchingAggregate(then, featuresSize, featureCountMin, featureCountMax);
        then.body("elements[0].keyAsString", equalTo(firstKey));
    }
    
    @Override
    protected void handleMultiMatchingAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        handleMultiMatchingGeohashAggregate(then, featuresSize);
    }

    @Override
    protected void handleMultiMatchingGeohashAggregate(ValidatableResponse then, int featuresSize) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(featuresSize))
        .body("elements.elements.elements", hasSize(featuresSize))
        .body("elements.elements.elements", hasSize(featuresSize));
    }

    //----------------------------------------------------------------
    //----------------------- FILTER PART ----------------------------
    //----------------------------------------------------------------
    
    @Override
    protected RequestSpecification givenFilterableRequestParams() {
        return given().param("agg", "geohash:centroid:interval-3");
    }
    
    @Override
    protected void handleNotMatchingRequest(ValidatableResponse then) {
        then.statusCode(200)
        .body("totalnb", equalTo(0));
    }

    @Override
    public void handleComplexFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(1));     
    }
    
    @Override
    protected void handleKnownFieldFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(59));
    }
    
    @Override
    protected void handleKnownFieldFilterWithOr(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(117));
    }

    @Override
    protected void handleKnownFieldLikeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(59));
    }

    //TODO : fix the case where the field is full text
    /*@Override
    protected void handleKnownFullTextFieldLikeFilter(ValidatableResponse then) throws Exception {
         then.statusCode(200)
        .body("features.size()", equalTo(595));
    }*/

    @Override
    protected void handleKnownFieldFilterNotEqual(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(478));
    }
    
    @Override
    protected void handleMatchingQueryFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(595));
    }
    
    @Override
    protected void handleMatchingBeforeFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(3));
    }

    @Override
    protected void handleMatchingAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(3));
    }

    @Override
    protected void handleMatchingBeforeAfterFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(2));
    }
    
    @Override
    protected void handleMatchingPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(1));
    }

    @Override
    protected void handleMatchingNotPwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(17));
    }

    @Override
    protected void handleMatchingPwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(8));
    }
    
    @Override
    protected void handleMatchingGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(1));
    }

    @Override
    protected void handleMatchingNotGwithinFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(4));
    }

    @Override
    protected void handleMatchingGwithinComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(8));
    }
    
    @Override
    protected void handleMatchingGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(1));
    }

    @Override
    protected void handleMatchingNotGintersectFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(1));
    }

    @Override
    protected void handleMatchingGintersectComboFilter(ValidatableResponse then) throws Exception {
        then.statusCode(200)
        .body("elements.size()", equalTo(3));
    }
}
