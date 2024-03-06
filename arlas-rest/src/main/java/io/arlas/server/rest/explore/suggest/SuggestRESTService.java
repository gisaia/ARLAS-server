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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.commons.rest.utils.ServerConstants.PARTITION_FILTER;

public class SuggestRESTService extends ExploreRESTServices {

    public SuggestRESTService(ExploreService exploreService) { super(exploreService); }

    @Timed
    @Path("{collections}/_suggest")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Suggest",
            description = "Suggest the the n (n=size) most relevant terms given the filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    public Response suggest(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(
                    name = "collections",
                    description = "collections, comma separated",
                    required = true)
            @PathParam(value = "collections") String collections,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = """
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

                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q",
                    description = "A full text search")
            @QueryParam(value = "q") String q,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = "Pretty print",
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "size",
                    description = "The maximum number of entries or sub-entries to be returned. The default value is 10",
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "10"))
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @Parameter(name = "from",
                    description = "From index to start the search from. Defaults to 0.",
                    schema = @Schema(type="integer", minimum = "1", defaultValue = "0"))
            @DefaultValue("0")
            @QueryParam(value = "size") Integer from,

            // --------------------------------------------------------
            // -----------------------  SUGGEST   -----------------------
            // --------------------------------------------------------

            @Parameter(name = "field",
                    description = "Name of the field to be used for retrieving the most relevant terms",
                    schema = @Schema(defaultValue = "_all"))
            @QueryParam(value = "field") String field,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) {
        return cache(Response.ok("suggest"), maxagecache); // TODO : right response
    }
}
