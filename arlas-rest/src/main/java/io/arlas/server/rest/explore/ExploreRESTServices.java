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

import io.arlas.server.services.ExploreService;
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
                version = "14.0.0-beta.4"),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS })

public abstract class ExploreRESTServices {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExploreRESTServices.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    protected ExploreService exploreService;

    public ExploreRESTServices(ExploreService exploreService) {
        this.exploreService = exploreService;
    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        return exploreService.getResponseCacheManager().cache(response, maxagecache);
    }

    public String getExplorePathUri() {
        return "explore/";
    }
}
