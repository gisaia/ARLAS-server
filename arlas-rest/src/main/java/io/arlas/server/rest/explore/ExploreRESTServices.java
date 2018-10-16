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

package io.arlas.server.rest.explore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.arlas.server.services.ExploreServices;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/explore")
@Api(value = "/explore")
@SwaggerDefinition(
        info = @Info(contact = @Contact(email = "contact@gisaia.com", name = "Gisaia", url = "http://www.gisaia.com/"),
                title = "ARLAS Exploration API",
                description = "Explore the content of ARLAS collections",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html"),
                version = "8.0.0"))

public abstract class ExploreRESTServices {

    protected static Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);

    protected static ObjectMapper mapper = new ObjectMapper();

    public ExploreServices getExploreServices() {
        return exploreServices;
    }

    protected ExploreServices exploreServices;

    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    public ExploreRESTServices(ExploreServices exploreServices) {
        this.exploreServices = exploreServices;
    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        return exploreServices.getResponseCacheManager().cache(response, maxagecache);
    }
}
