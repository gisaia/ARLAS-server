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

package io.arlas.server.ogc.wfs.services;

import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import net.opengis.wfs._2.ValueCollectionType;

import java.io.IOException;
import java.util.List;

public interface WFSToolService {

    public CollectionReferenceDescription getCollectionReferenceDescription(CollectionReference collectionReference) throws IOException;

    public Object getFeature(String id, String bbox, String constraint, String resourceid, String storedquery_id,
                             String partitionFilter, CollectionReference collectionReference, String[] excludes) throws ArlasException, IOException;
    public List<Object> getFeatures(String id, String bbox, String constraint, String resourceid,
                                    String partitionFilter, CollectionReference collectionReference, String[] excludes, Integer startindex, Integer count) throws ArlasException, IOException;

    public ValueCollectionType getPropertyValue (String id, String bbox, String constraint, String resourceid, String storedquery_id,
    String partitionFilter, CollectionReference collectionReference, String include, String[] excludes, Integer startindex, Integer count) throws ArlasException, IOException;
}
