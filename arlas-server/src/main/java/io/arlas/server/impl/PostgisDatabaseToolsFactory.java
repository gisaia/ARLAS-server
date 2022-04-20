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

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.commons.exceptions.ArlasConfigurationException;
import io.arlas.server.core.impl.jdbi.model.RequestFactory;
import io.arlas.server.core.impl.jdbi.postgis.PostgisRequestFactory;
import io.arlas.server.core.managers.CacheManager;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

public class PostgisDatabaseToolsFactory extends JdbiDatabaseToolsFactory {
    public PostgisDatabaseToolsFactory(Environment environment, ArlasServerConfiguration configuration, CacheManager cacheManager) throws ArlasConfigurationException {
        super(environment, configuration, cacheManager);
    }

    @Override
    protected String getDriverClass() {
        return "org.postgis.DriverWrapper";
    }

    @Override
    protected String getJdbcUrl(DataSourceFactory configuration) {
        return "jdbc:postgresql_postGIS://"+configuration.getUrl();
    }

    @Override
    protected RequestFactory getRequestFactory() {
        return new PostgisRequestFactory();
    }
}
