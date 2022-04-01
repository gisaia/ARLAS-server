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

package io.arlas.server.tests.rest.collections;

import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.tests.AbstractTestWithCollection;
import io.arlas.server.tests.DataSetTool;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CollectionServiceIT extends AbstractTestWithCollection {

    @Test
    public void test00NotFoundCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());
        jsonAsMap.put(CollectionReference.ID_PATH, "id");

        jsonAsMap.put(CollectionReference.ID_PATH, "unkownId");

        // PUT new collection with non-existing 'id' field in DATASET_INDEX_NAME
        handleNotFoundCollectionParameters(put(jsonAsMap));

        // PUT new collection with non-existing 'geometry' field in DATASET_INDEX_NAME
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, "geom");
        handleNotFoundCollectionParameters(put(jsonAsMap));

        // GET uncreated collection foo
        when().get(arlasPath + "collections/foo")
                .then().statusCode(404);
    }

    @Test
    public void test01Lifecycle() throws Exception {
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
    public void test02GetAllCollections() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());

        // PUT new collection 1
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPath + "collections/collection1")
                .then().statusCode(200);

        // GET all collections
        getAllCollections(COLLECTION_NAME, COLLECTION_NAME_ACTOR, "collection1");

        // DELETE collection 1
        when().delete(arlasPath + "collections/collection1")
                .then().statusCode(200);
        // GET deleted collection
        when().get(arlasPath + "collections/collection1")
                .then().statusCode(404);
    }

    private void getAllCollections(String... collectionNames) throws InterruptedException {
        getAllCollections(given(), collectionNames);
    }

    private void getAllCollections(RequestSpecification given, String... collectionNames) throws InterruptedException {
        int cpt = 0;
        while (cpt >= 0 && cpt < 5) {
            try {
                getAllCollectionsLoop(given, collectionNames);
                cpt = -1;
            } catch (Throwable e) {
                cpt++;
                Thread.sleep(1000);
            }
        }
        getAllCollectionsLoop(given, collectionNames);
    }

    private void getAllCollectionsLoop(RequestSpecification given, String... collectionNames) throws AssertionError {
        CollectionReference[] collections = given.when().get(arlasPath + "collections/")
                .then().statusCode(200)
                .extract()
                .as(CollectionReference[].class);
        assertThat(collections.length, equalTo(collectionNames.length));
        List<String> actualCollectionNames = Arrays.stream(collections).map(collection -> collection.collectionName).collect(Collectors.toList());
        if(collectionNames!= null && collectionNames.length > 0) {
            assertThat(actualCollectionNames, containsInAnyOrder(collectionNames));
        }
    }

    @Test
    public void test03ImportExportCollections() throws Exception {

        // GET all collections
        getAllCollections(COLLECTION_NAME,COLLECTION_NAME_ACTOR);

        // EXPORT all collections
        String jsonExport = get(arlasPath + "collections/_export").asString();

        // DELETE collection
        when().delete(arlasPath + "collections/" + COLLECTION_NAME)
                .then().statusCode(200);
        when().delete(arlasPath + "collections/" + COLLECTION_NAME_ACTOR)
                .then().statusCode(200);

        // GET deleted collection
        when().get(arlasPath + "collections/" + COLLECTION_NAME)
                .then().statusCode(404);
        when().get(arlasPath + "collections/" + COLLECTION_NAME_ACTOR)
                .then().statusCode(404);

        // IMPORT all collections
        given().multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems(COLLECTION_NAME_ACTOR,COLLECTION_NAME));

        // GET all collections
        getAllCollections(COLLECTION_NAME_ACTOR,COLLECTION_NAME);

        // IMPORT existing collections
        given().multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems(COLLECTION_NAME_ACTOR,COLLECTION_NAME));

        // GET all collections
        getAllCollections(COLLECTION_NAME_ACTOR,COLLECTION_NAME);

        // IMPORT a new collection
        given().multiPart("file", jsonExport.replaceAll(COLLECTION_NAME, "foo"))
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", hasItems("foo","foo_actor"));

        // GET all collections
        getAllCollections("foo", "foo_actor", COLLECTION_NAME, COLLECTION_NAME_ACTOR);

        // DELETE new collection
        when().delete(arlasPath + "collections/foo")
                .then().statusCode(200);
        when().delete(arlasPath + "collections/foo_actor")
                .then().statusCode(200);
    }

    @Test
    public void test04InvalidCollectionParameters() throws Exception {
        Map<String, Object> jsonAsMap = new HashMap<String, Object>();
        jsonAsMap.put(CollectionReference.INDEX_NAME, DataSetTool.DATASET_INDEX_NAME);
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());


        // PUT new collection with Index type Only
        jsonAsMap.remove(CollectionReference.INDEX_NAME);
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
    public void test05MissingInspireDublinCollectionParameters() throws Exception {
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
    public void test06InvalidInspireDublinCollectionParameters() throws Exception {
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
    public void test07CollectionWithDescription() throws Exception {
        Map<String, Object> jsonAsMap = getJsonAsMap();
        jsonAsMap.put(CollectionReference.INSPIRE_PATH, getInspireJsonAsMap());
        jsonAsMap.put(CollectionReference.DUBLIN_CORE_PATH, getDublinJsonAsMap());
        jsonAsMap.put(CollectionReference.DISPLAY_NAMES, getCollectionDescriptionJsonAsMap());

        // PUT new collection
        given().contentType("application/json").body(jsonAsMap)
                .when().put(arlasPath + "collections/foo_described")
                .then().statusCode(200);

        // GET collection
        when().get(arlasPath + "collections/foo_described")
                .then().statusCode(200)
                .body("collection_name", equalTo("foo_described"))
                .body("params.display_names.collection", equalTo(DataSetTool.DATASET_COLLECTION_DISPLAY_NAME))
                .body("params.display_names.fields['"+DataSetTool.DATASET_ID_PATH+"']", equalTo(DataSetTool.DATASET_ID_DESC))
                .body("params.display_names.fields['"+DataSetTool.DATASET_CENTROID_PATH+"']", equalTo(DataSetTool.DATASET_CENTROID_DESC))
                .body("params.display_names.fields['"+DataSetTool.DATASET_GEOMETRY_PATH+"']", equalTo(DataSetTool.DATASET_GEOMETRY_DESC))
                .body("params.display_names.fields['"+DataSetTool.DATASET_TIMESTAMP_PATH+"']", equalTo(DataSetTool.DATASET_TIMESTAMP_DESC));

        // DELETE collection
        when().delete(arlasPath + "collections/foo_described")
                .then().statusCode(200);
    }

    @Test
    public void test08WithCollectionFilter() throws Exception {
        // get collections with NON matching collection filter
        getAllCollections(given().header("column-filter", "notexisting:*"));

        // get collections with one matching collection filter
        getAllCollections(given().header("column-filter", "geodata_actor:*"), COLLECTION_NAME_ACTOR);

        // get collections with all matching collection filter
        getAllCollections(given().header("column-filter", "geodata*:*"), COLLECTION_NAME_ACTOR,COLLECTION_NAME);

        // get collections with no collection filter
        getAllCollections(COLLECTION_NAME_ACTOR,COLLECTION_NAME);

        // EXPORT collection with matching collection filter
        String jsonExport = given()
                .header("column-filter", COLLECTION_NAME_ACTOR+":*")
                .when()
                .get(arlasPath + "collections/_export").asString();

        // EXPORT collections with NON matching collection filter
        given().header("column-filter", "notexisting:*")
                .when()
                .get(arlasPath + "collections/_export").asString().isEmpty();


        // DELETE collection
        when().delete(arlasPath + "collections/" + COLLECTION_NAME_ACTOR)
                .then().statusCode(200);

        // GET deleted collection
        when().get(arlasPath + "collections/" + COLLECTION_NAME_ACTOR)
                .then().statusCode(404);

        // IMPORT collection with matching collection filter
        given().header("column-filter", COLLECTION_NAME_ACTOR+":*")
                .multiPart("file", jsonExport)
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(200)
                .body("collection_name", everyItem(equalTo(COLLECTION_NAME_ACTOR)));


        // IMPORT a new collection with NON matching collection filter
        given().header("column-filter", COLLECTION_NAME_ACTOR+":*")
                .multiPart("file", jsonExport.replaceAll(COLLECTION_NAME, "foo"))
                .when().post(arlasPath + "collections/_import")
                .then().statusCode(403);

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
        jsonAsMap.put(CollectionReference.ID_PATH, DataSetTool.DATASET_ID_PATH);
        jsonAsMap.put(CollectionReference.GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_PATH);
        jsonAsMap.put(CollectionReference.CENTROID_PATH, DataSetTool.DATASET_CENTROID_PATH);
        jsonAsMap.put(CollectionReference.H3_PATH, DataSetTool.DATASET_H3_PATH);
        jsonAsMap.put(CollectionReference.TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_PATH);
        jsonAsMap.put(CollectionReference.EXCLUDE_FIELDS, DataSetTool.DATASET_EXCLUDE_FIELDS);
        jsonAsMap.put(CollectionReference.EXCLUDE_WFS_FIELDS, DataSetTool.DATASET_EXCLUDE_WFS_FIELDS);
        return jsonAsMap;
    }

    private Object getCollectionDescriptionJsonAsMap() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put(CollectionReference.COLLECTION_DISPLAY_NAME, DataSetTool.DATASET_COLLECTION_DISPLAY_NAME);
        jsonAsMap.put(CollectionReference.FIELD_DISPLAY_NAME, getFieldDescriptionsJsonAsMap());
        return jsonAsMap;
    }

    private Object getFieldDescriptionsJsonAsMap() {
        Map<String, Object> jsonAsMap = new HashMap<>();
        jsonAsMap.put(DataSetTool.DATASET_ID_PATH, DataSetTool.DATASET_ID_DESC);
        jsonAsMap.put(DataSetTool.DATASET_CENTROID_PATH, DataSetTool.DATASET_CENTROID_DESC);
        jsonAsMap.put(DataSetTool.DATASET_GEOMETRY_PATH, DataSetTool.DATASET_GEOMETRY_DESC);
        jsonAsMap.put(DataSetTool.DATASET_TIMESTAMP_PATH, DataSetTool.DATASET_TIMESTAMP_DESC);
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
