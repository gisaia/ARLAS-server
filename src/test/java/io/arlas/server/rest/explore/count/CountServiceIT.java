package io.arlas.server.rest.explore.count;

import static io.restassured.RestAssured.given;

import io.arlas.server.rest.AbstractTest;
import io.arlas.server.rest.admin.DataSetTool;
import org.junit.Test;


public class CountServiceIT extends AbstractTest{

    @Test
    public void testBasicCount() throws Exception {
        // GET _count
        given()
                .param("f", "job:"+ DataSetTool.jobs[0])
                .param("after", 0)
                //.param("pwithin", "POLYGON((0 0,0 100,100 100,100 0,0 0))")
                .when().get("/explore/foo/_count")
                .then().statusCode(200);
    }

    @Test
    public void testBasicSearch() throws Exception {
        // GET _search
        given().param("f", "job:"+ DataSetTool.jobs[0])
                .when().get("/explore/foo/_search")
                .then().statusCode(200);
    }
}
