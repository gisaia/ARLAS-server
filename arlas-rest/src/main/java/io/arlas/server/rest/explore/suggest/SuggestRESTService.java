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
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SuggestRESTService extends ExploreRESTServices {
    public SuggestRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

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
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collections") String collections,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = "- A triplet for filtering the result. Multiple filter can be provided. " +
                            "The order does not matter. " +
                            "\n \n" +
                            "- A triplet is composed of a field name, a comparison operator and a value. " +
                            "\n \n" +
                            "  The possible values of the comparison operator are : " +
                            "\n \n" +
                            "       Operator   |                   Description                      | value type" +
                            "\n \n" +
                            "       :          |  {fieldName} equals {value}                        | numeric or strings " +
                            "\n \n" +
                            "       :gte:      |  {fieldName} is greater than or equal to  {value}  | numeric " +
                            "\n \n" +
                            "       :gt:       |  {fieldName} is greater than {value}               | numeric " +
                            "\n \n" +
                            "       :lte:      |  {fieldName} is less than or equal to {value}      | numeric " +
                            "\n \n" +
                            "       :lt:       |  {fieldName}  is less than {value}                 | numeric " +
                            "\n \n" +
                            "\n \n" +
                            "- The AND operator is applied between filters having different fieldNames. " +
                            "\n \n" +
                            "- The OR operator is applied on filters having the same fieldName. " +
                            "\n \n" +
                            "- If the fieldName starts with - then a must not filter is used" +
                            "\n \n" +
                            "- If the fieldName starts with - then a must not filter is used" +
                            "\n \n" +
                            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md "
                    ,

                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = "A full text search",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "q") String q,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = "The maximum number of entries or sub-entries to be returned. The default value is 10",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") Integer size,

            @ApiParam(name = "from", value = "From index to start the search from. Defaults to 0.",
                    defaultValue = "0",
                    allowableValues = "range[1, infinity]",
                    required = false)
            @DefaultValue("0")
            @QueryParam(value = "size") Integer from,

            // --------------------------------------------------------
            // -----------------------  SUGGEST   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "field", value = "Name of the field to be used for retrieving the most relevant terms",
                    allowMultiple = false,
                    defaultValue = "_all",
                    required = false)
            @QueryParam(value = "field") String field,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return cache(Response.ok("suggest"), maxagecache);// TODO : right response
    }
}
