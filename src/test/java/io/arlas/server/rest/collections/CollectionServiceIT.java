package io.arlas.server.rest.collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.arlas.server.rest.AbstractTestWithDataSet;
import io.arlas.server.rest.DataSetTool;

public class CollectionServiceIT extends AbstractTestWithDataSet {

    @Test
    public void testLifecycle() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put("index_name", DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put("type_name", DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put("id_path", DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put("geometry_path", DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put("centroid_path", DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put("timestamp_path", DataSetTool.DATASET_TIMESTAMP_PATH);

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap)
        .when().put("/collections/foo")
        .then().statusCode(200);

        // GET collection
        when().get("/collections/foo")
        .then().statusCode(200)
            .body("collection_name", equalTo("foo"))
            .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
            .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
            .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
            .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
            .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
            .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH));

        // DELETE collection
        when().delete("/collections/foo")
        .then().statusCode(200);

        // GET deleted collection
        when().get("/collections/foo")
        .then().statusCode(404);
    }

    @Override
    protected String getUrlPath(String collection) {
        return "/collections/"+collection;
    }
}
