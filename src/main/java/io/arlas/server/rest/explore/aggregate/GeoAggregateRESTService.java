package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.rest.explore.enumerations.FormatValues;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.geojson.FeatureCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GeoAggregateRESTService extends ExploreRESTServices {

    public GeoAggregateRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collections}/_geoaggregate")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value="Aggregate",
            produces=UTF8JSON,
            notes = "Aggregate the elements in the collection(s), given the filters and the aggregation parameters",
            consumes=UTF8JSON,
            response = FeatureCollection.class

    )
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation")})
    public Response geoaggregate(
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
            // -----------------------  AGGREGATION  -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="agg",
                    value="- The agg parameter should be given in the following formats:  " +
                            "\n \n" +
                            "       {type}:{field}:interval-{interval}:format-{format}:collect_field-{collect_field}:collect_fct-{function}:order-{order}:on-{on} " +
                            "\n \n" +
                            "Where the {type}:{field} part is mandatory AND interval, format, collect_field, collect_fct," +
                            " order AND on are optional sub-parameters. " +
                            "\n \n" +
                            "- {type} possible value is : geohash. " +
                            "\n \n" +
                            "- {interval} must be a numeric value. "+
                            "\n \n" +
                            "- format-{format} is to be specified when {type} = datehistogram. It's the date format for key aggregation. " +
                            "\n \n" +
                            "- {collect_fct} is the aggregation function to apply to collections on the specified {collect_field}. " +
                            "\n \n" +
                            "  {collect_fct} possible values are : "+
                            "\n \n" +
                            "       avg,cardinality,max,min,sum" +
                            "\n \n" +
                            "- {order} is set to sort the aggregation result on the field name or on the result itself. " +
                            "It's values are 'asc' or 'desc'. " +
                            "\n \n" +
                            "- {on} is set to specify whether the {order} is on the field name or the result. It's values are 'field' or 'result'. " +
                            "\n \n" +
                            "agg parameter in this case is not multiple. "+
                            "\n \n" +
                            "For more details, check https://gitlab.com/GISAIA.ARLAS/ARLAS-server/blob/master/doc/api/API-definition.md "

                    ,
                    allowMultiple = false,
                    required=true)
            @QueryParam(value="agg") List<String> agg,

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
            // -----------------------  SORT   -----------------------
            // --------------------------------------------------------

            @ApiParam(name ="sort",
                    value="Sort the result on a given field, ascending or descending :  '{fieldName}:(ASC, DESC)' . " +
                            "\n \n"+
                            "The parameter can be provided several times. The order matters. " +
                            "\n \n"+
                            "For aggregation, provide the 'agg' keyword as the {fieldName}.",
                    allowMultiple = true,
                    example = "city:DESC",
                    required=false)
            @QueryParam(value="sort") List<String> sort,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value="max-age-cache", required=false)
            @QueryParam(value="max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException {
        return Response.ok("aggregate").build();//TODO : right response
    }
}
