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

package io.arlas.server.wfs.requestfilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@Provider
@PreMatching
public class InsensitiveCaseFilter implements ContainerRequestFilter {
    Logger LOGGER = LoggerFactory.getLogger(InsensitiveCaseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            toLowerCaseKeyQueryParams( requestContext );
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
    /**
     * Replace the existing query parameters with lowercase key
     * @param request
     */
    private void toLowerCaseKeyQueryParams( ContainerRequestContext request ) throws URISyntaxException {
        UriBuilder builder = request.getUriInfo().getRequestUriBuilder();
        MultivaluedMap<String, String> queries = request.getUriInfo().getQueryParameters();
        String newRequest = request.getUriInfo().getRequestUri().normalize().toString();
        for(String key : queries.keySet()){
            newRequest =newRequest.replace("&"+key+"=","&"+key.toLowerCase()+"=");
            newRequest =newRequest.replace("?"+key+"=","?"+key.toLowerCase()+"=");

        }
        URI myURI = new URI(newRequest);
        request.setRequestUri( myURI );
    }
}
