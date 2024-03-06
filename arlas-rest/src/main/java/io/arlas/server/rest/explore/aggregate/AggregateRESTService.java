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

package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.request.AggregationsRequest;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.response.AggregationResponse;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.core.utils.MapExplorer;
import io.arlas.server.core.utils.ParamsParser;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.arlas.commons.rest.utils.ServerConstants.*;

public class AggregateRESTService extends ExploreRESTServices {

    public AggregateRESTService(ExploreService exploreService) {
        super(exploreService);
    }

    @Timed
    @Path("{collection}/_aggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Aggregate",
            description = Documentation.AGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AggregationResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response aggregate(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(
                    name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            @Parameter(name = "agg",
                    description = Documentation.AGGREGATION_PARAM_AGG,
                    required = true)
            @QueryParam(value = "agg") List<String> agg,

            // --------------------------------------------------------
            // ----------------------- FILTER -----------------------
            // --------------------------------------------------------
            @Parameter(name = "f",
                    description = Documentation.FILTER_PARAM_F,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "f") List<String> f,

            @Parameter(name = "q", description = Documentation.FILTER_PARAM_Q,
                    style = ParameterStyle.FORM,
                    explode = Explode.TRUE)
            @QueryParam(value = "q") List<String> q,

            @Parameter(name = "dateformat",
                    description = Documentation.FILTER_DATE_FORMAT)
            @QueryParam(value = "dateformat") String dateformat,

            @Parameter(name = "righthand",
                    schema = @Schema(defaultValue = "true"),
                    description = Documentation.FILTER_RIGHT_HAND)
            @QueryParam(value = "righthand") Boolean righthand,


            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "flat", description = Documentation.FORM_FLAT,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        AggregationsRequest aggregationsRequest = new AggregationsRequest();
        aggregationsRequest.filter = ParamsParser.getFilter(collectionReference, f, q, dateformat, righthand);
        aggregationsRequest.aggregations = ParamsParser.getAggregations(collectionReference, agg);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, aggregationsRequest);

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = aggregationsRequest;
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        AggregationResponse aggregationResponse = getArlasAggregation(request, collectionReference, Boolean.TRUE.equals(flat));
        aggregationResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);
        return cache(Response.ok(aggregationResponse), maxagecache);
    }

    @Timed
    @Path("{collection}/_aggregate")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Aggregate",
            description = Documentation.AGGREGATION_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AggregationResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response aggregatePost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @Parameter(
                    name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- AGGREGATION -----------------------
            // --------------------------------------------------------
            AggregationsRequest aggregationsRequest,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,


            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache")
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws NotFoundException, ArlasException {
        long startArlasTime = System.nanoTime();
        CollectionReference collectionReference = exploreService.getCollectionReferenceService()
                .getCollectionReference(collection, Optional.ofNullable(organisations));

        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        AggregationsRequest aggregationsRequestHeader = new AggregationsRequest();
        aggregationsRequestHeader.filter = ParamsParser.getFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequest);
        exploreService.setValidGeoFilters(collectionReference, aggregationsRequestHeader);

        ColumnFilterUtil.assertRequestAllowed(Optional.ofNullable(columnFilter), collectionReference, aggregationsRequest);

        request.basicRequest = aggregationsRequest;
        request.headerRequest = aggregationsRequestHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collectionReference);

        AggregationResponse aggregationResponse = getArlasAggregation(request, collectionReference, (aggregationsRequest.form != null && Boolean.TRUE.equals(aggregationsRequest.form.flat)));
        aggregationResponse.totalTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startArlasTime);

        return cache(Response.ok(aggregationResponse), maxagecache);
    }

    public AggregationResponse getArlasAggregation(MixedRequest request, CollectionReference collectionReference, boolean flat) throws ArlasException {
        AggregationResponse aggregationResponse = exploreService.aggregate(request,
                collectionReference,
                false,
                ((AggregationsRequest) request.basicRequest).aggregations,
                0,
                System.nanoTime());
        return  flat ? flatten(aggregationResponse) : aggregationResponse;
    }

    private AggregationResponse flatten(AggregationResponse aggregationResponse) {
        List<AggregationResponse> elements = aggregationResponse.elements;
        if (elements != null && !elements.isEmpty()) {
            for (AggregationResponse element : elements) {
                element.flattenedElements = new HashMap<>();
                element.flattenedElements.putAll(exploreService.flat(
                        element,
                        new MapExplorer.ReduceArrayOnKey(ArlasServerConfiguration.FLATTEN_CHAR),
                        s -> (!"elements".equals(s))
                ));
                element.elements = null;
                element.metrics = null;
            }
        }
        return aggregationResponse;
    }
}
