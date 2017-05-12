package io.arlas.server.rest.explore.describe;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class DescribeRESTService extends ExploreRESTServices {
    public DescribeRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("_list")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "List", produces = UTF8JSON, notes = "List the collections configured in ARLAS. ", consumes = UTF8JSON)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation") })
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
