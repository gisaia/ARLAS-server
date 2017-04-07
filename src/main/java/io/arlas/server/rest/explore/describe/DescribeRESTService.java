package io.arlas.server.rest.explore.describe;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DescribeRESTService extends ExploreServices {
    @Timed
    @Path("_describe")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Describe",
            produces=UTF8JSON,
            notes = "List  the collections configured in ARLAS. ",
            consumes=UTF8JSON
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation")})
    public Response describe(

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("describe").build();//TODO : right response
    }
}

