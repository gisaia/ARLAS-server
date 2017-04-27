package io.arlas.server.rest.explore;

import org.junit.Test;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractSizedTest extends AbstractFilteredTest {

    //----------------------------------------------------------------
    //----------------------- SUCCESS TESTS --------------------------
    //----------------------------------------------------------------
    @Test
    public void testSizeFilter() throws Exception {
        handleSizeParameter(
                givenBigSizedRequestParams()
                .when().get(getUrlFilterPath("geodata"))
                .then(), 10);
        handleSizeParameter(
                givenBigSizedRequestParams().param("size", "40")
                .when().get(getUrlFilterPath("geodata"))
                .then(), 40);
        handleSizeParameter(
                givenBigSizedRequestParams().param("size", Integer.valueOf(getBigSizedResponseSize()))
                .when().get(getUrlFilterPath("geodata"))
                .then(), getBigSizedResponseSize());
        handleSizeParameter(
                givenBigSizedRequestParams().param("size", Integer.valueOf(getBigSizedResponseSize()+5))
                .when().get(getUrlFilterPath("geodata"))
                .then(), getBigSizedResponseSize());
    }
    
    @Test
    public void testFromFilter() throws Exception {
        handleSizeParameter(
                givenBigSizedRequestParams()
                .when().get(getUrlFilterPath("geodata"))
                .then(), 10);
        handleSizeParameter(
                givenBigSizedRequestParams().param("from", Integer.toString(getBigSizedResponseSize()-5))
                .when().get(getUrlFilterPath("geodata"))
                .then(), 5);
        handleSizeParameter(
                givenBigSizedRequestParams().param("from", Integer.toString(getBigSizedResponseSize()+5))
                .when().get(getUrlFilterPath("geodata"))
                .then(), 0);
    }
    
    //----------------------------------------------------------------
    //------------------------- ERROR TESTS --------------------------
    //----------------------------------------------------------------
    
    @Test
    public void testInvalidSizeParameters() throws Exception {
        //SIZE
        handleInvalidParameters(
                givenBigSizedRequestParams().param("size", "0")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                givenBigSizedRequestParams().param("size", "-10")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                givenBigSizedRequestParams().param("size", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        
        //FROM
        handleInvalidParameters(
                givenBigSizedRequestParams().param("from", "-10")
                .when().get(getUrlFilterPath("geodata"))
                .then());
        handleInvalidParameters(
                givenBigSizedRequestParams().param("from", "foo")
                .when().get(getUrlFilterPath("geodata"))
                .then());
    }

    protected abstract RequestSpecification givenBigSizedRequestParams();
    protected abstract int getBigSizedResponseSize();
    protected abstract void handleSizeParameter(ValidatableResponse then, int size) throws Exception;
}
