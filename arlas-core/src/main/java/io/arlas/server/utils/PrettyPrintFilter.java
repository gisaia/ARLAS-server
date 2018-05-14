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

package io.arlas.server.utils;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

public class PrettyPrintFilter implements javax.ws.rs.container.ContainerResponseFilter {
    private static final String QUERY_PARAM_PRETTY = "pretty";
    private static final String QUERY_PARAM_TRUE = "true";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();

        for (Entry<String, List<String>> queryEntry : queryParams.entrySet()) {
            if (queryEntry.getKey().equalsIgnoreCase(QUERY_PARAM_PRETTY)) {
                String valueOfLastQueryParamIndex = queryEntry.getValue().get(queryEntry.getValue().size() - 1);
                if (valueOfLastQueryParamIndex.toLowerCase().equals(QUERY_PARAM_TRUE)) {
                    ObjectWriterInjector.set(new PrettyWriterModifier(true));
                    break;
                }
            }
        }
    }
}
