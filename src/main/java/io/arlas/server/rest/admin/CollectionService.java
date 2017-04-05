package io.arlas.server.rest.admin;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CollectionService extends AdminServices {

    @Timed
    @Path("{collection}") // TODO : fill it
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="...", // TODO : fill it
            produces=UTF8JSON,
            notes = "...", // TODO : fill it
            consumes=UTF8JSON
    )
    public Response get(
            @ApiParam(value="collection", required=true) // TODO : fill it
            @PathParam(value = "collection") String collections // TODO : fill it
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();
    }

    @Timed
    @Path("{collection}") // TODO : fill it
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="...", // TODO : fill it
            produces=UTF8JSON,
            notes = "...", // TODO : fill it
            consumes=UTF8JSON
    )
    public Response put(
            @ApiParam(value="collection", required=true) // TODO : fill it
            @PathParam(value = "collection") String collections // TODO : fill it
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();
    }

    @Timed
    @Path("{collection}") // TODO : fill it
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="...", // TODO : fill it
            produces=UTF8JSON,
            notes = "...", // TODO : fill it
            consumes=UTF8JSON
    )
    public Response delete(
            @ApiParam(value="collection", required=true) // TODO : fill it
            @PathParam(value = "collection") String collections // TODO : fill it
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();
    }
}
