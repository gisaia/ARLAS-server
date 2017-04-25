package io.arlas.server.rest.explore.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.geojson.Point;

import com.codahale.metrics.annotation.Timed;

import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.model.ArlasHit;
import io.arlas.server.model.ArlasHits;
import io.arlas.server.model.ArlasMD;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.CheckParams;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public class SearchRESTService extends ExploreRESTServices {

    public SearchRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_search")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Search", produces = UTF8JSON, notes = "Search and return the elements found in the collection, given the filters", consumes = UTF8JSON)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = ArlasHits.class) })
    public Response search(
            // --------------------------------------------------------
            // ----------------------- PATH -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "f",
                    value = "- A triplet for filtering the result. Multiple filter can be provided. " +
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
                    required = false)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = "A full text search",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "q") String q,

            @ApiParam(name = "before", value = "Any element having its point in time reference before the given timestamp",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") LongParam before,

            @ApiParam(name = "after", value = "Any element having its point in time reference after the given timestamp",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "after") LongParam after,

            @ApiParam(name = "pwithin", value = "Any element having its centroid contained within the given BBOX (top,left,bottom,right)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "pwithin") String pwithin,

            @ApiParam(name = "gwithin", value = "Any element having its geometry contained within the given geometry (WKT)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gwithin") String gwithin,

            @ApiParam(name = "gintersect", value = "Any element having its geometry intersecting the given geometry (WKT)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gintersect") String gintersect,

            @ApiParam(name = "notpwithin", value = "Any element having its centroid outside the given BBOX (top,left,bottom,right)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notpwithin") String notpwithin,

            @ApiParam(name = "notgwithin", value = "Any element having its geometry outside the given geometry (WKT)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgwithin") String notgwithin,

            @ApiParam(name = "notgintersect", value = "Any element having its geometry not intersecting the given geometry (WKT)",
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgintersect") String notgintersect,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = "Pretty print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @DefaultValue("false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "human", value = "Human readable print",
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @DefaultValue("false")
            @QueryParam(value = "human") Boolean human,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "include", value = "List the name patterns of the field to be included in the result. Seperate patterns with a comma.",
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "*",
                    required = false)
            @QueryParam(value = "include") String include,

            @ApiParam(name = "exclude", value = "List the name patterns of the field to be excluded in the result. Seperate patterns with a comma.",
                    allowMultiple = true,
                    defaultValue = "*",
                    example = "city,state",
                    required = false)
            @QueryParam(value = "exclude") String exclude,

            // --------------------------------------------------------
            // -----------------------  SIZE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = "The maximum number of entries or sub-entries to be returned. The default value is 10",
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from", value = "From index to start the search from. Defaults to 0.",
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    required = false)
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            // --------------------------------------------------------
            // -----------------------  SORT   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "sort",
                    value = "- Sort the result on the given fields ascending or descending. " +
                            "\n \n" +
                            "- Fields can be provided several times by separating them with a comma. The order matters. " +
                            "\n \n" +
                            "- For a descending sort, precede the field with '-'. The sort will be ascending otherwise." +
                            "\n \n" +
                            "- For aggregation, provide the `agg` keyword as the `{field}`.",
                    allowMultiple = false,
                    example = "-country,city",
                    required = false)
            @QueryParam(value = "sort") String sort,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        FluidSearch fluidSearch = new FluidSearch(exploreServices.getClient());
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        fluidSearch.setCollectionReference(collectionReference);

        if (f != null && !f.isEmpty()) {
            fluidSearch = fluidSearch.filter(f);
        }
        if (q != null) {
            fluidSearch = fluidSearch.filterQ(q);
        }
        if(before != null || after != null) {
            if((before!=null && before.get()<0) || (after != null && after.get()<0)
                    || (before != null && after != null && before.get() < after.get()))
                throw new InvalidParameterException(FluidSearch.INVALID_BEFORE_AFTER);
        }
        if (after != null) {
            fluidSearch = fluidSearch.filterAfter(after.get());
        }
        if (before != null) {
            fluidSearch = fluidSearch.filterBefore(before.get());
        }
        if (pwithin != null && !pwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(pwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (gwithin != null && !gwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterGWithin(gwithin);
        }
        if (gintersect != null && !gintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterGIntersect(gintersect);
        }
        if (notpwithin != null && !notpwithin.isEmpty()) {
            double[] tlbr = CheckParams.toDoubles(notpwithin);
            if (tlbr.length == 4 && tlbr[0]>tlbr[2] && tlbr[2]<tlbr[3]) {
                fluidSearch = fluidSearch.filterNotPWithin(tlbr[0], tlbr[1], tlbr[2], tlbr[3]);
            } else {
                throw new InvalidParameterException(FluidSearch.INVALID_BBOX);
            }
        }
        if (notgwithin != null && !notgwithin.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGWithin(notgwithin);
        }
        if (notgintersect != null && !notgintersect.isEmpty()) {
            fluidSearch = fluidSearch.filterNotGIntersect(notgintersect);
        }
        if (include != null) {
            fluidSearch = fluidSearch.include(include);
        }
        if (exclude != null) {
            fluidSearch = fluidSearch.exclude(exclude);
        }
        if (size != null && size.get() > 0) {
            if (from != null) {
                if(from.get() < 0) {
                    throw new InvalidParameterException(FluidSearch.INVALID_FROM);
                } else {
                    fluidSearch = fluidSearch.filterSize(size.get(), from.get());
                }
            } else {
                fluidSearch = fluidSearch.filterSize(size.get(), 0);
            }
        } else {
            throw new InvalidParameterException(FluidSearch.INVALID_SIZE);
        }
        if (sort != null) {
            fluidSearch = fluidSearch.sort(sort);
        }

        SearchHits searchHits = fluidSearch.exec().getHits();

        ArlasHits arlasHits = new ArlasHits();
        arlasHits.totalnb = searchHits.totalHits();
        arlasHits.nbhits = searchHits.getHits().length;
        arlasHits.hits = new ArrayList<>((int) arlasHits.nbhits);
        for (SearchHit hit : searchHits.getHits()) {
            ArlasHit arlasHit = new ArlasHit();
            arlasHit.data = hit.getSource();
            arlasHit.md = new ArlasMD();
            Map<String, Object> hitsSources = hit.getSource();
            if (collectionReference.params.idPath != null
                    && hitsSources.get(collectionReference.params.idPath) != null) {
                arlasHit.md.id = "" + hitsSources.get(collectionReference.params.idPath);
            }
            if (collectionReference.params.centroidPath != null
                    && hitsSources.get(collectionReference.params.centroidPath) != null) {
                String pointString = (String) hitsSources.get(collectionReference.params.centroidPath);
                String[] tokens = pointString.split(",");
                Double latitude = Double.parseDouble(tokens[0]);
                Double longitude = Double.parseDouble(tokens[1]);
                Point point = new Point(latitude, longitude);
                arlasHit.md.centroid = point;
            }
            if (collectionReference.params.geometryPath != null
                    && hitsSources.get(collectionReference.params.geometryPath) != null) {
                HashMap m = (HashMap) hitsSources.get(collectionReference.params.geometryPath);
                arlasHit.md.geometry = m;
            }
            if (collectionReference.params.timestampPath != null
                    && hitsSources.get(collectionReference.params.timestampPath) != null) {
                // TODO: parse timestamp
                // arlasHit.md.timestamp =
                // (String)hitsSources.get(collectionReference.params.timestampPath);
            }
            arlasHits.hits.add(arlasHit);
        }
        return Response.ok(arlasHits).build();
    }
}
