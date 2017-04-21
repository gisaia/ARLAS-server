package io.arlas.server.rest.explore.count;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.arlas.server.rest.admin.DataSetTool;
import io.restassured.RestAssured;

public class CountServiceIT {

    DataSetTool dataset = null;
    
    static {
	RestAssured.baseURI = "http://arlas-server";
	RestAssured.port = 9999;
	RestAssured.basePath = "/arlas";
    }

    @Before
    public void before() {
	try {
	    dataset = DataSetTool.init();
	    dataset.loadDataSet();
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	Map<String, Object> jsonAsMap = new HashMap<String, Object>();
	jsonAsMap.put("indexName", DataSetTool.DATASET_INDEX_NAME);
	jsonAsMap.put("typeName", DataSetTool.DATASET_TYPE_NAME);
	jsonAsMap.put("idPath", DataSetTool.DATASET_ID_PATH);
	jsonAsMap.put("geometryPath", DataSetTool.DATASET_GEOMETRY_PATH);
	jsonAsMap.put("centroidPath", DataSetTool.DATASET_CENTROID_PATH);
	jsonAsMap.put("timestampPath", DataSetTool.DATASET_TIMESTAMP_PATH);

	// PUT new collection
	given().contentType("application/json").body(jsonAsMap)
	.when().put("/collections/foo")
	.then().statusCode(200);
    }

    @After
    public void after() {
	dataset.clearDataSet();
	
	// DELETE collection
	when().delete("/collections/foo")
	.then().statusCode(200);
    }

    @Test
    public void testBasicCount() throws Exception {

	// GET _count
	given().param("f", "test")
	.when().get("/explore/foo/_count")
	.then().statusCode(200);
    }

    @Test
    public void testBasicSearch() throws Exception {

	// GET _search
	given().param("f", "test")
	.when().get("/explore/foo/_search")
	.then().statusCode(200);
    }
}
