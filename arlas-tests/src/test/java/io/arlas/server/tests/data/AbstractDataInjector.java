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

package io.arlas.server.tests.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.server.tests.Data;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.RasterTileURL;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDataInjector {

    static Logger LOGGER = LoggerFactory.getLogger(AbstractDataInjector.class);

    public final static String DATA_SINK_NAME ="dataset";

    //TODO make these fields consistent between implementations (Elasticsearch, Postgis, ...)
    public final static String DATASET_ID_PATH="id";
    public final static String DATASET_GEO_PARAMS="geo_params";
    public final static String DATASET_GEOMETRY_PATH="geo_params_geometry";
    public final static String DATASET_WKT_GEOMETRY_PATH="geo_params_wktgeometry";
    public final static String DATASET_CENTROID_PATH="geo_params_centroid";
    public final static String DATASET_TIMESTAMP_PATH="params_startdate";
    public final static String DATASET_EXCLUDE_FIELDS = "params_ci*";
    public final static String DATASET_EXCLUDE_WFS_FIELDS="params_country";
    public final static String DATASET_TIMESTAMP_FORMAT = "epoch_millis";
    public static final String DATASET_INSPIRE_LINEAGE = "Dataset loaded for testing";
    public static final String DATASET_INSPIRE_TOPIC_CATEGORY = "biota";
    public static final String DATASET_DUBLIN_CORE_TITLE = "geodata";
    public static final String DATASET_DUBLIN_CORE_DESCRIPTION = "geodata set for testing";
    public static final String DATASET_DUBLIN_CORE_LANGUAGE = "eng";
    public final static RasterTileURL DATASET_TILE_URL = new RasterTileURL(Optional.ofNullable(System.getenv("ARLAS_TILE_URL")).orElse("{id}/{z}/{x}/{y}.png"),5,18,true);

    public static final String[] jobs = {"Actor", "Announcers", "Archeologists", "Architect", "Brain Scientist", "Chemist", "Coach", "Coder", "Cost Estimator", "Dancer", "Drafter"};
    public static final String[] cities = {"Paris", "London", "New York", "Tokyo", "Toulouse", "Marseille", "Lyon", "Bordeaux", "Lille", "Albi", "Calais"};
    public static final String[] countries = {"Afghanistan",
            "Albania",
            "Algeria",
            "Andorra",
            "Angola",
            "Antigua",
            "Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria"
    };
    public static boolean WKT_GEOMETRIES = false;

    public void loadDataSet() throws IOException, ArlasException {
        //Create a single sink with all data
        clearDataSink(DATA_SINK_NAME);
        createDataSink(DATA_SINK_NAME);
        fillDataSink(DATA_SINK_NAME,-170,170,-80,80);
        LOGGER.info("Data sink created : " + DATA_SINK_NAME);
    }

    public void clearDataSet() throws IOException, ArlasException {
        //Create a single sink with all data
        clearDataSink(DATA_SINK_NAME);
        LOGGER.info("Data sink cleared : " + DATA_SINK_NAME);
    }

    public abstract void createDataSink(String dataSinkName);
    public abstract void writeData(String dataSinkName, Data data);
    public abstract void clearDataSink(String dataSinkName);

    public void fillDataSink(String dataSinkName, int lonMin, int lonMax, int latMin, int latMax) throws JsonProcessingException, ArlasException {
        Data data;

        for (int i = lonMin; i <= lonMax; i += 10) {
            for (int j = latMin; j <= latMax; j += 10) {
                int i2 = i + 6;
                int j2 = j + 6;
                data = new Data();
                data.id = String.valueOf("ID_" + i + "_" + j + "DI").replace("-", "_");
                data.fullname = "My name is " + data.id;
                data.params.age = Math.abs(i * j);
                data.params.startdate = 1l * (i + 1000) * (j + 1000);
                if (data.params.startdate >= 1013600) {
                    data.params.weight = (i + 10) * (j + 10);
                }
                data.params.stopdate = 1l * (i + 1000) * (j + 1000) + 100;
                data.geo_params.centroid = j + "," + i;
                data.geo_params.other_geopoint = j2 + "," + i2;
                data.params.job = jobs[((Math.abs(i) + Math.abs(j)) / 10) % (jobs.length - 1)];
                data.params.country = countries[((Math.abs(i) + Math.abs(j)) / 10) % (countries.length - 1)];
                data.params.city = cities[((Math.abs(i) + Math.abs(j)) / 10) % (cities.length - 1)];
                List<LngLatAlt> coords = new ArrayList<>();
                List<LngLatAlt> second_coords = new ArrayList<>();
                String wktGeometry = "POLYGON ((";
                coords.add(new LngLatAlt(i - 1, j + 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 + 1));
                wktGeometry += (i - 1) + " " + (j + 1) + ",";
                coords.add(new LngLatAlt(i + 1, j + 1));
                second_coords.add(new LngLatAlt(i2 + 1, j2 + 1));
                wktGeometry += " " + (i + 1) + " " + (j + 1) + ",";
                coords.add(new LngLatAlt(i + 1, j - 1));
                second_coords.add(new LngLatAlt(i2 + 1, j2 - 1));
                wktGeometry += " " + (i + 1) + " " + (j - 1) + ",";
                coords.add(new LngLatAlt(i - 1, j - 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 - 1));
                wktGeometry += " " + (i - 1) + " " + (j - 1) + ",";
                coords.add(new LngLatAlt(i - 1, j + 1));
                second_coords.add(new LngLatAlt(i2 - 1, j2 + 1));
                wktGeometry += " " + (i - 1) + " " + (j + 1) + "))";

                data.geo_params.geometry = new Polygon(coords);
                data.geo_params.second_geometry = new Polygon(second_coords);
                data.geo_params.wktgeometry = wktGeometry;
                writeData(dataSinkName, data);
            }
        }
    }
}