/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.rest.collections;

import io.arlas.server.AbstractTestWithCollection;
import io.arlas.server.DataSetTool;
import io.arlas.server.model.CollectionReference;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CollectionServiceIT extends AbstractTestWithCollection {

    @Test
    public void testLifecycle() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPath + "collections/foo")
                .then().statusCode(200);

        // GET collection
        when().get(arlasPath + "collections/foo")
                .then().statusCode(200)
                .body("collection_name", equalTo("foo"))
                .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
                .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
                .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
                .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
                .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
                .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH))
                .body("params.exclude_fields", equalTo(DataSetTool.DATASET_EXCLUDE_FIELDS))
                .body("params.exclude_wfs_fields", equalTo(DataSetTool.DATASET_EXCLUDE_WFS_FIELDS))
                .body("params.custom_params.timestamp_format", equalTo(DataSetTool.DATASET_TIMESTAMP_FORMAT));


        // PUT SCHEMA JSON
        given().multiPart("json_schema_file", DataSetTool.jsonSchema.toString())
                .when().put(arlasPath + "collections/foo/_import_json_schema")
                .then().statusCode(200)
                .body("collection_name", equalTo("foo"))
                .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
                .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
                .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
                .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
                .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
                .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH))
                .body("params.custom_params.timestamp_format", equalTo(DataSetTool.DATASET_TIMESTAMP_FORMAT))
                .body("params.json_schema.required", hasSize(2))
                .body("params.json_schema.properties.md.required", hasSize(4))
                .body("params.json_schema.properties.md.properties.geometry.properties.coordinates.items.items.items.type", equalTo("number"));


        // GET collection
        when().get(arlasPath + "collections/foo")
                .then().statusCode(200)
                .body("collection_name", equalTo("foo"))
                .body("params.index_name", equalTo(DataSetTool.DATASET_INDEX_NAME))
                .body("params.type_name", equalTo(DataSetTool.DATASET_TYPE_NAME))
                .body("params.id_path", equalTo(DataSetTool.DATASET_ID_PATH))
                .body("params.geometry_path", equalTo(DataSetTool.DATASET_GEOMETRY_PATH))
                .body("params.centroid_path", equalTo(DataSetTool.DATASET_CENTROID_PATH))
                .body("params.timestamp_path", equalTo(DataSetTool.DATASET_TIMESTAMP_PATH))
                .body("params.exclude_fields", equalTo(DataSetTool.DATASET_EXCLUDE_FIELDS))
                .body("params.exclude_wfs_fields", equalTo(DataSetTool.DATASET_EXCLUDE_WFS_FIELDS))
                .body("params.custom_params.timestamp_format", equalTo(DataSetTool.DATASET_TIMESTAMP_FORMAT))
                .body("params.json_schema.required", hasSize(2))
                .body("params.json_schema.properties.md.required", hasSize(4))
                .body("params.json_schema.properties.md.properties.geometry.properties.coordinates.items.items.items.type", equalTo("number"));


        // DELETE collection
        when().delete(arlasPath + "collections/foo")
                .then().statusCode(200);

        // GET deleted collection
        when().get(arlasPath + "collections/foo")
                .then().statusCode(404);
    }

    @Test
    public void testGetAllCollections() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();

        // PUT new collection 1
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPath + "collections/collection1")
                .then().statusCode(200);

        // GET all collections
        getAllCollections(hasItems(equalTo(COLLECTION_NAME), equalTo("collection1")));

        // DELETE collection 1
        when().delete(arlasPath + "collections/collection1")
                .then().statusCode(200);
        // GET deleted collection
        when().get(arlasPath + "collections/collection1")
                .then().statusCode(404);
    }

    private void getAllCollections(Matcher matcher) throws InterruptedException {
        int cpt = 0;
        while (cpt > 0 && cpt < 5) {
            try {
                when().get(arlasPath + "collections/")
                        .then().statusCode(200)
                        .body("collection_name", matcher);
                cpt = -1;
            } catch (Exception e) {
                cpt++;
                Thread.sleep(1000);
            }
        }
    }

    @Test
    public void testImportExportCollections() throws Exception {

        // GET all collections
        getAllCollections(everyItem(equalTo(COLLECTION_NAME)));

        // EXPORT all collections
        String jsonExport = get(arlasPath + "collections/_export").asString();

        // DELETE collection
        when().delete(arlasPath + "collections/" + COLLECTION_NAME)
                .then().statusCode(200);

        // GET deleted collection
        when().get(arlasPath + "collections/" + COLLECTION_NAME)
                .then().statusCode(404);

        // IMPORT all collections
        given().multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", everyItem(equalTo(COLLECTION_NAME)));

        // GET all collections
        getAllCollections(everyItem(equalTo(COLLECTION_NAME)));
        /*when().get(arlasPrefix+"collections/")
                .then().statusCode(200)
                .body("collection_name", everyItem(equalTo(COLLECTION_NAME)));*/

        // IMPORT existing collections
        given().multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", everyItem(equalTo(COLLECTION_NAME)));

        // GET all collections
        getAllCollections(everyItem(equalTo(COLLECTION_NAME)));

        // IMPORT a new collection
        given().multiPart("file", jsonExport.replaceAll(COLLECTION_NAME, "foo"))
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems(equalTo("foo")));

        // GET all collections
        getAllCollections(hasItems(equalTo("foo"), equalTo(COLLECTION_NAME)));

        // DELETE new collection
        when().delete(arlasPath + "collections/foo")
                .then().statusCode(200);
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
        when().get(arlasPath + "collections/foo")
                .then().statusCode(404);
    }

    @Test
    public void testNotFoundCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<>();
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
        when().get(arlasPath + "collections/foo")
                .then().statusCode(404);
    }


    private void handleInvalidCollectionParameters(ValidatableResponse then) throws Exception {
        then.statusCode(400);
    }

    private void handleNotFoundCollectionParameters(ValidatableResponse then) throws Exception {
        then.statusCode(404);
    }

    private ValidatableResponse put(Map<String, Object> jsonAsMap) {
        return given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPath + "collections/foo")
                .then();
    }

    private Map<String, Object> getJsonAsMap() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_PATH);
        jsonAsMap.put(CollectionReference.EXCLUDE_FIELDS, DataSetTool.DATASET_EXCLUDE_FIELDS);
        jsonAsMap.put(CollectionReference.EXCLUDE_WFS_FIELDS, DataSetTool.DATASET_EXCLUDE_WFS_FIELDS);
        return jsonAsMap;
    }

    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "/collections/" + collection;
    }
}
