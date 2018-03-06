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

import java.util.Map;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.TimestampTypeMapper;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Hit {

    public MD md;

    public Object data;

    public Hit() {}
    
    public Hit(CollectionReference collectionReference, Map<String,Object> source) throws ArlasException {
        data = source;
        md = new MD();
        if (collectionReference.params.idPath != null) {
            md.id = "" + MapExplorer.getObjectFromPath(collectionReference.params.idPath, source);
        }
        if (collectionReference.params.centroidPath != null) {
            Object m = MapExplorer.getObjectFromPath(collectionReference.params.centroidPath, source);
            if(m != null) {
                md.centroid = GeoTypeMapper.getGeoJsonObject(m);
            }
        }
        if (collectionReference.params.geometryPath != null) {
            Object m = MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source);
            if(m != null) {
                md.geometry = GeoTypeMapper.getGeoJsonObject(m);
            }
        }
        if (collectionReference.params.timestampPath != null) {
            Object t = MapExplorer.getObjectFromPath(collectionReference.params.timestampPath, source);
            if (t != null) {
                String f = collectionReference.params.customParams.get(CollectionReference.TIMESTAMP_FORMAT);
                md.timestamp = TimestampTypeMapper.getTimestamp(t, f);
            }
        }
    }
}
