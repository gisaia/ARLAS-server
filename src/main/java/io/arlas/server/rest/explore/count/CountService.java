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

public class CountService extends ExploreServices {

    @Timed
    @Path("/tile/{index}/{z}/{x}/{y}.png") // TODO : fill it
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Access a tile", // TODO : fill it
            produces=UTF8JSON,
            notes = "Access a tile made of sub-tiles found in elasticsearch.", // TODO : fill it
            consumes=UTF8JSON
    )
    public Response tile(
            @ApiParam(value="index", required=true) // TODO : fill it
            @PathParam(value = "index") String index, // TODO : fill it

            @ApiParam(value="z", required=true) // TODO : fill it
            @PathParam(value = "z") Integer z, // TODO : fill it

            @ApiParam(value="x", required=true) // TODO : fill it
            @PathParam(value = "x") Integer x, // TODO : fill it

            @ApiParam(value="y", required=true) // TODO : fill it
            @PathParam(value = "y") Integer y, // TODO : fill it


            @ApiParam(value="filters", required=false) // TODO : fill it
            @QueryParam(value="filters") List<String> filters, // TODO : fill it

            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("hello").build();
    }
}
