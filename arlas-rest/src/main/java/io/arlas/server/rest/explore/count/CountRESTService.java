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

package io.arlas.server.rest.explore.count;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Count;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.Hits;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreService;
import io.arlas.server.utils.ColumnFilterUtil;
import io.arlas.server.utils.ParamsParser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

public class CountRESTService extends ExploreRESTServices {

    public CountRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_count")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count", produces = UTF8JSON, notes = "Count the number of elements found in the collection(s), given the filters", consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response count(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collections",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "dateformat",
                    value = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionfilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        Count count = new Count();
        count.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat);
        exploreService.setValidGeoFilters(collectionReference, count);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, count);

        MixedRequest request = new MixedRequest();
        request.basicRequest = count;
        Count countHeader = new Count();
        countHeader.filter = ParamsParser.getFilter(partitionfilter);
        exploreService.setValidGeoFilters(collectionReference, countHeader);
        request.headerRequest = countHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        Hits hits = exploreService.count(request, collectionReference);
        return cache(Response.ok(hits), maxagecache);
    }


    @Timed
    @Path("{collection}/_count")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count", produces = UTF8JSON, notes = "Count the number of elements found in the collection(s), given the filters", consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response countPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "collection",
                    value = "collections",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "partition-filter") String partitionfilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            Count count
    ) throws NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreService.getCollectionReferenceService().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, count);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, count);

        request.basicRequest = count;
        Count countHeader = new Count();
        countHeader.filter = ParamsParser.getFilter(partitionfilter);
        exploreService.setValidGeoFilters(collectionReference, countHeader);
        request.headerRequest = countHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        Hits hits = exploreService.count(request, collectionReference);
        return Response.ok(hits).build();
    }
}
