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

package io.arlas.server.ogc.wfs;

import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.dao.ElasticCollectionReferenceDaoImpl;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.ogc.wfs.services.ElasticWFSToolServiceImpl;
import io.arlas.server.core.services.ExploreServices;

public class WFSService extends WFSRESTService {


    public WFSService(ExploreServices exploreServices, ArlasServerConfiguration configuration, WFSHandler wfsHandler) throws ArlasException {
        super(wfsHandler);
        this.dao = new ElasticCollectionReferenceDaoImpl(exploreServices.getClient(), configuration.arlasindex, configuration.arlascachesize, configuration.arlascachetimeout);
        this.wfsToolService = new ElasticWFSToolServiceImpl(exploreServices);
    }
}
