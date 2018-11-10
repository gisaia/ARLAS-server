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

package io.arlas.server.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DublinCoreElementName {

    public static final String DEFAULT_TITLE = "WFS service (download service) published by ARLAS";
    public static final String DEFAULT_DESCRIPTION = "WFS service (download service) published by ARLAS";

    public DublinCoreElementName(){
    }

    @JsonProperty(value = "title", required = false)
    public String title = "";

    @JsonProperty(value = "creator", required = false)
    public String creator = "";

    @JsonProperty(value = "subject", required = false)
    public String subject = "";

    @JsonProperty(value = "description", required = false)
    public String description = "";

    @JsonProperty(value = "publisher", required = false)
    public String publisher = "";

    @JsonProperty(value = "contributor", required = false)
    public String contributor = "";

    @JsonProperty(value = "type", required = false, defaultValue = "service")
    public String type = "service";

    @JsonProperty(value = "format", required = false)
    public String format = "";

    @JsonProperty(value = "identifier", required = false)
    public String identifier = String.valueOf(java.util.UUID.randomUUID());

    @JsonProperty(value = "source", required = false)
    public String source = "";

    @JsonProperty(value = "language", required = false, defaultValue = "eng")
    public String language = "eng";

    @JsonProperty(value = "bbox", required = false)
    public Bbox bbox = new Bbox();

    private Date date = new Date();
    @JsonGetter(value = "date")
    public String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
       return simpleDateFormat.format(date);
    }

    private Polygon coverageGeometry;

    private JSONObject coverage;
    @JsonGetter(value = "coverage")
    public JSONObject getCoverage(){
        coverageGeometry = new org.geojson.Polygon();
        List<LngLatAlt> exteriorRing = new ArrayList<>();
        exteriorRing.add(new LngLatAlt(bbox.west, bbox.south));
        exteriorRing.add(new LngLatAlt(bbox.east, bbox.south));
        exteriorRing.add(new LngLatAlt(bbox.east, bbox.north));
        exteriorRing.add(new LngLatAlt(bbox.west, bbox.north));
        exteriorRing.add(new LngLatAlt(bbox.west, bbox.south));
        coverageGeometry.setExteriorRing(exteriorRing);
        coverage = new JSONObject();
        JSONArray jsonArayExt = new JSONArray();
        coverageGeometry.getExteriorRing().forEach(lngLatAlt -> {
            JSONArray jsonArayLngLat = new JSONArray();
            jsonArayLngLat.add(0, lngLatAlt.getLongitude());
            jsonArayLngLat.add(1, lngLatAlt.getLatitude());
            jsonArayExt.add(jsonArayLngLat);
        });
        JSONArray jsonAray = new JSONArray();
        jsonAray.add(jsonArayExt);
        coverage.put("type", "Polygon");
        coverage.put("coordinates", jsonAray);
        return coverage;
    }

    private String coverageCentroid;
    @JsonGetter(value = "coverage_centroid")
    public String getCoverageCentroid(){
        if (coverageGeometry != null) {
            LngLatAlt bottomLeft = coverageGeometry.getExteriorRing().get(0);
            LngLatAlt topRight = coverageGeometry.getExteriorRing().get(2);
            double centroidLat = (bottomLeft.getLatitude() + topRight.getLatitude()) / 2;
            double centroidLng = (bottomLeft.getLongitude() + topRight.getLongitude()) / 2;
            coverageCentroid = centroidLat + "," + centroidLng;
        } else {
            coverageCentroid = "0,0";
        }
        return coverageCentroid;
    }

    public class Bbox {
        @JsonProperty(value = "north", required = true)
        public double north = 90.0;

        @JsonProperty(value = "south", required = true)
        public double south = -90.0;

        @JsonProperty(value = "east", required = true)
        public double east = 180.0;

        @JsonProperty(value = "west", required = true)
        public double west = -180.0;
    }

}
