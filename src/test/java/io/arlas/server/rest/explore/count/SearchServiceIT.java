package io.arlas.server.rest.explore.count;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.arlas.server.rest.AbstractTest;

public class SearchServiceIT extends AbstractTest {

    @Test
    public void testBasicSearch() throws Exception {
        // GET _search
        given()
                // .param("f", "job:"+ DataSetTool.jobs[0])
                // .param("f", "job:"+ DataSetTool.jobs[1])
                // .param("f", "job:"+ DataSetTool.jobs[2])
                // .param("f", "job:"+ DataSetTool.jobs[3]) //TODO : when OR is
                // implemented
            .param("after", 1000000)
            .param("before", 2000000)
            .param("pwithin", "20,20,-20,-20")
            .param("notpwithin", "5,5,-5,-5")
            .param("size", "100")
            .param("from", "2")
            .param("sort", "-job,id")
        .when().get("/explore/foo/_search")
        .then().statusCode(200).extract().body().asString();
    }
}
