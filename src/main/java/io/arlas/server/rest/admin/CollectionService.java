package io.arlas.server.rest.admin;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.collectionsModel.CollectionReference;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CollectionService extends AdminServices {

    @Timed
    @Path("{collection}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Get a collection reference",
            produces=UTF8JSON,
            notes = "Get a collection reference in ARLAS",
            consumes=UTF8JSON
    )
    public Response get(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();// TODO : right reponse
    }

    @Timed
    @Path("{collection}") //
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Add a collection reference",
            produces=UTF8JSON,
            notes = "Add a collection reference in ARLAS",
            consumes=UTF8JSON
    )
    public Response put(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  COLLECTION REFERENCE    -----------------------
            // --------------------------------------------------------
            @ApiParam(value="collectionReference", required=true)
            @QueryParam(value = "collectionReference") String collectionReference

    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();// TODO : right response
    }

    @Timed
    @Path("{collection}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Add a collection reference",
            produces=UTF8JSON,
            notes = "Add a collection reference in ARLAS",
            consumes=UTF8JSON
    )
    public Response delete(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();//TODO : right response
    }
}
