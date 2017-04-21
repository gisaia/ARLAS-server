package io.arlas.server.rest.admin;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.restassured.RestAssured;

public class CollectionServiceIT {
    
    static {
	RestAssured.baseURI = "http://arlas-server";
	RestAssured.port = 9999;
	RestAssured.basePath = "/arlas";
    }
    
    @Test
    public void testLifecycle() throws Exception {
	Map<String, Object> jsonAsMap = new HashMap<String, Object>();
	jsonAsMap.put("indexName", "bar");
	jsonAsMap.put("typeName", "type");
	jsonAsMap.put("idPath","path");
	jsonAsMap.put("geometryPath","geopath");
	jsonAsMap.put("centroidPath","centroidpath");
	jsonAsMap.put("timestampPath","tspath");
        
	//PUT new collection
	given()
	  .contentType("application/json")
	  .body(jsonAsMap)
	.when()
	  .put("/collections/foo")
	.then()
	  .statusCode(200);
	
	//GET collection
	when()
	  .get("/collections/foo")
	.then()
	  .statusCode(200)
	  .body("collectionName", equalTo("foo"))
	  .body("indexName", equalTo("bar"))
	  .body("typeName", equalTo("type"))
	  .body("idPath", equalTo("path"))
	  .body("geometryPath", equalTo("geopath"))
	  .body("centroidPath", equalTo("centroidpath"))
	  .body("timestampPath", equalTo("tspath"));
	
	//DELETE collection
	when()
	  .delete("/collections/foo")
	.then()
	  .statusCode(200);
	
	//GET deleted collection
	when()
	  .get("/collections/foo")
	.then()
	  .statusCode(404);
    }
}
