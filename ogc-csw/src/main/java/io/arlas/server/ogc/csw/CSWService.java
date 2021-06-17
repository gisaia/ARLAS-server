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

package io.arlas.server.ogc.csw;

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.InspireConfiguration;
import io.arlas.server.core.app.OGCConfiguration;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.MetaCollectionReferenceParameters;
import io.arlas.server.core.model.OgcInspireConfigurationParameters;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;

import java.util.List;
import java.util.Optional;

public class CSWService extends CSWRESTService {

    private static final String META_COLLECTION_ID_PATH = "dublin_core_element_name.identifier";
    private static final String META_COLLECTION_GEOMETRY_PATH = "dublin_core_element_name.coverage";
    private static final String META_COLLECTION_CENTROID_PATH = "dublin_core_element_name.coverage_centroid";
    private static final String META_COLLECTION_TIMESTAMP_PATH = "dublin_core_element_name.date";

    public CSWService(CollectionReferenceService collectionReferenceService, OGCCollectionReferenceDao ogcDao, CSWHandler cswHandler, ArlasServerConfiguration configuration) throws ArlasException {
        super(cswHandler);
        this.collectionReferenceService = collectionReferenceService;
        initMetaCollection(configuration.arlasIndex, configuration.ogcConfiguration, configuration.inspireConfiguration);
        this.ogcDao = ogcDao;
    }

    private void initMetaCollection(String index, OGCConfiguration ogcConfiguration, InspireConfiguration inspireConfiguration) throws ArlasException {
        List<CollectionReference> collectionReferences =  collectionReferenceService.getAllCollectionReferences(Optional.empty());
        long count = collectionReferences.stream().filter(collectionReference -> collectionReference.collectionName.equals(getMetacollectionName())).count();
        if (count > 0) {
            collectionReferenceService.deleteCollectionReference(getMetacollectionName());
        }
        CollectionReference metacolletion = createMetaCollection(index, ogcConfiguration, inspireConfiguration);
        collectionReferenceService.putCollectionReference(metacolletion);
    }

    private CollectionReference createMetaCollection(String index, OGCConfiguration ogcConfiguration, InspireConfiguration inspireConfiguration) {
        CollectionReference collectionReference =  new CollectionReference();
        collectionReference.collectionName = getMetacollectionName();
        MetaCollectionReferenceParameters collectionReferenceParameters = new MetaCollectionReferenceParameters();
        collectionReferenceParameters.indexName = index;
        collectionReferenceParameters.idPath = META_COLLECTION_ID_PATH;
        collectionReferenceParameters.geometryPath = META_COLLECTION_GEOMETRY_PATH;
        collectionReferenceParameters.centroidPath = META_COLLECTION_CENTROID_PATH;
        collectionReferenceParameters.timestampPath = META_COLLECTION_TIMESTAMP_PATH;
        collectionReferenceParameters.inspireConfigurationParameters = new OgcInspireConfigurationParameters();
        collectionReferenceParameters.inspireConfigurationParameters.reponsibleParty = ogcConfiguration.serviceProviderName;
        collectionReferenceParameters.inspireConfigurationParameters.reponsiblePartyRole = ogcConfiguration.serviceProviderRole;
        collectionReferenceParameters.inspireConfigurationParameters.setConformityParameter();
        collectionReferenceParameters.inspireConfigurationParameters.publicAccessLimitations = inspireConfiguration.publicAccessLimitations;
        collectionReferenceParameters.inspireConfigurationParameters.accessAndUseConditions = inspireConfiguration.accessAndUseConditions;
        collectionReference.params = collectionReferenceParameters;
        return collectionReference;
    }

}
