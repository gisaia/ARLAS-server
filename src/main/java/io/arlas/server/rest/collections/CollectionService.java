package io.arlas.server.rest.collections;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.CollectionReferenceParameters;
import io.arlas.server.rest.ResponseFormatter;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public abstract class CollectionService extends CollectionRESTServices {

    protected CollectionReferenceDao dao = null;
    
    @Timed
    @Path("/")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Get all collection references",
            produces=UTF8JSON,
            notes = "Get all collection references in ARLAS",
            consumes=UTF8JSON,
            response = CollectionReference.class

    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
	    @ApiResponse(code = 404, message = "Collections not found."),
	    @ApiResponse(code = 500, message = "Arlas Server Error.")})

    public Response getAll() throws InterruptedException, ExecutionException, IOException, ArlasException {	
	List<CollectionReference> collections = dao.getAllCollectionReferences();
	
	ObjectMapper mapper = new ObjectMapper();
	ArrayNode json = mapper.createArrayNode();
	for(CollectionReference collection : collections)
	    json.add(collection.toJson());
	return ResponseFormatter.getResultResponse(json.toString());
    }

    @Timed
    @Path("{collection}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Get a collection reference",
            produces=UTF8JSON,
            notes = "Get a collection reference in ARLAS",
            consumes=UTF8JSON,
            response = CollectionReference.class

    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
	    @ApiResponse(code = 404, message = "Collection not found."),
	    @ApiResponse(code = 500, message = "Arlas Server Error.")})

    public Response get(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
	CollectionReference cr = dao.getCollectionReference(collection);
	return ResponseFormatter.getResultResponse(cr.toJsonString());
    }

    @Timed
    @Path("{collection}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Add a collection reference",
            produces=UTF8JSON,
            notes = "Add a collection reference in ARLAS",
            consumes=UTF8JSON,
            response = CollectionReference.class
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
	    @ApiResponse(code = 400, message = "JSON parameter malformed."),
	    @ApiResponse(code = 500, message = "Arlas Server Error.")})
    public Response put(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,
            @ApiParam(name = "collectionParams",
                        value="collectionParams",
                        required=true)
            @NotNull @Valid CollectionReferenceParameters collectionReferenceParameters

    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
	CollectionReference cr = dao.putCollectionReference(collection, collectionReferenceParameters);
	return ResponseFormatter.getResultResponse(cr.toJsonString());
    }

    @Timed
    @Path("{collection}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Delete a collection reference",
            produces=UTF8JSON,
            notes = "Delete a collection reference in ARLAS",
            consumes=UTF8JSON
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation"),
	    @ApiResponse(code = 404, message = "Collection not found."),
	    @ApiResponse(code = 500, message = "Arlas Server Error.")})

    public Response delete(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection
    ) throws InterruptedException, ExecutionException, IOException, ArlasException {
	dao.deleteCollectionReference(collection);
	return ResponseFormatter.getSuccessResponse("Collection " + collection + " deleted.");
    }
}
