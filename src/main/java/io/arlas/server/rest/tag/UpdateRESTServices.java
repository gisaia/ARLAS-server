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

package io.arlas.server.rest.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Search;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.*;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/write")
@Api(value = "/write")

public abstract class UpdateRESTServices {

    protected static Logger LOGGER = LoggerFactory.getLogger(UpdateRESTServices.class);

    protected static ObjectMapper mapper = new ObjectMapper();

    public UpdateServices getUpdateServices() {
        return updateServices;
    }

    protected UpdateServices updateServices;

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public UpdateRESTServices(UpdateServices updateServices) {
        this.updateServices = updateServices;
    }
}
