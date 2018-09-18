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

package io.arlas.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.model.DublinCoreElementName;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class CollectionTool extends AbstractTestContext {

    public static String COLLECTION_NAME = "geodata";


    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "load":
                new CollectionTool().load();
                break;
            case "loadcsw":
                new CollectionTool().loadCsw(0);
                break;
            case "delete":
                new CollectionTool().delete();
                break;
            case "deletecsw":
                new CollectionTool().deleteCsw();
                break;
        }
        DataSetTool.close();
    }

    @Test
    public  void load() {
        this.load(0);
    }

    public  void load(long sleepAfter) {

        try {
            DataSetTool.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CollectionReferenceParameters params = new CollectionReferenceParameters();
        params.indexName = DataSetTool.DATASET_INDEX_NAME;
        params.typeName = DataSetTool.DATASET_TYPE_NAME;
        params.idPath = DataSetTool.DATASET_ID_PATH;
        params.geometryPath = DataSetTool.DATASET_GEOMETRY_PATH;
        params.centroidPath = DataSetTool.DATASET_CENTROID_PATH;
        params.timestampPath = DataSetTool.DATASET_TIMESTAMP_PATH;
        params.excludeFields = DataSetTool.DATASET_EXCLUDE_FIELDS;
        params.excludeWfsFields = DataSetTool.DATASET_EXCLUDE_WFS_FIELDS;
        params.taggableFields = DataSetTool.DATASET_TAGGABLE_FIELDS;
        params.rasterTileURL = DataSetTool.DATASET_TILE_URL;
        params.rasterTileWidth=256;
        params.rasterTileHeight=256;

        // PUT new collection
        given().contentType("application/json").body(params).when().put(getUrlPath()).then().statusCode(200);

        try {
            Thread.sleep(sleepAfter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void loadCsw() throws IOException {
        this.loadCsw(0);
    }

    public  void loadCsw(long sleepAfter) throws IOException {
        try {
            DataSetTool.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionTool.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        DublinCoreElementName[] dcelements = objectMapper.readValue(dcelementForCollection, DublinCoreElementName[].class);
        Arrays.asList(dcelements).forEach(dublinCoreElementName -> {
                    CollectionReferenceParameters params = new CollectionReferenceParameters();
                    params.indexName = DataSetTool.DATASET_INDEX_NAME;
                    params.typeName = DataSetTool.DATASET_TYPE_NAME;
                    params.idPath = DataSetTool.DATASET_ID_PATH;
                    params.geometryPath = DataSetTool.DATASET_GEOMETRY_PATH;
                    params.centroidPath = DataSetTool.DATASET_CENTROID_PATH;
                    params.timestampPath = DataSetTool.DATASET_TIMESTAMP_PATH;
                    params.excludeFields = DataSetTool.DATASET_EXCLUDE_FIELDS;
                    params.excludeWfsFields = DataSetTool.DATASET_EXCLUDE_WFS_FIELDS;
                    params.taggableFields = DataSetTool.DATASET_TAGGABLE_FIELDS;
                    params.rasterTileURL = DataSetTool.DATASET_TILE_URL;
                    params.dublinCoreElementName=dublinCoreElementName;
                    String url = arlasPath + "collections/" + dublinCoreElementName.title.split(" ")[0].toLowerCase();
                    // PUT new collection
                    given().contentType("application/json").body(params).when().put(url).then().statusCode(200);
                }
        );
        try {
            Thread.sleep(sleepAfter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  void delete() throws IOException {
        DataSetTool.clearDataSet();
        //DELETE collection
        when().delete(getUrlPath()).then().statusCode(200);
    }

    public  void deleteCsw() throws IOException {
        DataSetTool.clearDataSet();
        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionTool.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        DublinCoreElementName[] dcelements = objectMapper.readValue(dcelementForCollection, DublinCoreElementName[].class);
        Arrays.asList(dcelements).forEach(dublinCoreElementName -> {
                    String url = arlasPath + "collections/" + dublinCoreElementName.title.split(" ")[0].toLowerCase();
                    //DELETE collection
                    when().delete(url).then().statusCode(200);
                }
        );
    }

    @Override
    protected String getUrlPath(String collection) {
        return getUrlPath();
    }

    protected static String getUrlPath() {
        return arlasPath + "collections/" + COLLECTION_NAME;
    }
}

