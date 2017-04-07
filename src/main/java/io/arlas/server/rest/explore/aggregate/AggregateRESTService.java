package io.arlas.server.rest.explore.aggregate;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.rest.explore.enumerations.AggregationType;
import io.arlas.server.rest.explore.enumerations.FormatValues;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.geojson.FeatureCollection;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AggregateRESTService extends ExploreServices {

    @Timed
    @Path("{collections}/_aggregate")
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
    public Response aggregate(
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
                    value="Type of aggregation",
                    allowMultiple = false,
                    allowableValues = AggregationType.allowableAggregationTypes,
                    example = "datehistogram",
                    required=true)
            @QueryParam(value="agg") String agg,

            @ApiParam(name ="agg_field", value="Aggregates on the {field}.",
                    allowMultiple = true,
                    example = "date",
                    required=false)
            @QueryParam(value="agg_field") String agg_field,

            @ApiParam(name ="agg_interval",
                    value="Size of the intervals. " +
                            "\n \n" +
                            "If aggregation type is 'datehistogram' : Size of a time interval with the given unit " +
                            "(no space between number and unit) " +
                            "{size}(year,quarter,month,week,day,hour,minute,second) " +
                            "\n \n" +
                            "If aggregation type is 'geohash' : The geohash length range is from 1 to 12: " +
                            "lower the length, greater is the surface of aggregation. " +
                            "\n \n" +
                            "If aggregation type is 'numeric' : The interval size of the numeric aggregation",
                    allowMultiple = true,
                    example = "10day",
                    required=true)
            @QueryParam(value="agg_interval") String agg_interval,

            @ApiParam(name ="agg_format", value="Date format for key aggregation.",
                    allowMultiple = true,
                    example = "yyyyMMdd",
                    required=false)
            @QueryParam(value="agg_format") String agg_format,

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
            // -----------------------  FORMAT   -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="format", value="JSON or GeoJSON format",
                    allowMultiple = false,
                    defaultValue = "json",
                    allowableValues = FormatValues.allowableFormatValues,
                    required=false)
            @QueryParam(value="format") String format,

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
                    value="Sort the result on a given field, ascending or descending :  '{fieldName}:(ASC, DESC)' . " +
                            "\n \n"+
                            "The parameter can be provided several times. The order matters. " +
                            "\n \n"+
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
        return Response.ok("aggregate").build();//TODO : right response
    }
}
