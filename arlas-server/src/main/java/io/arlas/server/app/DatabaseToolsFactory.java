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

package io.arlas.server.app;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.server.services.CollectionReferenceService;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;
import io.arlas.server.ogc.wfs.services.WFSToolService;
import io.arlas.server.services.ExploreService;

import java.util.Map;

public abstract class DatabaseToolsFactory {
    protected ArlasServerConfiguration configuration;

    public DatabaseToolsFactory(ArlasServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public abstract ExploreService getExploreService();

    public abstract CollectionReferenceService getCollectionReferenceService();

    public abstract OGCCollectionReferenceDao getOGCCollectionReferenceDao();

    public abstract WFSToolService getWFSToolService();

    public abstract Map<String, HealthCheck> getHealthChecks();
}
