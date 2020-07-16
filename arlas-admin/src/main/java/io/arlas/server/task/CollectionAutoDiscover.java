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

package io.arlas.server.task;

import com.google.common.collect.ImmutableMultimap;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.CollectionAutoDiscoverConfiguration;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.utils.MapExplorer;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CollectionAutoDiscover extends Task implements Runnable {

    private CollectionReferenceDao collectionDao;
    private CollectionAutoDiscoverConfiguration configuration;

    Logger LOGGER = LoggerFactory.getLogger(CollectionAutoDiscover.class);

    public CollectionAutoDiscover(CollectionReferenceDao collectionDao, ArlasServerConfiguration configuration) {
        super("collection-auto-discover");
        this.configuration = configuration.collectionAutoDiscoverConfiguration;
        this.collectionDao = collectionDao;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter arg1) throws Exception {
        try {
            List<CollectionReferenceDescription> discoveredCollections = collectionDao.getAllIndicesAsCollections();
            List<CollectionReferenceDescription> existingCollections;
            try {
                existingCollections = collectionDao.describeAllCollections(collectionDao.getAllCollectionReferences(Optional.empty()), Optional.empty());
            } catch (Exception e) {
                existingCollections = new ArrayList<>();
            }
            for (CollectionReferenceDescription collection : discoveredCollections) {
                if (!existingCollections.contains(collection)) {
                    CollectionReferenceDescription collectionToAdd = checkCollectionValidity(collection);
                    if (collectionToAdd != null) {
                        collectionDao.putCollectionReference(collectionToAdd);
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
        if (collection.params.idPath == null || collection.params.idPath.isEmpty()
                || collection.params.centroidPath == null || collection.params.centroidPath.isEmpty()
                || collection.params.geometryPath == null || collection.params.geometryPath.isEmpty()
                || collection.params.timestampPath == null || collection.params.timestampPath.isEmpty())
            return null;
        return collection;
    }

    @Override
    public void run() {
        try {
            execute(null, null);
        } catch (Exception e) {
            LOGGER.debug("Unable to run scheduled task " + this.getClass().getCanonicalName());
        }
    }

}
