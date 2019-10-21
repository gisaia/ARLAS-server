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

package io.arlas.server.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.StringUtil;
import io.arlas.server.utils.TimestampTypeMapper;
import io.dropwizard.jackson.JsonSnakeCase;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsonSnakeCase
public class Hit {
    private static Logger LOGGER = LoggerFactory.getLogger(Hit.class);

    public MD md;

    public Object data;

    @JsonIgnore
    private Map<String, Object> dataAsMap;

    @JsonIgnore
    private Map<String, GeoJsonObject> geometriesAsMap;

    @JsonIgnore
    private boolean flat;

    public Hit() {
    }

    public Hit(CollectionReference collectionReference, Map<String, Object> source, Boolean flat, Boolean ignoreGeo) throws ArlasException {
        this(collectionReference, source, null, flat, ignoreGeo);
    }

    public Hit(CollectionReference collectionReference, Map<String, Object> source, String returned_geometries, Boolean flat, Boolean ignoreGeo) throws ArlasException {
        this.flat = flat;
        this.geometriesAsMap = new HashMap<>();

        md = new MD();
        if (collectionReference.params.idPath != null) {
            md.id = "" + MapExplorer.getObjectFromPath(collectionReference.params.idPath, source);
        }
        CollectionReferenceManager.setCollectionGeometriesType(source, collectionReference, returned_geometries);
        if (collectionReference.params.centroidPath != null) {
            try {
                Object m = MapExplorer.getObjectFromPath(collectionReference.params.centroidPath, source);
                if (m != null) {
                    md.centroid = GeoTypeMapper.getGeoJsonObject(m, collectionReference.params.getGeometryType(collectionReference.params.centroidPath));
                    this.geometriesAsMap.put(collectionReference.params.centroidPath, md.centroid);
                }
            } catch (ArlasException e) {
                // no exception is thrown as this Hit is returned as response of a `_search` query where the geometry is not necessarily needed
                LOGGER.error(e.getMessage());            }
        }
        if (collectionReference.params.geometryPath != null) {
            try {
                Object m = MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source);
                if (m != null) {
                    md.geometry = GeoTypeMapper.getGeoJsonObject(m, collectionReference.params.getGeometryType(collectionReference.params.geometryPath));
                    this.geometriesAsMap.put(collectionReference.params.geometryPath, md.geometry);
                }
            } catch (ArlasException e) {
                // no exception is thrown as this Hit is returned as response of a `_search` query where the geometry is not necessarily needed
                LOGGER.error(e.getMessage());
            }
        }
        if (collectionReference.params.timestampPath != null) {
            Object t = MapExplorer.getObjectFromPath(collectionReference.params.timestampPath, source);
            if (t != null) {
                String f = collectionReference.params.customParams.get(CollectionReference.TIMESTAMP_FORMAT);
                md.timestamp = TimestampTypeMapper.getTimestamp(t, f);
            }
        }

        if (returned_geometries != null) {
            md.returnedGeometries = new ArrayList<>();
            for (String path : returned_geometries.split(",")) {
                // skip centroidPath and geometryPath if requested, as they are already set before
                if (!path.equals(collectionReference.params.centroidPath)
                        && !path.equals(collectionReference.params.geometryPath)) {
                    Object m = MapExplorer.getObjectFromPath(path, source);
                    if (m != null) {
                        GeoJsonObject geoJsonObject = GeoTypeMapper.getGeoJsonObject(m, collectionReference.params.getGeometryType(path));
                        md.returnedGeometries.add(new Geo(path, geoJsonObject));
                        this.geometriesAsMap.put(path, geoJsonObject);
                    }
                }
            }
        }

        if (ignoreGeo) {
            getGeoPathsToExcludeFromResponse(collectionReference).stream().forEach(e->{
                if (e.contains(".")) {
                    String pathToRemove = e.substring(0,e.lastIndexOf("."));
                    String keyToRemove = e.substring(e.lastIndexOf(".")+1);
                    Optional.ofNullable((Map) MapExplorer.getObjectFromPath(pathToRemove, source)).map(objectWithAttributeToRemove -> objectWithAttributeToRemove.remove(keyToRemove));
                } else {
                    source.remove(e);
                }
            });
        }
        dataAsMap = flat ? MapExplorer.flat(source,new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR), new HashSet<>()) : source;
        data = dataAsMap;
    }

    public boolean isFlat() {
        return flat;
    }

    public Map<String, Object> getDataAsMap() {
        return dataAsMap;
    }

    private Set<String> getGeoPathsToExcludeFromResponse(CollectionReference collectionReference){
        Set<String> excludeFromData = new HashSet<>();
        //excludeFromData.add(collectionReference.params.centroidPath); TODO : decide whether the centroid should be in the response or not
        if(!StringUtil.isNullOrEmpty(collectionReference.params.geometryPath)){
            excludeFromData.add(collectionReference.params.geometryPath);
        }
        return excludeFromData;
    }

    public GeoJsonObject getGeometry(String path) {
        return this.geometriesAsMap.get(path);
    }
}