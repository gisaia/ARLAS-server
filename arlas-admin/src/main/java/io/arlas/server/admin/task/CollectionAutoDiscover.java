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

package io.arlas.server.admin.task;

import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.CollectionAutoDiscoverConfiguration;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.MapExplorer;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

public class CollectionAutoDiscover extends Task implements Runnable {

    private final CollectionReferenceService collectionReferenceService;
    private final CollectionAutoDiscoverConfiguration configuration;

    Logger LOGGER = LoggerFactory.getLogger(CollectionAutoDiscover.class);

    public CollectionAutoDiscover(CollectionReferenceService collectionReferenceService, ArlasServerConfiguration configuration) {
        super("collection-auto-discover");
        this.configuration = configuration.collectionAutoDiscoverConfiguration;
        this.collectionReferenceService = collectionReferenceService;
    }

    @Override
    public void execute(Map<String,List<String>> arg0, PrintWriter arg1) throws Exception {
        try {
            List<CollectionReferenceDescription> discoveredCollections = collectionReferenceService.getAllIndicesAsCollections();
            List<CollectionReferenceDescription> existingCollections;
            try {
                existingCollections = collectionReferenceService.describeAllCollections(collectionReferenceService.getAllCollectionReferences(Optional.empty()), Optional.empty());
            } catch (Exception e) {
                existingCollections = new ArrayList<>();
            }
            for (CollectionReferenceDescription collection : discoveredCollections) {
                if (!existingCollections.contains(collection)) {
                    CollectionReferenceDescription collectionToAdd = checkCollectionValidity(collection);
                    if (collectionToAdd != null) {
                        collectionReferenceService.putCollectionReference(collectionToAdd);
                    }
                }
            }
        } catch (ArlasConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private CollectionReferenceDescription checkCollectionValidity(CollectionReferenceDescription collection) throws ArlasConfigurationException {
        List<String> idPaths = configuration.getPreferredIdFieldNames();
        List<String> timestampPaths = configuration.getPreferredTimestampFieldNames();
        List<String> centroidPaths = configuration.getPreferredCentroidFieldNames();
        List<String> geometryPaths = configuration.getPreferredGeometryFieldNames();
        List<String> h3Paths = configuration.getPreferredH3FieldNames();
        for (String path : idPaths) {
            Object field = MapExplorer.getObjectFromPath(path, collection.properties);
            if (field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.idPath = path;
                break;
            }
        }
        for (String path : timestampPaths) {
            Object field = MapExplorer.getObjectFromPath(path, collection.properties);
            if (field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.timestampPath = path;
                if (((CollectionReferenceDescriptionProperty) field).format != null) {
                    collection.params.customParams = new HashMap<>();
                    collection.params.customParams.put(CollectionReference.TIMESTAMP_FORMAT, ((CollectionReferenceDescriptionProperty) field).format);
                }
                break;
            }
        }
        for (String path : centroidPaths) {
            Object field = MapExplorer.getObjectFromPath(path, collection.properties);
            if (field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.centroidPath = path;
                break;
            }
        }
        for (String path : geometryPaths) {
            Object field = MapExplorer.getObjectFromPath(path, collection.properties);
            if (field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.geometryPath = path;
                break;
            }
        }
        for (String path : h3Paths) {
            Object field = MapExplorer.getObjectFromPath(path, collection.properties);
            if (field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.h3Path = path;
                break;
            }
        }
        if (StringUtil.isNullOrEmpty(collection.params.idPath)
                || StringUtil.isNullOrEmpty(collection.params.centroidPath)
                || StringUtil.isNullOrEmpty(collection.params.geometryPath)
                || StringUtil.isNullOrEmpty(collection.params.timestampPath))
            return null;
        return collection;    }

    @Override
    public void run() {
        try {
            execute(null, null);
        } catch (Exception e) {
            LOGGER.debug("Unable to run scheduled task " + this.getClass().getCanonicalName());
        }
    }

}
