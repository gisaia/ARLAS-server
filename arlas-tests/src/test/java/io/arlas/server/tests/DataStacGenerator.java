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

import org.geojson.LngLatAlt;
import org.geojson.Polygon;

import java.util.*;

public class DataStacGenerator {

    private static final int NUM_FEATURES = 60;
    private static final long SEED = 12345L;
    private static final Random random = new Random(SEED);
    private static final Calendar c = Calendar.getInstance();

    public static List<DataStac> generateDataStacList() {
        List<DataStac> features = new ArrayList<>();
        for (int i = 0; i < NUM_FEATURES; i++) {
            features.add(generateDataStac(i));
        }
        return features;
    }

    private static DataStac generateDataStac(int index) {
        DataStac feature = new DataStac();
        feature.collection = "geodes";
        feature.catalog = "S2L2A";
        feature.id = "feature-" + index;
        feature.type = "Feature";
        // Geometry
        List<LngLatAlt> coords = new ArrayList<>();
        LngLatAlt origin = new LngLatAlt(randomLongitude(), randomLatitude());
        coords.add(origin);
        coords.add(new LngLatAlt(origin.getLongitude() + 5, origin.getLatitude()));
        coords.add(new LngLatAlt(origin.getLongitude() + 5, origin.getLatitude() - 5));
        coords.add(new LngLatAlt(origin.getLongitude() , origin.getLatitude() - 5));
        coords.add(origin); // Close polygon
        feature.geometry = new Polygon(coords);

        // Bounding box and centroid
        feature.bbox = calculateBoundingBox(feature.geometry);
        feature.centroid = getCentroid(feature.geometry);

        // Assets
        DataStac.Asset overview = new DataStac.Asset();
        overview.name = "overview";
        overview.href = "https://example.com/overview/" + index + ".jpg";
        overview.title = "Overview " + index;
        overview.description = "Overview for feature " + index;
        overview.type = "image/jpeg";
        overview.roles = Collections.singletonList("overview");
        feature.assets.put("overview",overview);

        // Properties
        long baseTime = 1743546822L;
        feature.properties.datetime = baseTime + index * 60;
        feature.properties.startDatetime= baseTime + index * 60 - 1800;
        feature.properties.endDatetime = baseTime + index * 60;
        feature.properties.keywords = Arrays.asList("static", "test", "feature" + index);
        feature.properties.platform = "S2B";
        feature.properties.instrument = "MSI";
        feature.properties.dataType = "S2MSI1C";
        feature.properties.itemType = "GRIDDED";
        feature.properties.itemFormat = "SAFE";
        feature.properties.cloudCover = random.nextDouble() * 100;
        feature.properties.bands = Collections.emptyList();
        feature.properties.processingLevel = "L1C";
        feature.properties.orbitDirection = "DESCENDING";
        feature.properties.orbit = 42000.0 + random.nextDouble() * 1000;
        feature.properties.instrumentMode = "INS-NOBS";
        feature.properties.hasOverview = true;
        c.setTime(new Date(feature.properties.datetime * 1000));
        feature.properties.dayOfWeek =  c.get(Calendar.DAY_OF_WEEK);
        feature.properties.dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        feature.properties.hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        feature.properties.minuteOfDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        feature.properties.month = c.get(Calendar.MONTH);
        feature.properties.year = c.get(Calendar.YEAR);
        feature.properties.season = "Spring";
        feature.properties.corners = calculateCorner(feature.geometry);
        feature.properties.bandCommonNames = Collections.emptyList();
        feature.properties.bands = Collections.emptyList();
        feature.properties.geohash2 = "3h";
        feature.properties.geohash3 = "3hp";
        feature.properties.geohash4 = "3hpm";
        feature.properties.geohash5 = "3hpmc";
        feature.properties.endpointUrl = "https://example.com/data/" + index + ".zip";

        return feature;
    }

    private static double randomLatitude() {
        return -45 + random.nextDouble() * 90;
    }

    private static double randomLongitude() {
        return -179 + random.nextDouble() * 360;
    }

    public static double[] getCentroid(Polygon polygon) {
        List<LngLatAlt> ring = polygon.getCoordinates().get(0);
        double sumX = 0;
        double sumY = 0;
        int count = 0;
        for (LngLatAlt coord : ring) {
            sumX += coord.getLongitude();
            sumY += coord.getLatitude();
            count++;
        }
        double centroidX = sumX / count;
        double centroidY = sumY / count;
        return new double[]{centroidX, centroidY};
    }

    public static double[] calculateBoundingBox(Polygon polygon) {
        double minLat = -90;
        double minLon = -180;
        double maxLat = 90;
        double maxLon = -180;
        List<LngLatAlt> outerRing = polygon.getCoordinates().get(0);
        for (LngLatAlt coord : outerRing) {
            double lon = coord.getLongitude();
            double lat = coord.getLatitude();
            if (lon < minLon) minLon = lon;
            if (lon > maxLon) maxLon = lon;
            if (lat < minLat) minLat = lat;
            if (lat > maxLat) maxLat = lat;
        }
        return new double[]{minLon, minLat, maxLon, maxLat};
    }
    public static List<LngLatAlt> calculateCorner(Polygon polygon){
        double[] bbox = calculateBoundingBox(polygon);
        List<LngLatAlt> corners = new ArrayList<>();
        //generated__tltrbrbl
        corners.add(new LngLatAlt(bbox[0],bbox[3]));
        corners.add(new LngLatAlt(bbox[2],bbox[3]));
        corners.add(new LngLatAlt(bbox[2],bbox[1]));
        corners.add(new LngLatAlt(bbox[0],bbox[1]));

        return corners;
    }
}

