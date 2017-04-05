package io.arlas.server.rest.explore.count;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CountRESTService extends ExploreServices {

    @Timed
    @Path("{collections}/count") // TODO : fill it
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="...", // TODO : fill it
            produces=UTF8JSON,
            notes = "...", // TODO : fill it
            consumes=UTF8JSON
    )
    public Response tile(
            @ApiParam(value="collections", required=true) // TODO : fill it
            @PathParam(value = "collections") String collections, // TODO : fill it

            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();
    }
}
