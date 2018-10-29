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

package io.arlas.server.dao;

import io.arlas.server.app.INSPIREConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferences;
import io.arlas.server.utils.BoundingBox;
import org.elasticsearch.index.query.QueryBuilder;

import java.io.IOException;
import java.util.List;

/**
 * DAO interface for collection references
 */
public interface CollectionReferenceDao {
    public void initCollectionDatabase() throws ArlasException;

    public CollectionReference getCollectionReference(String ref) throws ArlasException;

    public CollectionReference getMetaCollectionReference(OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws ArlasException;

    public List<CollectionReference> getCollectionReferences(String[] includes, String[]
            excludes, int size, int from, String[] ids, String q, BoundingBox boundingBox) throws ArlasException, IOException;

    public CollectionReferences getCollectionReferencesForCSW(QueryBuilder queryBuilder,  Boolean isConfigurationQuery, String[] includes, String[] excludes, int size,
                                                        int from) throws ArlasException, IOException;
    public CollectionReferences getCollectionReferences(String[] includes, String[] excludes, int size,
                                                              int from) throws ArlasException, IOException;

    public long countCollectionReferences(String[] ids, String q, BoundingBox boundingBox) throws ArlasException, IOException;

    public List<CollectionReference> getAllCollectionReferences() throws ArlasException;

    public CollectionReference putCollectionReference(CollectionReference collectionReference)
            throws ArlasException;

    public void deleteCollectionReference(String ref) throws ArlasException;
}
