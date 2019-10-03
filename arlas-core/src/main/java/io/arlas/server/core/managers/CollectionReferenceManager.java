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

package io.arlas.server.core.managers;

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.utils.GeoTypeMapper;
import io.arlas.server.core.utils.MapExplorer;

public class CollectionReferenceManager {

    public static void setCollectionGeometriesType(Object source, CollectionReference collectionReference) throws ArlasException {
        if (collectionReference.params.getGeometryType() == null) {
            Object geometry = collectionReference.params.geometryPath != null ?
                    MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source) : null;
            if (geometry != null) {
                collectionReference.params.setGeometryType(GeoTypeMapper.getGeometryType(geometry));
            }
        }
        if (collectionReference.params.getCentroidType() == null) {
            Object centroid = collectionReference.params.centroidPath != null ?
                    MapExplorer.getObjectFromPath(collectionReference.params.centroidPath, source) : null;
            if (centroid != null) {
                collectionReference.params.setCentroidType(GeoTypeMapper.getGeometryType(centroid));
            }
        }
    }
}
