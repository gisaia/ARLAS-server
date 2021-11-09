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

package io.arlas.server.core.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DublinCoreElementName implements Serializable {
    private static final long serialVersionUID = -3452567240629298463L;

    public DublinCoreElementName(){
    }

    @JsonProperty(value = "title")
    public String title = "";

    @JsonProperty(value = "creator")
    public String creator = "";

    @JsonProperty(value = "subject")
    public String subject = "";

    @JsonProperty(value = "description")
    public String description = "";

    @JsonProperty(value = "publisher")
    public String publisher = "";

    @JsonProperty(value = "contributor")
    public String contributor = "";

    @JsonProperty(value = "type")
    public String type = "";

    @JsonProperty(value = "format")
    public String format = "";

    @JsonProperty(value = "identifier")
    public String identifier = String.valueOf(java.util.UUID.randomUUID());

    @JsonProperty(value = "source")
    public String source = "";

    @JsonProperty(value = "language")
    public String language = "";

    @JsonProperty(value = "bbox")
    public Bbox bbox;

    private Date date = new Date();
    @JsonGetter(value = "date")
    public String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
       return simpleDateFormat.format(date);
    }

    private Polygon coverageGeometry;

    private JSONObject coverage;
    @JsonGetter(value = "coverage")
    public JSONObject getCoverage() {
        if (bbox != null) {
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
        }
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
        }
        return coverageCentroid;
    }

    public class Bbox implements Serializable {
        private static final long serialVersionUID = 364766455618062216L;

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
