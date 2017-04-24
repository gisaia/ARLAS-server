package io.arlas.server.rest.explore.count;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;
import io.arlas.server.rest.DataSetTool;

public class CountServiceIT extends AbstractTestWithDataSet {

    @Test
    public void testBasicCount() throws Exception {
        // GET _count
        given().param("f", "job:" + DataSetTool.jobs[0])
            .param("after", 1000000)
            .param("before", 2000000)
            .param("pwithin", "10,10,-10,-10")
            .param("notpwithin", "5,5,-5,-5")
        .when().get("/explore/foo/_count")
        .then().statusCode(200).body(equalTo("1"));
    }
}
