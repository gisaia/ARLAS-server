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
import io.arlas.server.core.model.response.GaussianResponse;
import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GMMTool {

    public static final String RESULTS_FILE = "gmm/results/_gmm_result.json";
    public static final String MAPPING_FILE = "gmm/gmm.mapping.json";
    public static final String DATA_FILE = "gmm/data.json";

    public static String COLLECTION_NAME = "gmm_current";

    public final static String DATASET_INDEX_NAME="gmm";
    public final static String DATASET_ID_PATH="unique_id";
    public final static String DATASET_GEOMETRY_PATH="box_geom";
    public final static String DATASET_CENTROID_PATH="point_geom";
    public final static String DATASET_TIMESTAMP_PATH="timestamp";
    public static final String DATASET_INSPIRE_LINEAGE = "Dataset loaded for GMM testing";
    public static final String DATASET_INSPIRE_TOPIC_CATEGORY = "biota";
    public static final String DATASET_DUBLIN_CORE_TITLE = "gmm_current";
    public static final String DATASET_DUBLIN_CORE_DESCRIPTION = "gmm_current set for testing";
    public static final String DATASET_DUBLIN_CORE_LANGUAGE = "eng";

    public static List<List<GaussianResponse>> loadGMM(String file) throws IOException {
        List<List<GaussianResponse>> output = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        FeatureCollection fc = mapper.readValue(GMMTool.class.getClassLoader().getResource(file), FeatureCollection.class);

        for (Feature feature : fc.getFeatures()) {
            List<GaussianResponse> gmm = new ArrayList<>();
            for (LinkedHashMap<String, Object> gaussian : (List<LinkedHashMap<String, Object>>) feature.getProperties().get("gmm")) {
                gmm.add(new GaussianResponse(gaussian));
            }
            output.add(gmm);
        }

        return output;
    }

    public static void main(String... args) throws IOException {
        List<List<GaussianResponse>> gaussians = loadGMM(RESULTS_FILE);
        System.out.println(gaussians);

        ObjectMapper mapper = new ObjectMapper();
        GMMDataSet fc = mapper.readValue(GMMTool.class.getClassLoader().getResource(DATA_FILE), GMMDataSet.class);

        for (GMMData obj: fc.data) {
            System.out.println(obj);
        }
    }

}
