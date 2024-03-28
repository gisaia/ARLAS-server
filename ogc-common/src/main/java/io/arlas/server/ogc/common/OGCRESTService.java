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

package io.arlas.server.ogc.common;

import io.arlas.server.core.services.ArlasRESTServices;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;


public class OGCRESTService extends ArlasRESTServices {
    protected CollectionReferenceService collectionReferenceService = null;
    protected OGCCollectionReferenceDao ogcDao = null;
    private static final String META_COLLECTION_NAME = "metacollection";

    protected String getMetacollectionName() {
        return META_COLLECTION_NAME;
    }

}
