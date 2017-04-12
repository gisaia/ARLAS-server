package io.arlas.server.rest.explore.suggest;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SuggestRESTService extends ExploreRESTServices {
    public SuggestRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collections}/_suggest")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Suggest",
            produces=UTF8JSON,
            notes = "Suggest the the n (n=size) most relevant terms given the filters",
            consumes=UTF8JSON
    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation")})
    public Response suggest(
            // --------------------------------------------------------
            // -----------------------  PATH    -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collections",
                    value="collections, comma separated",
                    allowMultiple = false,
                    required=true)
            @PathParam(value = "collections") String collections,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="f",
                    value="A triplet for filtering the result. Multiple filter can be provided. " +
                            "The order does not matter. " +
                            "\n \n" +
                            "A triplet is composed of a field name, a comparison operator and a value. " +
                            "\n \n" +
                            "The AND operator is applied between filters having different fieldNames. " +
                            "\n \n" +
                            "The OR operator is applied on filters having the same fieldName. " +
                            "\n \n" +
                            "If the fieldName starts with - then a must not filter is used" +
                            "\n \n" +
                            "Operator   |                   Description                      | value type" +
                            "\n \n" +
                            ":          |  {fieldName} equals {value}                        | numeric or strings " +
                            "\n \n" +
                            ":gte:      |  {fieldName} is greater than or equal to  {value}  | numeric " +
                            "\n \n" +
                            ":gt:       |  {fieldName} is greater than {value}               | numeric " +
                            "\n \n" +
                            ":lte:      |  {fieldName} is less than or equal to {value}      | numeric " +
                            "\n \n" +
                            ":lt:       |  {fieldName}  is less than {value}                 | numeric "
                    ,
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="f") String f,

            @ApiParam(name ="q", value="A full text search",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="q") String q,

            @ApiParam(name ="before", value="Any element having its point in time reference before the given timestamp",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="before") Long before,

            @ApiParam(name ="after", value="Any element having its point in time reference after the given timestamp",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="after") Long after,

            @ApiParam(name ="pwithin", value="Any element having its centroid contained within the given geometry (WKT)",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="pwithin") String pwithin,

            @ApiParam(name ="gwithin", value="Any element having its geometry contained within the given geometry (WKT)",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="gwithin") String gwithin,

            @ApiParam(name ="gintersect", value="Any element having its geometry intersecting the given geometry (WKT)",
                    allowMultiple = false,
                    required=false)
            @QueryParam(value="gintersect") String gintersect,

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
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="size", value="The maximum number of entries or sub-entries to be returned. The default value is 10",
                    allowMultiple = true,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    required=false)
            @QueryParam(value="size") Integer size,

            // --------------------------------------------------------
            // -----------------------  SUGGEST   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="field", value="Name of the field to be used for retrieving the most relevant terms",
                    allowMultiple = false,
                    defaultValue = "_all",
                    example = "recommended",
                    required=false)
            @QueryParam(value="field") String field,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("suggest").build();//TODO : right response
    }
}
