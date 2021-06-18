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

package io.arlas.server.core.impl.jdbi.model;

import io.arlas.server.core.model.enumerations.AggregatedGeometryEnum;
import io.arlas.server.core.model.enumerations.CollectionFunction;

import java.util.Arrays;
import java.util.List;

import static io.arlas.server.core.impl.jdbi.model.SelectRequest.US;
import static io.arlas.server.core.services.FluidSearchService.AGGREGATED_GEOMETRY_SUFFIX;
import static io.arlas.server.core.utils.StringUtil.concat;

public class ColumnQualifier {
    private static List<String> metrics = Arrays.asList(
            CollectionFunction.AVG.name().toLowerCase(),
            CollectionFunction.CARDINALITY.name().toLowerCase(),
            CollectionFunction.MAX.name().toLowerCase(),
            CollectionFunction.MIN.name().toLowerCase(),
            CollectionFunction.SUM.name().toLowerCase(),
            CollectionFunction.GEOCENTROID.name().toLowerCase(),
            CollectionFunction.GEOBBOX.name().toLowerCase()
            );

    private static List<String> geoMetrics = Arrays.asList(
            CollectionFunction.GEOCENTROID.name().toLowerCase(),
            CollectionFunction.GEOBBOX.name().toLowerCase()
            );

    private static String bbox = concat(AggregatedGeometryEnum.BBOX.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, "");
    private static String centroid = concat(AggregatedGeometryEnum.CENTROID.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, "");

    private static List<String> aggGeos = Arrays.asList(
            bbox,
            centroid,
            concat(AggregatedGeometryEnum.GEOHASH.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, ""),
            concat(AggregatedGeometryEnum.GEOHASH_CENTER.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, ""),
            concat(AggregatedGeometryEnum.CELL.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, ""),
            concat(AggregatedGeometryEnum.CELLCENTER.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, "")
            );

    public static String getQualifier(CollectionFunction fct, String field) {
        return concat(fct.name().toLowerCase(),
                US, field.replace(".", ""));
    }

    public static String getQualifier(AggregatedGeometryEnum ag, String field) {
        return concat(concat(ag.value(), AGGREGATED_GEOMETRY_SUFFIX).replace(US, ""),
                US, field.replace(".", ""));
    }

    public static String[] splitQualifier(String qualifier) {
        return qualifier.split(US, 2);
    }
    
    public static boolean isMetric(String name) {
        return metrics.contains(name.split(US)[0]);
    }

    public static boolean isGeoMetric(String name) {
        return geoMetrics.contains(name.split(US)[0]);
    }

    public static boolean isGeoMetricBbox(String name) {
        return CollectionFunction.GEOBBOX.name().toLowerCase().equals(name.split(US)[0]);
    }

    public static boolean isGeoMetricCentroid(String name) {
        return CollectionFunction.GEOCENTROID.name().toLowerCase().equals(name.split(US)[0]);
    }

    public static boolean isAggregatedGeometry(String name) {
        return aggGeos.contains(name.split(US)[0]);
    }

    public static boolean isAggregatedGeoBbox(String name) {
        return bbox.equals(name.split(US)[0]);
    }

    public static boolean isAggregatedGeoCentroid(String name) {
        return centroid.equals(name.split(US)[0]);
    }
}
