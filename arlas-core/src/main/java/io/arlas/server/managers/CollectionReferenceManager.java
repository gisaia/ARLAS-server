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

package io.arlas.server.managers;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.impl.elastic.utils.GeoTypeMapper;
import io.arlas.server.services.CollectionReferenceService;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.FieldType;
import io.arlas.server.utils.MapExplorer;

public class CollectionReferenceManager {
    private CacheManager cacheManager;
    public CollectionReferenceService collectionReferenceService;

    private final static CollectionReferenceManager collectionReferenceManager = new CollectionReferenceManager();

    public static CollectionReferenceManager getInstance() {
        return collectionReferenceManager;
    }

    private CollectionReferenceManager() {
    }

    public CollectionReferenceService getCollectionReferenceService() {
        return collectionReferenceService;
    }

    public void init(CollectionReferenceService collectionReferenceService, CacheManager cacheManager) {
        this.collectionReferenceService = collectionReferenceService;
        this.cacheManager = cacheManager;
    }

    public FieldType getType(CollectionReference collectionReference, String field, boolean throwException) throws ArlasException {
        FieldType fieldType = cacheManager.getFieldType(collectionReference.collectionName, field);
        if (fieldType == null) {
            String[] props = field.split("\\.");
            CollectionReferenceDescriptionProperty esField = collectionReferenceService.describeCollection(collectionReference).properties.get(props[0]);
            if (esField == null) {
                return getUnknownType(field, collectionReference.collectionName, throwException);
            }
            for (int i=1; i<props.length; i++) {
                esField = esField.properties.get(props[i]);
                if (esField == null) {
                    return getUnknownType(field, collectionReference.collectionName, throwException);
                }
            }
            if (esField != null) {
                fieldType = esField.type;
                cacheManager.putFieldType(collectionReference.collectionName, field, fieldType);
            } else {
                return getUnknownType(field, collectionReference.collectionName, throwException);
            }
        }
        return fieldType;
    }

    public static void setCollectionGeometriesType(Object source, CollectionReference collectionReference) throws ArlasException {
        setCollectionGeometriesType(source, collectionReference, null);
    }

    public static void setCollectionGeometriesType(Object source, CollectionReference collectionReference, String returned_geometries) throws ArlasException {
        if (collectionReference.params.getGeometryType(collectionReference.params.geometryPath) == null) {
            Object geometry = collectionReference.params.geometryPath != null ?
                    MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source) : null;
            if (geometry != null) {
                collectionReference.params.setGeometryType(collectionReference.params.geometryPath,
                        GeoTypeMapper.getGeometryType(geometry));
            }
        }
        if (collectionReference.params.getGeometryType(collectionReference.params.centroidPath) == null) {
            Object centroid = collectionReference.params.centroidPath != null ?
                    MapExplorer.getObjectFromPath(collectionReference.params.centroidPath, source) : null;
            if (centroid != null) {
                collectionReference.params.setGeometryType(collectionReference.params.centroidPath,
                        GeoTypeMapper.getGeometryType(centroid));
            }
        }
        if (returned_geometries != null) {
            for (String path : returned_geometries.split(",")) {
                if (collectionReference.params.getGeometryType(path) == null) {
                    Object geometry = MapExplorer.getObjectFromPath(path, source);
                    if (geometry != null)
                        collectionReference.params.setGeometryType(path, GeoTypeMapper.getGeometryType(geometry));
                }
            }
        }
    }

    private FieldType getUnknownType(String parentField, String collectionName, boolean throwException) throws ArlasException{
        if (throwException) {
            throw new NotFoundException("Field '" + parentField + "' not found in collection " + collectionName);
        } else {
            return FieldType.UNKNOWN;
        }
    }
}