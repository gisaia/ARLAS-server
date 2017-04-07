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
    @Path("{collections}/_count")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Count",
            produces=UTF8JSON,
            notes = "Count the number of elements found in the collection(s), given the filters",
            consumes=UTF8JSON
    )
    public Response count(
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
                            ":>=        |  {fieldName} is greater than or equal to  {value}  | numeric " +
                            "\n \n" +
                            ":>         |  {fieldName} is greater than {value}               | numeric " +
                            "\n \n" +
                            ":< =       |  {fieldName} is less than or equal to {value}      | numeric " +
                            "\n \n" +
                            ":<         |  {fieldName}  is less than {value}                 | numeric "
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
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("count").build();
    }
}
