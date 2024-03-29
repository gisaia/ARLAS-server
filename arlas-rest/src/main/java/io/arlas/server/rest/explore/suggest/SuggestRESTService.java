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

package io.arlas.server.rest.explore.suggest;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.commons.rest.utils.ServerConstants.PARTITION_FILTER;

public class SuggestRESTService extends ExploreRESTServices {

    public SuggestRESTService(ExploreService exploreService) { super(exploreService); }

    @Timed
    @Path("{collections}/_suggest")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Suggest", produces = UTF8JSON, notes = "Suggest the the n (n=size) most relevant terms given the filters", consumes = UTF8JSON)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation")})
    public Response suggest(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collections",
                    value = "collections, comma separated",
                    required = true)
            @PathParam(value = "collections") String collections,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = """
                            - A triplet for filtering the result. Multiple filter can be provided. The order does not matter.\s
                            \s
                            - A triplet is composed of a field name, a comparison operator and a value.\s
                            \s
                              The possible values of the comparison operator are :\s
                            \s
                                   Operator   |                   Description                      | value type
                            \s
                                   :          |  {fieldName} equals {value}                        | numeric or strings\s
                            \s
                                   :gte:      |  {fieldName} is greater than or equal to  {value}  | numeric\s
                            \s
                                   :gt:       |  {fieldName} is greater than {value}               | numeric\s
                            \s
                                   :lte:      |  {fieldName} is less than or equal to {value}      | numeric\s
                            \s
                                   :lt:       |  {fieldName}  is less than {value}                 | numeric\s
                            \s

                            \s
                            - The AND operator is applied between filters having different fieldNames.\s
                            \s
                            - The OR operator is applied on filters having the same fieldName.\s
                            \s
                            - If the fieldName starts with - then a must not filter is used
                            \s
                            - If the fieldName starts with - then a must not filter is used
                            \s
                            For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md\s"""
                    ,

                    allowMultiple = true)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q",
                    value = "A full text search")
            @QueryParam(value = "q") String q,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size",
                    value = "The maximum number of entries or sub-entries to be returned. The default value is 10",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]")
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @ApiParam(name = "from",
                    value = "From index to start the search from. Defaults to 0.",
                    defaultValue = "0",
                    allowableValues = "range[1, infinity]")
            @DefaultValue("0")
            @QueryParam(value = "size") Integer from,

            // --------------------------------------------------------
            // -----------------------  SUGGEST   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "field",
                    value = "Name of the field to be used for retrieving the most relevant terms",
                    defaultValue = "_all")
            @QueryParam(value = "field") String field,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) {
        return cache(Response.ok("suggest"), maxagecache); // TODO : right response
    }
}
