package io.arlas.server.rest.explore.search;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.rest.explore.enumerations.FormatValues;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.geojson.FeatureCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SearchRESTService extends ExploreServices {

    @Timed
    @Path("{collections}/search")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Search",
            produces=UTF8JSON,
            notes = "Search and return the elements found in the collection(s), given the filters",
            consumes=UTF8JSON,
            response = FeatureCollection.class
    )
    public Response search(
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
                            "The order does not matter. A triplet is composed of a field name, a comparison operator and a value. " +
                            "The AND operator is applied between filters having different fieldNames. " +
                            "The OR operator is applied on filters having the same fieldName. " +
                            "If the fieldName starts with - then a must not filter is used",
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
            // -----------------------  FORMAT   -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="format", value="JSON or GeoJSON format",
                    allowMultiple = false,
                    defaultValue = "json",
                    allowableValues = FormatValues.allowableFormatValues,
                    required=false)
            @QueryParam(value="format") String format,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="include", value="List the name patterns of the field to be included in the result. Seperate patterns with a comma.",
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "*",
                    required=false)
            @QueryParam(value="include") String include,

            @ApiParam(name ="exclude", value="List the name patterns of the field to be excluded in the result. Seperate patterns with a comma.",
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "city,state",
                    required=false)
            @QueryParam(value="exclude") String exclude,

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
            // -----------------------  SORT   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="sort",
                    value="Sort the result on a given field, ascending or descending (ASC, DESC). " +
                            "The parameter can be provided several times. The order matters. " +
                            "For aggregation, provide the 'agg' keyword as the {fieldName}.",
                    allowMultiple = true,
                    defaultValue = "10",
                    example = "city:DESC",
                    required=false)
            @QueryParam(value="sort") String sort,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("search").build();
    }
}
