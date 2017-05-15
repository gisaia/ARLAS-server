package io.arlas.server.rest.explore.count;


import com.codahale.metrics.annotation.Timed;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.Count;
import io.arlas.server.model.response.ArlasError;
import io.arlas.server.model.response.ArlasHits;
import io.arlas.server.rest.explore.Documentation;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.ParamsParser;
import io.dropwizard.jersey.params.LongParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.elasticsearch.search.SearchHits;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CountRESTService extends ExploreRESTServices {

    public CountRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_count")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count", produces = UTF8JSON, notes = "Count the number of elements found in the collection(s), given the filters", consumes = UTF8JSON, response = ArlasHits.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = ArlasHits.class, responseContainer = "ArlasHits" ),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = ArlasError.class), @ApiResponse(code = 400, message = "Bad request.", response = ArlasError.class) })
    public Response count(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value="collections",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="f",
                    value= Documentation.FILTER_PARAM_F,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="f") List<String> f,

            @ApiParam(name ="q", value=Documentation.FILTER_PARAM_Q,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="q") String q,

            @ApiParam(name ="before", value=Documentation.FILTER_PARAM_BEFORE,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="before") LongParam before,

            @ApiParam(name ="after", value=Documentation.FILTER_PARAM_AFTER,
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="after") LongParam after,

            @ApiParam(name ="pwithin", value=Documentation.FILTER_PARAM_PWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="pwithin") String pwithin,

            @ApiParam(name ="gwithin", value=Documentation.FILTER_PARAM_GWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gwithin") String gwithin,

            @ApiParam(name ="gintersect", value=Documentation.FILTER_PARAM_GINTERSECT,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gintersect") String gintersect,

            @ApiParam(name ="notpwithin", value=Documentation.FILTER_PARAM_NOTPWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notpwithin") String notpwithin,

            @ApiParam(name ="notgwithin", value=Documentation.FILTER_PARAM_NOTGWITHIN,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgwithin") String notgwithin,

            @ApiParam(name ="notgintersect", value=Documentation.FILTER_PARAM_NOTGINTERSECT,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgintersect") String notgintersect,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty,

            @ApiParam(name ="human", value=Documentation.FORM_HUMAN,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="human") Boolean human,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        fluidSearch.setCollectionReference(collectionReference);

        Count count = new Count();
        count.filter = ParamsParser.getFilter(f,q,before,after,pwithin,gwithin,gintersect,notpwithin,notgwithin,notgintersect);

        ArlasHits arlasHits = getArlasHits(collectionReference, count);
        return Response.ok(arlasHits).build();
    }


    @Timed
    @Path("{collection}/_count")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Count", produces = UTF8JSON, notes = "Count the number of elements found in the collection(s), given the filters", consumes = UTF8JSON, response = ArlasHits.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = ArlasHits.class, responseContainer = "ArlasHits" ),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = ArlasError.class), @ApiResponse(code = 400, message = "Bad request.", response = ArlasError.class) })
    public Response countPost(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value="collections",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // -----------------------  SEARCH  -----------------------
            // --------------------------------------------------------
            Count count
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ArlasHits arlasHits = getArlasHits(collectionReference, count);
        return Response.ok(arlasHits).build();
    }

    protected ArlasHits getArlasHits(CollectionReference collectionReference, Count count) throws ArlasException, IOException {
        SearchHits searchHits = this.getExploreServices().count(count,collectionReference);
        ArlasHits arlasHits = new ArlasHits();
        arlasHits.totalnb = searchHits.totalHits();
        arlasHits.nbhits = searchHits.getHits().length;
        return arlasHits;
    }
}
