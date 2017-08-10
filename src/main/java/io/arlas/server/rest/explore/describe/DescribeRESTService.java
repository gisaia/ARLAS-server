package io.arlas.server.rest.explore.describe;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DescribeRESTService extends ExploreRESTServices {
    public DescribeRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("_list")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "List", produces = UTF8JSON, notes = "List the collections configured in ARLAS. ", consumes = UTF8JSON, response = CollectionReferenceDescription.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = CollectionReferenceDescription.class, responseContainer = "CollectionReferenceDescription" ),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class) })
    public Response list(

            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        List<CollectionReference> collectionReferences = exploreServices.getDaoCollectionReference().getAllCollectionReferences();
        ElasticAdmin elasticAdmin = new ElasticAdmin(this.getExploreServices().getClient());
        List<CollectionReferenceDescription> collectionReferenceDescriptionList = elasticAdmin.describeAllCollections(collectionReferences);
        return Response.ok(collectionReferenceDescriptionList).build();
    }
}
