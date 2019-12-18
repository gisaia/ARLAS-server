/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.rest.explore.search;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.Link;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Page;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.*;
import io.arlas.server.ns.ATOM;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.*;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang.BooleanUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SearchRESTService extends ExploreRESTServices {

    public SearchRESTService(ExploreServices exploreServices) {
        super(exploreServices);
    }

    @Timed
    @Path("{collection}/_search")
    @GET
    @Produces({UTF8JSON, ATOM.APPLICATION_ATOM_XML})
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Search", produces = UTF8JSON + "," + ATOM.APPLICATION_ATOM_XML, notes = Documentation.SEARCH_OPERATION, consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response search(
            @Context UriInfo uriInfo,
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
                    value = Documentation.FILTER_PARAM_F,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "f") List<String> f,

            @ApiParam(name = "q", value = Documentation.FILTER_PARAM_Q,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "q") List<String> q,

            @ApiParam(name = "pwithin", value = Documentation.FILTER_PARAM_PWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "pwithin") List<String> pwithin,

            @ApiParam(name = "gwithin", value = Documentation.FILTER_PARAM_GWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gwithin") List<String> gwithin,

            @ApiParam(name = "gintersect", value = Documentation.FILTER_PARAM_GINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "gintersect") List<String> gintersect,

            @ApiParam(name = "notpwithin", value = Documentation.FILTER_PARAM_NOTPWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notpwithin") List<String> notpwithin,

            @ApiParam(name = "notgwithin", value = Documentation.FILTER_PARAM_NOTGWITHIN,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgwithin") List<String> notgwithin,

            @ApiParam(name = "notgintersect", value = Documentation.FILTER_PARAM_NOTGINTERSECT,
                    allowMultiple = true,
                    required = false)
            @QueryParam(value = "notgintersect") List<String> notgintersect,

            @ApiParam(name = "dateformat", value = Documentation.FILTER_DATE_FORMAT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "dateformat") String dateformat,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

//            @ApiParam(hidden = true) TODO uncomment
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // -----------------------  FORM    -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @DefaultValue("false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "flat", value = Documentation.FORM_FLAT,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "flat") Boolean flat,

            // --------------------------------------------------------
            // -----------------------  PROJECTION   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "include", value = Documentation.PROJECTION_PARAM_INCLUDE,
                    allowMultiple = true,
                    defaultValue = "*",
                    required = false)
            @QueryParam(value = "include") String include,

            @ApiParam(name = "exclude", value = Documentation.PROJECTION_PARAM_EXCLUDE,
                    allowMultiple = true,
                    defaultValue = "",
                    required = false)
            @QueryParam(value = "exclude") String exclude,

            // --------------------------------------------------------
            // -----------------------  PAGE   -----------------------
            // --------------------------------------------------------

            @ApiParam(name = "size", value = Documentation.PAGE_PARAM_SIZE,
                    defaultValue = "10",
                    allowableValues = "range[1, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("10")
            @QueryParam(value = "size") IntParam size,

            @ApiParam(name = "from", value = Documentation.PAGE_PARAM_FROM,
                    defaultValue = "0",
                    allowableValues = "range[0, infinity]",
                    type = "integer",
                    required = false)
            @DefaultValue("0")
            @QueryParam(value = "from") IntParam from,

            @ApiParam(name = "sort",
                    value = Documentation.PAGE_PARAM_SORT,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "sort") String sort,


            @ApiParam(name = "after",
                    value = Documentation.PAGE_PARAM_AFTER,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "after") String after,

            @ApiParam(name = "before",
                    value = Documentation.PAGE_PARAM_BEFORE,
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "before") String before,


            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {

        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        List<String> fields = new ArrayList<>();
        if (collectionReference.params.idPath != null)
            fields.add(collectionReference.params.idPath);
        if (collectionReference.params.geometryPath != null)
            fields.add(collectionReference.params.geometryPath);
        if (collectionReference.params.centroidPath != null)
            fields.add(collectionReference.params.centroidPath);
        if (collectionReference.params.timestampPath != null)
            fields.add(collectionReference.params.timestampPath);
        if(exclude!=null && exclude!=""){
            List<String> excludeField = Arrays.asList(exclude.split(","));
            CheckParams.checkExcludeField(excludeField, fields);
        }

        Search search = new Search();
        search.filter = ParamsParser.getFilter(f, q, pwithin, gwithin, gintersect, notpwithin, notgwithin, notgintersect, dateformat);
        search.page = ParamsParser.getPage(size, from, sort,after,before);
        search.projection = ParamsParser.getProjection(include, exclude);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        exploreServices.setValidGeoFilters(searchHeader);
        request.headerRequest = searchHeader;
        request.columnFilter = columnFilter;

        Hits hits = getArlasHits(request, collectionReference,BooleanUtils.isTrue(flat),uriInfo,"GET");
        return cache(Response.ok(hits), maxagecache);
    }

    @Timed
    @Path("{collection}/_search")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Search", produces = UTF8JSON, notes = Documentation.SEARCH_OPERATION, consumes = UTF8JSON, response = Hits.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Hits.class, responseContainer = "ArlasHits"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response searchPost(
            @Context UriInfo uriInfo,
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
            // ----------------------- SEARCH -----------------------
            // --------------------------------------------------------
            Search search,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = "Column-Filter") Optional<String> columnFilter,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,

            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws InterruptedException, ExecutionException, IOException, NotFoundException, ArlasException {
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }

        Search searchHeader = new Search();
        searchHeader.filter = ParamsParser.getFilter(partitionFilter);

        exploreServices.setValidGeoFilters(search);
        exploreServices.setValidGeoFilters(searchHeader);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = columnFilter;

        Hits hits = getArlasHits(request, collectionReference, (search.form != null && BooleanUtils.isTrue(search.form.flat)),uriInfo,"POST");
        return cache(Response.ok(hits), maxagecache);
    }


    protected Hits getArlasHits(MixedRequest request, CollectionReference collectionReference, Boolean flat, UriInfo uriInfo, String method) throws ArlasException, IOException {
        SearchHits searchHits = this.getExploreServices().search(request, collectionReference);
        Search searchRequest  = (Search)request.basicRequest;
        Hits hits = new Hits(collectionReference.collectionName);
        hits.totalnb = searchHits.getTotalHits();
        hits.nbhits = searchHits.getHits().length;
        HashMap<String,Link> links = new HashMap<>();
        hits.hits = new ArrayList<>((int) hits.nbhits);
        List<SearchHit>searchHitList= Arrays.asList(searchHits.getHits());
        if(searchRequest.page != null && searchRequest.page.before != null ){
            Collections.reverse(searchHitList);
        }
        for (SearchHit hit : searchHitList) {
            hits.hits.add(new Hit(collectionReference, hit.getSourceAsMap(), flat, false));
        }
        Link self = new Link();
        self.href = getRequestUri(uriInfo);
        self.method = method;
        Link next = null;
        Link previous = null;
        int lastIndex = (int) hits.nbhits -1;
        String sortParam = searchRequest.page != null ? searchRequest.page.sort : null;
        String afterParam = searchRequest.page != null ? searchRequest.page.after : null;
        String beforeParam = searchRequest.page != null ? searchRequest.page.before : null;
        Integer sizeParam = searchRequest.page != null ? searchRequest.page.size : ExploreServices.SEARCH_DEFAULT_PAGE_SIZE;
        String lastHitAfter = "";
        String firstHitAfter = "";
        if (lastIndex >= 0 && sizeParam == hits.nbhits && sortParam != null && (afterParam != null || sortParam.contains(collectionReference.params.idPath))) {
            next = new Link();
            next.method = method;
            // Use sorted value of last element return by ES to build after param of next & previous link
            lastHitAfter = Arrays.stream(searchHitList.get(lastIndex).getSortValues()).map(value->value.toString()).collect(Collectors.joining(","));
        }
        if (searchHitList.size()>0 && sortParam != null && (beforeParam != null || sortParam.contains(collectionReference.params.idPath))) {
            previous = new Link();
            previous.method = method;
            firstHitAfter = Arrays.stream(searchHitList.get(0).getSortValues()).map(value->value.toString()).collect(Collectors.joining(","));
        }

        switch (method){
            case"GET":
                links.put("self", self);
                if (next != null){
                    next.href = getNextHref(uriInfo, lastHitAfter);
                    links.put("next", next);
                }
                if (previous != null){
                    previous.href = getPreviousHref(uriInfo, firstHitAfter);
                    links.put("previous", previous);
                }
                break;
            case"POST":
                self.body = searchRequest;
                links.put("self", self);
                if (next != null){
                    Page nextPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection =searchRequest.projection;
                    nextPage.sort=searchRequest.page.sort;
                    nextPage.size=searchRequest.page.size;
                    nextPage.from =searchRequest.page.from;
                    nextPage.after = lastHitAfter;
                    search.page = nextPage;
                    next.href = self.href;
                    next.body = search;
                    links.put("next", next);
                }
                if (previous != null){
                    Page previousPage = new Page();
                    Search search = new Search();
                    search.filter = searchRequest.filter;
                    search.form = searchRequest.form;
                    search.projection =searchRequest.projection;
                    previousPage.sort=searchRequest.page.sort;
                    previousPage.size=searchRequest.page.size;
                    previousPage.from =searchRequest.page.from;
                    previousPage.before = firstHitAfter;
                    search.page = previousPage;
                    previous.href = self.href;
                    previous.body = search;
                    links.put("previous", previous);
                }
                break;
        }
        hits.links = links;
        return hits;
    }

    private String getBaseUri(UriInfo uriInfo) {
        String baseUri = this.exploreServices.getBaseUri();
        if (StringUtil.isNullOrEmpty(baseUri)) {
            baseUri = uriInfo.getBaseUri().toString();
        }
        return baseUri;
    }

    private String getPathUri(UriInfo uriInfo) {
        return uriInfo.getPath();
    }

    private String getAbsoluteUri(UriInfo uriInfo) {
        return getBaseUri(uriInfo) + getPathUri(uriInfo);
    }

    private String getQueryParameters(UriInfo uriInfo) {
       return uriInfo.getRequestUriBuilder().toTemplate().replace(uriInfo.getAbsolutePath().toString(), "");
    }

    private String getNextQueryParameters(UriInfo uriInfo, String afterValue) {
        return uriInfo.getRequestUriBuilder()
                .replaceQueryParam("after", afterValue)
                .replaceQueryParam("before", "")
                .toTemplate()
                .replace(uriInfo.getAbsolutePath().toString(), "").replace("&before=","").replace("before=&","");
    }

    private String getPreviousQueryParameters(UriInfo uriInfo, String afterValue) {
        return uriInfo.getRequestUriBuilder()
                .replaceQueryParam("after", "")
                .replaceQueryParam("before", afterValue)
                .toTemplate()
                .replace(uriInfo.getAbsolutePath().toString(), "").replace("&after=","").replace("after=&","");
    }

    private String getRequestUri(UriInfo uriInfo) {
        return getAbsoluteUri(uriInfo) + getQueryParameters(uriInfo);
    }

    private String getNextHref(UriInfo uriInfo, String afterValue) {
        return getAbsoluteUri(uriInfo) + getNextQueryParameters(uriInfo, afterValue);
    }
    private String getPreviousHref(UriInfo uriInfo, String afterValue) {
        return getAbsoluteUri(uriInfo) + getPreviousQueryParameters(uriInfo, afterValue);
    }
}
