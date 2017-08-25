package io.arlas.server.task;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.arlas.server.exceptions.ArlasConfigurationException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.utils.MapExplorer;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMultimap;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.CollectionAutoDiscoverConfiguration;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.rest.collections.CollectionRESTServices;
import io.dropwizard.servlets.tasks.Task;

public class CollectionAutoDiscover extends Task implements Runnable {
    
    private ElasticAdmin admin;
    private CollectionReferenceDao collectionDao;
    private CollectionAutoDiscoverConfiguration configuration;

    Logger LOGGER = LoggerFactory.getLogger(CollectionAutoDiscover.class);

    public CollectionAutoDiscover(TransportClient client, ArlasServerConfiguration configuration) {
        super("collection-auto-discover");
        this.admin = new ElasticAdmin(client);
        this.configuration = configuration.collectionAutoDiscoverConfiguration;
        this.collectionDao = new ElasticCollectionReferenceDaoImpl(client, configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
    }

    @Override
    public void execute(ImmutableMultimap<String, String> arg0, PrintWriter arg1) throws Exception {
        try {
            List<CollectionReferenceDescription> discoveredCollections = admin.getAllIndecesAsCollections();
            List<CollectionReferenceDescription> existingCollections = null;
            try {
                existingCollections = admin.describeAllCollections(collectionDao.getAllCollectionReferences());
            } catch (Exception e) {
                existingCollections = new ArrayList<>();
            }
            for (CollectionReferenceDescription collection : discoveredCollections) {
                if (!existingCollections.contains(collection)) {
                    CollectionReferenceDescription collectionToAdd = checkCollectionValidity(collection);
                    if (collectionToAdd != null) {
                        collectionDao.putCollectionReference(collectionToAdd.collectionName, collectionToAdd.params);
                    }
                }
            }
        } catch(ArlasConfigurationException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    private CollectionReferenceDescription checkCollectionValidity(CollectionReferenceDescription collection) throws ArlasConfigurationException {
        List<String> idPaths = configuration.getPreferredIdFieldNames();
        List<String> timestampPaths = configuration.getPreferredTimestampFieldNames();
        List<String> centroidPaths = configuration.getPreferredCentroidFieldNames();
        List<String> geometryPaths = configuration.getPreferredGeometryFieldNames();
        for(String path : idPaths) {
            Object field = MapExplorer.getObjectFromPath(path,collection.properties);
            if(field != null && field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.idPath = path;
                break;
            }
        }
        for(String path : timestampPaths) {
            Object field = MapExplorer.getObjectFromPath(path,collection.properties);
            if(field != null && field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.timestampPath = path;
                if (((CollectionReferenceDescriptionProperty)field).format != null){
                    collection.params.custom_params = new HashMap<>();
                    collection.params.custom_params.put(CollectionReference.TIMESTAMP_FORMAT,((CollectionReferenceDescriptionProperty)field).format);
                }
                break;
            }
        }
        for(String path : centroidPaths) {
            Object field = MapExplorer.getObjectFromPath(path,collection.properties);
            if(field != null && field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.centroidPath = path;
                break;
            }
        }
        for(String path : geometryPaths) {
            Object field = MapExplorer.getObjectFromPath(path,collection.properties);
            if(field != null && field instanceof CollectionReferenceDescriptionProperty) {
                collection.params.geometryPath = path;
                break;
            }
        }
        if(collection.params.idPath == null || collection.params.idPath.isEmpty()
                || collection.params.centroidPath == null || collection.params.centroidPath.isEmpty()
                || collection.params.geometryPath == null || collection.params.geometryPath.isEmpty()
                || collection.params.timestampPath == null || collection.params.timestampPath.isEmpty())
            return null;
        return collection;
    }

    @Override
    public void run() {
        try {
            execute(null,null);
        } catch (Exception e) {
            LOGGER.debug("Unable to run scheduled task " + this.getClass().getCanonicalName());
        }
    }

}
