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
package io.arlas.server.impl;

import com.codahale.metrics.health.HealthCheck;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.DatabaseToolsFactory;
import io.arlas.server.exceptions.ArlasConfigurationException;
import io.arlas.server.impl.jdbi.model.RequestFactory;
import io.arlas.server.impl.jdbi.service.JdbiCollectionReferenceService;
import io.arlas.server.impl.jdbi.service.JdbiExploreService;
import io.arlas.server.managers.CacheManager;
import io.arlas.server.ogc.common.dao.JdbiOGCCollectionReferenceDao;
import io.arlas.server.ogc.common.dao.OGCCollectionReferenceDao;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.wfs.services.JdbiWFSToolService;
import io.arlas.server.ogc.wfs.services.WFSToolService;
import io.arlas.server.services.CollectionReferenceService;
import io.arlas.server.services.ExploreService;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;

import java.util.HashMap;
import java.util.Map;

public abstract class JdbiDatabaseToolsFactory extends DatabaseToolsFactory {
    private final Jdbi jdbi;
    private final CacheManager cacheManager;
    private final ExploreService exploreService;
    private final CollectionReferenceService collectionReferenceService;
    private OGCCollectionReferenceDao ogcDao;
    private WFSToolService wfsService;

    public JdbiDatabaseToolsFactory(Environment environment, ArlasServerConfiguration configuration, CacheManager cacheManager) throws ArlasConfigurationException {
        super(configuration);
        if (configuration.database == null) {
            throw new ArlasConfigurationException("Database configuration missing in config file.");
        }
        configuration.database.setDriverClass(getDriverClass());
        configuration.database.setUrl(getJdbcUrl(configuration.database));
        this.jdbi = new JdbiFactory().build(environment, configuration.database, "jdbi");
        this.jdbi.installPlugin(new Jackson2Plugin());
        this.jdbi.installPlugin(new PostgresPlugin());
        this.cacheManager = cacheManager;
        this.collectionReferenceService = new JdbiCollectionReferenceService(jdbi, configuration.arlasIndex, cacheManager);
        this.exploreService = new JdbiExploreService(jdbi, collectionReferenceService, configuration.arlasBaseUri, configuration.arlasRestCacheTimeout, getRequestFactory());
        if (configuration.arlasServiceCSWEnabled) {
            this.ogcDao = new JdbiOGCCollectionReferenceDao(jdbi, collectionReferenceService, configuration.arlasIndex, Service.CSW);
        }
        if (configuration.arlasServiceWFSEnabled) {
            this.wfsService = new JdbiWFSToolService((JdbiExploreService) exploreService);
        }
    }

    @Override
    public ExploreService getExploreService() {
        return this.exploreService;
    }

    @Override
    public CollectionReferenceService getCollectionReferenceService() {
        return this.collectionReferenceService;
    }

    @Override
    public OGCCollectionReferenceDao getOGCCollectionReferenceDao() {
        return ogcDao;
    }

    @Override
    public WFSToolService getWFSToolService() {
        return wfsService;
    }

    @Override
    public Map<String, HealthCheck> getHealthChecks() {
        Map<String, HealthCheck> ret = new HashMap<>();
        // dropwizard-jdbi3 already provides a healthcheck
        return ret;
    }

    protected abstract String getDriverClass();

    protected abstract String getJdbcUrl(DataSourceFactory configuration);

    protected abstract RequestFactory getRequestFactory();
}
