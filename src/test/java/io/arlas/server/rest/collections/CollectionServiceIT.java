package io.arlas.server.rest.collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isOneOf;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.DataSetTool;
import io.arlas.server.model.CollectionReference;
import io.restassured.response.ValidatableResponse;

public class CollectionServiceIT extends AbstractTestWithCollection {

    @Test
    public void testLifecycle() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap)
        .when().put(arlasPrefix+"collections/foo")
        .then().statusCode(200);

        // GET collection
        when().get(arlasPrefix+"collections/foo")
        .then().statusCode(200)
            .body("collection_name", equalTo("foo"))
            .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
            .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
            .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
            .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
            .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
            .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH));

        // DELETE collection
        when().delete(arlasPrefix+"collections/foo")
        .then().statusCode(200);

        // GET deleted collection
        when().get(arlasPrefix+"collections/foo")
        .then().statusCode(404);
    }

    @Test
    public void testGetAllCollections() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();

        // PUT new collection 1
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPrefix+"collections/collection1")
                .then().statusCode(200);

        // PUT new collection 2
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPrefix+"collections/collection2")
                .then().statusCode(200);

        // GET all collections
        when().get(arlasPrefix+"collections/")
                .then().statusCode(200)
                .body("collection_name", everyItem(isOneOf(COLLECTION_NAME,"collection1","collection2")));

        // DELETE collection 1
        when().delete(arlasPrefix+"collections/collection1")
                .then().statusCode(200);
        // GET deleted collection
        when().get(arlasPrefix+"collections/collection1")
                .then().statusCode(404);

        // DELETE collection 2
        when().delete(arlasPrefix+"collections/collection2")
                .then().statusCode(200);
        // GET deleted collection
        when().get(arlasPrefix+"collections/collection2")
                .then().statusCode(404);

    }

    @Test
    public void testInvalidCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);

        // PUT new collection with Index name Only
        handleInvalidCollectionParameters(put(jsonAsMap));

        // PUT new collection with Index type Only
        jsonAsMap.remove(CollectionReference.INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        handleInvalidCollectionParameters(put(jsonAsMap));

        // GET uncreated collection foo
        when().get(arlasPrefix+"collections/foo")
                .then().statusCode(404);
    }

    @Test
    public void testNotFoundCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, "unknownId");

        // PUT new collection with non-existing 'id' field from DATASET_TYPE_NAME in DATASET_INDEX_NAME
        handleNotFoundCollectionParameters(put(jsonAsMap));

        // PUT new collection with non-existing 'geometry' field from DATASET_TYPE_NAME in DATASET_INDEX_NAME
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, "geom");
        handleNotFoundCollectionParameters(put(jsonAsMap));

        // GET uncreated collection foo
        when().get(arlasPrefix+"collections/foo")
                .then().statusCode(404);
    }


    private void handleInvalidCollectionParameters(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    private void handleNotFoundCollectionParameters(ValidatableResponse then) throws Exception {
        then.statusCode(404);
    }

    private ValidatableResponse put(Map<String, Object> jsonAsMap){
        return given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPrefix+"collections/foo")
                .then();
    }

    private Map<String, Object> getJsonAsMap(){
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH , DataSetTool.DATASET_TIMESTAMP_PATH);
        return jsonAsMap;
    }
    @Override
    protected String getUrlPath(String collection) {
        return arlasPrefix + "/collections/"+collection;
    }
}
