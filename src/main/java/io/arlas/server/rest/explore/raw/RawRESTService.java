package io.arlas.server.rest.explore.raw;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.arlas.server.core.ElasticDocument;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.ArlasError;
import io.arlas.server.model.response.ArlasHit;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class RawRESTService extends ExploreRESTServices {
    public RawRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/{identifier}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Get an Arlas document", produces = UTF8JSON, notes = "Returns a raw indexed document.", consumes = UTF8JSON, response = ArlasHit.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = ArlasHit.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = ArlasError.class),
            @ApiResponse(code = 400, message = "Bad request.", response = ArlasError.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = ArlasError.class) })
    public Response getArlasHit(
         // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value="collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,
            
            @ApiParam(
                    name = "identifier",
                    value="identifier",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "identifier") String identifier,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value="Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty,

            @ApiParam(name ="human", value="Human readable print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="human") Boolean human,


            // --------------------------------------------------------
            // ----------------------- EXTRA -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException("Collection " + collection + " not found.");
        }

        ElasticDocument elasticDoc = new ElasticDocument(this.getExploreServices().getClient());
        Map<String,Object>  source = elasticDoc.getSource(collectionReference, identifier);
        if(source == null || source.isEmpty()) {
            throw new NotFoundException("Document " + identifier + " not found.");
        }
        
        ArlasHit hit = new ArlasHit(collectionReference, source);
        return Response.ok(hit).build();
    }
}
