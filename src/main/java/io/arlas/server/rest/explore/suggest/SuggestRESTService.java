package io.arlas.server.rest.explore.suggest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class SuggestRESTService extends ExploreRESTServices {
    public SuggestRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collections}/_suggest")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Suggest", produces = UTF8JSON, notes = "Suggest the the n (n=size) most relevant terms given the filters", consumes = UTF8JSON)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation") })
    public Response suggest(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
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
                    value="- A triplet for filtering the result. Multiple filter can be provided. " +
                            "The order does not matter. " +
                            "\n \n" +
                            "- A triplet is composed of a field name, a comparison operator and a value. " +
                            "\n \n" +
                            "  The possible values of the comparison operator are : " +
                            "\n \n" +
                            "       Operator   |                   Description                      | value type" +
                            "\n \n" +
                            "       :          |  {fieldName} equals {value}                        | numeric or strings " +
                            "\n \n" +
                            "       :gte:      |  {fieldName} is greater than or equal to  {value}  | numeric " +
                            "\n \n" +
                            "       :gt:       |  {fieldName} is greater than {value}               | numeric " +
                            "\n \n" +
                            "       :lte:      |  {fieldName} is less than or equal to {value}      | numeric " +
                            "\n \n" +
                            "       :lt:       |  {fieldName}  is less than {value}                 | numeric " +
                            "\n \n" +
                            "\n \n" +
                            "- The AND operator is applied between filters having different fieldNames. " +
                            "\n \n" +
                            "- The OR operator is applied on filters having the same fieldName. " +
                            "\n \n" +
                            "- If the fieldName starts with - then a must not filter is used" +
                            "\n \n" +
                            "- If the fieldName starts with - then a must not filter is used" +
                            "\n \n" +
                            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md "
                    ,

                    allowMultiple = true,
                    required=false)
            @QueryParam(value="f") List<String> f,

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
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="pwithin") String pwithin,

            @ApiParam(name ="gwithin", value="Any element having its geometry contained within the given geometry (WKT)",
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gwithin") String gwithin,

            @ApiParam(name ="gintersect", value="Any element having its geometry intersecting the given geometry (WKT)",
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="gintersect") String gintersect,

            @ApiParam(name ="notpwithin", value="Any element having its centroid outside the given geometry (WKT)",
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notpwithin") String notpwithin,

            @ApiParam(name ="notgwithin", value="Any element having its geometry outside the given geometry (WKT)",
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgwithin") String notgwithin,

            @ApiParam(name ="notgintersect", value="Any element having its geometry not intersecting the given geometry (WKT)",
                    allowMultiple = true,
                    required=false)
            @QueryParam(value="notgintersect") String notgintersect,

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
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    required=false)
            @DefaultValue("10")
            @QueryParam(value="size") Integer size,

            @ApiParam(name ="from", value="From index to start the search from. Defaults to 0.",
                    defaultValue = "0",
                    allowableValues = "range[1, infinity]",
                    required=false)
            @DefaultValue("0")
            @QueryParam(value="size") Integer from,

            // --------------------------------------------------------
            // -----------------------  SUGGEST   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="field", value="Name of the field to be used for retrieving the most relevant terms",
                    allowMultiple = false,
                    defaultValue = "_all",
                    required=false)
            @QueryParam(value="field") String field,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("suggest").build();// TODO : right response
    }
}
