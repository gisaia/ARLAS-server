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

package io.arlas.server.ogc.common.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.exceptions.NotImplementedException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferenceParameters;
import io.arlas.server.core.model.CollectionReferences;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.core.utils.BoundingBox;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;

public class JdbiOGCCollectionReferenceDao implements OGCCollectionReferenceDao {

    private final String arlasIndex;
    private final Service service;
    private final CollectionReferenceService collectionReferenceService;

    private static ObjectMapper mapper;
    private static ObjectReader reader;

    static {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        reader = mapper.readerFor(CollectionReferenceParameters.class);
    }

    public JdbiOGCCollectionReferenceDao(Jdbi jdbi, CollectionReferenceService collectionReferenceService, String index, Service service) {
        this.collectionReferenceService = collectionReferenceService;
        this.arlasIndex = index;
        this.service = service;
    }

    @Override
    public CollectionReferences getCollectionReferences(String[] includes, String[] excludes, int size, int from, String[] ids, String q, String constraint, BoundingBox boundingBox) throws ArlasException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public CollectionReferences getCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from, String[] ids, String q, String constraint, BoundingBox boundingBox, CollectionReference collectionReferenceToRemove) throws ArlasException, IOException {
        throw new NotImplementedException();
    }

    @Override
    public CollectionReferences getAllCollectionReferencesExceptOne(String[] includes, String[] excludes, int size, int from, CollectionReference collectionReferenceToRemove) throws ArlasException, IOException {
        throw new NotImplementedException();
    }
}
