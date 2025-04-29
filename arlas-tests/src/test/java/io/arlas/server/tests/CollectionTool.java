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

package io.arlas.server.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.model.DublinCoreElementName;
import io.arlas.server.core.model.Inspire;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.Expression;
import io.arlas.server.core.model.request.Filter;
import io.arlas.server.core.model.request.MultiValueFilter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class CollectionTool extends AbstractTestContext {

    public static String COLLECTION_NAME = "geodata";
    public static String COLLECTION_NAME_ACTOR = "geodata_actor";

    public static void main(String[] args) throws IOException, ArlasException {
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
    public  void load() throws ArlasException {
        this.load(0);
    }

    public  void load(long sleepAfter) throws ArlasException {
        load(sleepAfter, true);
    }

    public  void load(long sleepAfter, boolean exclude) throws ArlasException {
        try {
            DataSetTool.loadDataSet();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        CollectionReferenceParameters params = new CollectionReferenceParameters();
        params.indexName = DataSetTool.DATASET_INDEX_NAME;
        params.idPath = DataSetTool.DATASET_ID_PATH;
        params.geometryPath = DataSetTool.WKT_GEOMETRIES ? DataSetTool.DATASET_WKT_GEOMETRY_PATH:DataSetTool.DATASET_GEOMETRY_PATH;
        params.centroidPath = DataSetTool.DATASET_CENTROID_PATH;
        params.h3Path = DataSetTool.DATASET_H3_PATH;
        params.timestampPath = DataSetTool.DATASET_TIMESTAMP_PATH;
        params.excludeFields = DataSetTool.DATASET_EXCLUDE_FIELDS;
        params.excludeWfsFields = DataSetTool.DATASET_EXCLUDE_WFS_FIELDS;
        params.rasterTileURL = DataSetTool.DATASET_TILE_URL;
        params.rasterTileWidth=256;
        params.rasterTileHeight=256;
        params.inspire = new Inspire();
        params.inspire.lineage = DataSetTool.DATASET_INSPIRE_LINEAGE;
        params.inspire.topicCategories = Arrays.asList(DataSetTool.DATASET_INSPIRE_TOPIC_CATEGORY);
        params.dublinCoreElementName = new DublinCoreElementName();
        params.dublinCoreElementName.title = DataSetTool.DATASET_DUBLIN_CORE_TITLE;
        params.dublinCoreElementName.description = DataSetTool.DATASET_DUBLIN_CORE_DESCRIPTION;
        params.dublinCoreElementName.language = DataSetTool.DATASET_DUBLIN_CORE_LANGUAGE;


        // PUT new collection
        given().contentType("application/json").body(params).when().put(getUrlPath()).then().statusCode(200);
        Filter filter = new Filter();
        filter.f =Arrays.asList(new MultiValueFilter<>(new Expression("params.job", OperatorEnum.eq, DataSetTool.jobs[0])));
        params.filter =filter;
        given().contentType("application/json").body(params).when().put(arlasPath + "collections/" + COLLECTION_NAME_ACTOR).then().statusCode(200);

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
        } catch (ArlasException e) {
            e.printStackTrace();
        }

        InputStreamReader dcelementForCollection = new InputStreamReader(CollectionTool.class.getClassLoader().getResourceAsStream("csw.collection.dcelements.json"));
        ObjectMapper objectMapper = new ObjectMapper();
        DublinCoreElementName[] dcelements = objectMapper.readValue(dcelementForCollection, DublinCoreElementName[].class);
        Arrays.asList(dcelements).forEach(dublinCoreElementName -> {
                    CollectionReferenceParameters params = new CollectionReferenceParameters();
                    params.indexName = DataSetTool.DATASET_INDEX_NAME;
                    params.idPath = DataSetTool.DATASET_ID_PATH;
                    params.geometryPath = DataSetTool.DATASET_GEOMETRY_PATH;
                    params.centroidPath = DataSetTool.DATASET_CENTROID_PATH;
                    params.timestampPath = DataSetTool.DATASET_TIMESTAMP_PATH;
                    params.excludeFields = DataSetTool.DATASET_EXCLUDE_FIELDS;
                    params.excludeWfsFields = DataSetTool.DATASET_EXCLUDE_WFS_FIELDS;
                    params.rasterTileURL = DataSetTool.DATASET_TILE_URL;
                    params.dublinCoreElementName=dublinCoreElementName;
                    params.inspire = new Inspire();
                    params.inspire.lineage = DataSetTool.DATASET_INSPIRE_LINEAGE;
                    params.inspire.topicCategories = Arrays.asList(DataSetTool.DATASET_INSPIRE_TOPIC_CATEGORY);
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
        when().delete(arlasPath + "collections/" + COLLECTION_NAME_ACTOR).then().statusCode(200);
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

