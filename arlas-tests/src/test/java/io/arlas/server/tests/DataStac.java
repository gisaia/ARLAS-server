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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geojson.LngLatAlt;
import org.geojson.Polygon;

public class DataStac {
    public Map<String, DataStac.Asset> assets = new HashMap<>();
    public DataStac.Properties properties = new DataStac.Properties();
    @JsonProperty("collection")
    public String collection;

    @JsonProperty("catalog")
    public String catalog;

    @JsonProperty("id")
    public String id;

    @JsonProperty("geometry")
    public Polygon geometry;

    @JsonProperty("bbox")
    public double[] bbox;

    @JsonProperty("centroid")
    public double[] centroid;

    @JsonProperty("type")
    public String type;

    static class Asset {
        @JsonProperty("name")
        public String name;

        @JsonProperty("href")
        public String href;

        @JsonProperty("title")
        public String title;

        @JsonProperty("description")
        public String description;

        @JsonProperty("type")
        public String type;

        @JsonProperty("roles")
        public List<String> roles;

        @JsonProperty("airs__managed")
        public Boolean airsManaged;

        @JsonProperty("asset_format")
        public String assetFormat;

        @JsonProperty("storage__tier")
        public String storageTier;

        @JsonProperty("storage__platform")
        public String storagePlatform;

        @JsonProperty("airs__object_store_bucket")
        public String objectStoreBucket;

        @JsonProperty("airs__object_store_key")
        public String objectStoreKey;
    }
    class Properties {
        @JsonProperty("datetime")
        public long datetime;

        @JsonProperty("start_datetime")
        public long startDatetime;

        @JsonProperty("end_datetime")
        public long endDatetime;

        @JsonProperty("keywords")
        public List<String> keywords;

        @JsonProperty("platform")
        public String platform;

        @JsonProperty("instrument")
        public String instrument;

        @JsonProperty("data_type")
        public String dataType;

        @JsonProperty("item_type")
        public String itemType;

        @JsonProperty("item_format")
        public String itemFormat;

        @JsonProperty("eo__cloud_cover")
        public double cloudCover;

        @JsonProperty("eo__bands")
        public List<String> bands;

        @JsonProperty("processing__level")
        public String processingLevel;

        @JsonProperty("acq__acquisition_orbit_direction")
        public String orbitDirection;

        @JsonProperty("acq__acquisition_orbit")
        public Double orbit;

        @JsonProperty("sar__instrument_mode")
        public String instrumentMode;

        @JsonProperty("generated__has_overview")
        public boolean hasOverview;

        @JsonProperty("generated__day_of_week")
        public int dayOfWeek;

        @JsonProperty("generated__day_of_year")
        public int dayOfYear;

        @JsonProperty("generated__hour_of_day")
        public int hourOfDay;

        @JsonProperty("generated__minute_of_day")
        public int minuteOfDay;

        @JsonProperty("generated__month")
        public int month;

        @JsonProperty("generated__year")
        public int year;

        @JsonProperty("generated__season")
        public String season;

        @JsonProperty("generated__tltrbrbl")
        public List<LngLatAlt> corners;

        @JsonProperty("generated__band_common_names")
        public List<String> bandCommonNames;

        @JsonProperty("generated__band_names")
        public List<String> bandNames;

        @JsonProperty("generated__geohash2")
        public String geohash2;

        @JsonProperty("generated__geohash3")
        public String geohash3;

        @JsonProperty("generated__geohash4")
        public String geohash4;

        @JsonProperty("generated__geohash5")
        public String geohash5;

        @JsonProperty("endpoint_url")
        public String endpointUrl;
    }

    public final static String GEOMETRY_PATH = "geometry";
    public final static String CENTROID_PATH = "centroid";
    public final static String TIMESTAMP_PATH = "properties.datetime";

}



