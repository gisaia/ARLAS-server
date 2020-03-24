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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CollectionServiceIT extends AbstractTestWithCollection {

    @Test
    public void testLifecycle() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());

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
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());

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
        getAllCollections(array(equalTo(COLLECTION_NAME),equalTo(COLLECTION_NAME_ACTOR)));

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
                .body("collection_name", hasItems(COLLECTION_NAME_ACTOR,COLLECTION_NAME));

        // GET all collections
        getAllCollections(everyItem(equalTo(COLLECTION_NAME)));

        // IMPORT existing collections
        given().multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems(COLLECTION_NAME_ACTOR,COLLECTION_NAME));

        // GET all collections
        getAllCollections(everyItem(equalTo(COLLECTION_NAME)));

        // IMPORT a new collection
        given().multiPart("file", jsonExport.replaceAll(COLLECTION_NAME, "foo"))
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems(equalTo("foo")));

        // GET all collections
        getAllCollections(hasItems(equalTo("foo"), array(equalTo(COLLECTION_NAME),equalTo(COLLECTION_NAME_ACTOR))));

        // DELETE new collection
        when().delete(arlasPath + "collections/foo")
                .then().statusCode(200);
        when().delete(arlasPath + "collections/foo_actor")
                .then().statusCode(200);
    }

    @Test
    public void testInvalidCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());


        // PUT new collection with Index type Only
        jsonAsMap.remove(CollectionReference.INDEX_NAME);
        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        handleInvalidCollectionParameters(put(jsonAsMap));

        // GET uncreated collection foo
        when().get(arlasPath + "collections/foo")
                .then().statusCode(404);

        // PUT new collection with id as exclude field
        Map<String, Object> wrongJsonAsMap = getJsonAsMap();
        wrongJsonAsMap.put(CollectionReference.EXCLUDE_FIELDS, DataSetTool.DATASET_ID_PATH);
        given().contentType("application/json").body(wrongJsonAsMap)
                .when().put(arlasPath + "collections/foo")
                .then().statusCode(400);

        // PUT new collection with geoparams parent field as exclude field
        wrongJsonAsMap.put(CollectionReference.EXCLUDE_FIELDS, DataSetTool.DATASET_GEO_PARAMS);
        given().contentType("application/json").body(wrongJsonAsMap)
                .when().put(arlasPath + "collections/foo")
                .then().statusCode(400);
    }

    @Test
    public void testMissingInspireDublinCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();

        // PUT new collection with missing both Inspire & dublin_core parameters
        handleInspireDublinMissingCollectionParameters(put(jsonAsMap));

        // PUT new collection with missing Inspire parameters only
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());
        handleInspireDublinMissingCollectionParameters(put(jsonAsMap));

        // PUT new collection with missing Dublin parameters only
        jsonAsMap.remove(CollectionReference.DUBLIN_CORE_PATH);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        handleInspireDublinMissingCollectionParameters(put(jsonAsMap));
    }

    @Test
    public void testInvalidInspireDublinCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireInvalidTopicCategoryJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());

        // PUT new collection with Invalid TopicCategory
        handleInvalidCollectionParameters(put(jsonAsMap));

        // PUT new collection with Invalid dublin core language
        jsonAsMap.remove(CollectionReference.INSPIRE_PATH);
        jsonAsMap.remove(CollectionReference.DUBLIN_CORE_PATH);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinInvalidLanguageJsonAsMap());
        handleInvalidCollectionParameters(put(jsonAsMap));
    }


    @Test
    public void testNotFoundCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());
        jsonAsMap.put(CollectionReference.ID_PATH, "id");

        jsonAsMap.put(CollectionReference.TYPE_NAME, DataSetTool.DATASET_TYPE_NAME);
        jsonAsMap.put(CollectionReference.ID_PATH, "unkownId");

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

    private void handleInspireDublinMissingCollectionParameters(ValidatableResponse then) throws Exception {
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

    private Map<String, Object> getDublinJsonAsMap() {
        Map<String, Object> dublinSubJsonAsMap = new HashMap<>();
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_TITLE, DataSetTool.DATASET_DUBLIN_CORE_TITLE);
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_DESCRIPTION, DataSetTool.DATASET_DUBLIN_CORE_DESCRIPTION);
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_LANGUAGE, DataSetTool.DATASET_DUBLIN_CORE_LANGUAGE);
        return dublinSubJsonAsMap;
    }

    private Map<String, Object> getDublinInvalidLanguageJsonAsMap() {
        Map<String, Object> dublinSubJsonAsMap = new HashMap<>();
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_TITLE, DataSetTool.DATASET_DUBLIN_CORE_TITLE);
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_DESCRIPTION, DataSetTool.DATASET_DUBLIN_CORE_DESCRIPTION);
        dublinSubJsonAsMap.put(CollectionReference.DUBLIN_CORE_LANGUAGE, "english");
        return dublinSubJsonAsMap;
    }

    private Map<String, Object> getInspireJsonAsMap() {
        Map<String, Object> inspireSubJsonAsMap = new HashMap<>();
        inspireSubJsonAsMap.put(CollectionReference.INSPIRE_LINEAGE, DataSetTool.DATASET_INSPIRE_LINEAGE);
        List<String> topicCategories =  new ArrayList<>();
        topicCategories.add(DataSetTool.DATASET_INSPIRE_TOPIC_CATEGORY);
        inspireSubJsonAsMap.put(CollectionReference.INSPIRE_TOPIC_CATEGORIES, topicCategories);
        return inspireSubJsonAsMap;
    }

    private Map<String, Object> getInspireInvalidTopicCategoryJsonAsMap() {
        Map<String, Object> inspireSubJsonAsMap = new HashMap<>();
        inspireSubJsonAsMap.put(CollectionReference.INSPIRE_LINEAGE, DataSetTool.DATASET_INSPIRE_LINEAGE);
        List<String> invalidTopicCategories =  new ArrayList<>();
        invalidTopicCategories.add("foo");
        inspireSubJsonAsMap.put(CollectionReference.INSPIRE_TOPIC_CATEGORIES, invalidTopicCategories);
        return inspireSubJsonAsMap;
    }



    @Override
    protected String getUrlPath(String collection) {
        return arlasPath + "/collections/" + collection;
    }
}
