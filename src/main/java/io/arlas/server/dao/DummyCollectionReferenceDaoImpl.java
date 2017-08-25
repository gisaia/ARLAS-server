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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;

public class DummyCollectionReferenceDaoImpl implements CollectionReferenceDao {

    public static Map<String, CollectionReferenceParameters> index = new HashMap<String, CollectionReferenceParameters>();

    @Override
    public void initCollectionDatabase() {
    }

    @Override
    public CollectionReference getCollectionReference(String ref) {
        CollectionReference collection = new CollectionReference(ref);
        collection.params = index.get(ref);
        return collection;
    }

    @Override
    public List<CollectionReference> getAllCollectionReferences() {
        List<CollectionReference> collections = new ArrayList<CollectionReference>();
        for (String ref : index.keySet()) {
            CollectionReference collection = new CollectionReference(ref);
            collection.params = index.get(ref);
            collections.add(collection);
        }
        return collections;
    }

    @Override
    public CollectionReference putCollectionReference(String ref, CollectionReferenceParameters desc) {
        index.put(ref, desc);
        return new CollectionReference(ref, desc);
    }

    @Override
    public void deleteCollectionReference(String ref) {
        if (index.remove(ref) == null)
            throw new NotFoundException("collection " + ref + " not found");
    }

    @Override
    public void checkCollectionReferenceParameters(CollectionReferenceParameters collectionRefParams) throws ArlasException {
        // Nothing to do
    }

}
