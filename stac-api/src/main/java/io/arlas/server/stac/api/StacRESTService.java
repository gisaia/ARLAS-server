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

package io.arlas.server.stac.api;

import com.ethlo.time.ITU;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.app.STACConfiguration;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Link;
import io.arlas.server.core.model.enumerations.ComputationEnum;
import io.arlas.server.core.model.enumerations.OperatorEnum;
import io.arlas.server.core.model.request.ComputationRequest;
import io.arlas.server.core.model.request.MixedRequest;
import io.arlas.server.core.model.request.Search;
import io.arlas.server.core.model.response.ArlasHit;
import io.arlas.server.core.model.response.Hits;
import io.arlas.server.core.model.response.MD;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.*;
import io.arlas.server.stac.model.Collection;
import io.arlas.server.stac.model.*;
import io.dropwizard.jersey.params.IntParam;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.Polygon;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.arlas.server.core.utils.TimestampTypeMapper.formatDate;
import static jakarta.ws.rs.core.UriBuilder.fromUri;

@Path("/stac")
@Tag(name="stac", description="STAC API")
@OpenAPIDefinition
public abstract class StacRESTService {
    protected ExploreService exploreService;
    protected CollectionReferenceService collectionReferenceService;
    protected STACConfiguration configuration;
    protected ResponseCacheManager responseCacheManager;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final GeoJsonReader reader = new GeoJsonReader();
    public static final ObjectWriter writer = objectMapper.writer();
    public final String baseUri;

    public StacRESTService(STACConfiguration configuration,
                           int arlasRestCacheTimeout,
                           CollectionReferenceService collectionReferenceService,
                           ExploreService exploreService,String baseUri) {
        super();
        this.configuration = configuration;
        this.collectionReferenceService = collectionReferenceService;
        this.exploreService = exploreService;
        this.responseCacheManager = new ResponseCacheManager(arlasRestCacheTimeout);
        this.baseUri = baseUri;
    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        return responseCacheManager.cache(response, maxagecache);
    }

    // ------------------

    protected StacLink getRootLink(UriInfo uriInfo) {
        return new StacLink().rel("root").type(MediaType.APPLICATION_JSON)
                .href(fromUri(new UriInfoWrapper(uriInfo,baseUri).getBaseUri()).path(StacRESTService.class).build().toString());
    }

    protected StacLink getParentLink(UriInfo uriInfo) {
        return new StacLink().rel("parent").type(MediaType.APPLICATION_JSON)
                .href(fromUri(new UriInfoWrapper(uriInfo,baseUri).getBaseUri()).path(StacRESTService.class).build().toString());
    }

    protected StacLink getSelfLink(UriInfo uriInfo) {
        return new StacLink().rel("self").type(MediaType.APPLICATION_JSON)
                .href(uriInfo.getRequestUriBuilder().build().toString());
    }

    protected StacLink getApiLink(UriInfo uriInfo) {
        return new StacLink().rel("service-desc").type("application/vnd.oai.openapi+json;version=3.0")
                .href(fromUri(new UriInfoWrapper(uriInfo,baseUri).getBaseUri()).path(StacRESTService.class).path("/api").build().toString());
    }

    protected StacLink getRawLink(String url, String rel) {
        return getRawLink(url, rel, MediaType.APPLICATION_JSON);
    }

    protected StacLink getRawLink(String url, String rel, Object body) {
        return new StacLink().method("POST").rel(rel).type(MediaType.APPLICATION_JSON).href(url).body(body);
    }

    protected StacLink getLink(UriInfo uriInfo, String path, String rel, String type) {
        return getLink(uriInfo, path, null, rel, type);
    }

    protected StacLink getLink(UriInfo uriInfo, String path, String method, String rel, String type) {
        return new StacLink().method(method).rel(rel).type(type)
                .href(fromUri(new UriInfoWrapper(uriInfo,baseUri).getBaseUri()).path(StacRESTService.class).path(path).build().toString());
    }

    protected String getAggregateUrl(UriInfo uriInfo, CollectionReference collection, String type, String id) {
        UriBuilder builder = fromUri(new UriInfoWrapper(uriInfo,baseUri).getBaseUri())
                .path("explore")
                .path(collection.collectionName)
                .path(type);
        if (id != null) {
            builder.queryParam("f", StringUtil.concat(collection.params.idPath, ":", OperatorEnum.eq.name(), ":", id));
        }
        return builder.build().toString();
    }

    protected Collection getCollection(CollectionReference collectionReference, UriInfo uriInfo) throws ArlasException {
        // https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#link-object
        List<StacLink> cLinks = new ArrayList<>();
        cLinks.add(getRootLink(uriInfo));
        cLinks.add(getParentLink(uriInfo));
        if(collectionReference.params.licenseUrls != null &&  !collectionReference.params.licenseUrls.isEmpty()){
            collectionReference.params.licenseUrls.forEach(l -> cLinks.add(getRawLink(l, "licence")));
        }
        String licenseName = collectionReference.params.licenseName;
        if(licenseName == null){
            licenseName = "proprietary";
        }
        if((licenseName.equals("proprietary") || licenseName.equals("various"))  && collectionReference.params.licenseUrls == null  ){
            cLinks.add(getRawLink("missing configuration", "licence"));
        }
        cLinks.add(getLink(uriInfo, "collections/" + collectionReference.collectionName, "self", MediaType.APPLICATION_JSON));
        cLinks.add(getLink(uriInfo, "collections/" + collectionReference.collectionName + "/items", "items", "application/geo+json"));

        // https://github.com/radiantearth/stac-spec/blob/master/collection-spec/collection-spec.md#collection-fields
        return new Collection()
                .id(collectionReference.collectionName)
                .stacVersion(configuration.stacVersion)
                .stacExtensions(new ArrayList<>())
                .title(collectionReference.params.dublinCoreElementName.title)
                .description(collectionReference.params.dublinCoreElementName.description)
                .keywords(collectionReference.params.inspire.keywords.stream().map(k -> k.value).collect(Collectors.toList()))
                .license(licenseName)
                //.providers(new ArrayList<>()) // TODO optional
                .extent(new Extent().spatial(getSpatialExtent(collectionReference))
                        .temporal(getTemporalExtent(collectionReference)))
                //.summaries(new HashMap<>()) // TODO strongly recommended
                .links(cLinks)
                //.assets(new HashMap<>()) // TODO optional
                ;
    }

    protected Item getItem(ArlasHit hit,CollectionReference collection, UriInfo uriInfo){
        List<StacLink> links = getItemLinks(uriInfo, collection, hit.md.id);
        return new Item()
                .stacVersion(configuration.stacVersion)
                .stacExtensions(configuration.stacExtensions)
                .itemStacModel(hit,collection.collectionName, GeoUtil.getBbox(hit.getGeometry(collection.params.geometryPath)))
                .links(links);
    }

    protected Item getItem(Feature feature, CollectionReference collection, UriInfo uriInfo) {
        // add datetime https://github.com/radiantearth/stac-spec/blob/master/item-spec/item-spec.md#properties-object
        MD md = feature.getProperty(MD.class.getSimpleName().toLowerCase());
        List<StacLink> links = getItemLinks(uriInfo, collection, md.id);
        feature.setProperty("datetime", ITU.formatUtc(OffsetDateTime.ofInstant(Instant.ofEpochMilli(md.timestamp), ZoneOffset.UTC)));

        Map<String, Object> assets = new HashMap<>();
        Asset geojsonAsset = new Asset()
                .name("geojson")
                .title("Export geojson")
                .description("Get this item in geosjon format")
                .href(getAggregateUrl(uriInfo, collection, "_geosearch", md.id))
                .type("application/geo+json")
                .roles(new ArrayList<>(Collections.singleton("geojson")));

        Asset shapeFileAsset = new Asset()
                .name("shapefile")
                .title("Export shapefile")
                .description("Get this item in shapefile format")
                .href(getAggregateUrl(uriInfo, collection, "_shapesearch", md.id))
                .type("application/zip")
                .roles(new ArrayList<>(Collections.singleton("shapefile")));

        assets.put("geojson", geojsonAsset);
        assets.put("shapefile", shapeFileAsset);

        // https://github.com/radiantearth/stac-spec/blob/master/item-spec/item-spec.md#item-fields
        return new Item()
                .stacVersion(configuration.stacVersion)
                .stacExtensions(new ArrayList<>())
                .id(md.id)
                .geometry(feature.getGeometry())
                .bbox(GeoUtil.getBbox(feature.getGeometry()))
                .properties(feature.getProperties())
                .links(links)
                .assets(assets)
                .collection(collection.collectionName);
    }

    private List<StacLink> getItemLinks(UriInfo uriInfo, CollectionReference collection, String id){
        List<StacLink> links = new ArrayList<>();
        links.add(getLink(uriInfo, "collections/" + collection.collectionName  + "/items/" + id, "self", MediaType.APPLICATION_JSON));
        links.add(getRootLink(uriInfo));
        links.add(getLink(uriInfo, "collections/" + collection.collectionName, "parent", MediaType.APPLICATION_JSON));
        links.add(getLink(uriInfo, "collections/" + collection.collectionName, "collection", MediaType.APPLICATION_JSON));
        return links;
    }

    private static String getCleanSortBy(String idPath, String sortByParam) {
        AtomicBoolean hasId = new AtomicBoolean(false);
        String sortBy = sortByParam == null ? "" : sortByParam;
        List<String> sort = Arrays.stream(sortBy.split(","))
                .filter(s -> !s.isEmpty())
                .map(s ->  {
                    String ret = s.startsWith("+") ? s.substring(1) : s;
                    if ((ret.startsWith("-") && ret.substring(1).equals(idPath)) || ret.equals(idPath)) {
                        hasId.set(true);
                    }
                    return ret;
                }).collect(Collectors.toList());
        if (!hasId.get()) {
            sort.add(idPath);
        }
        return String.join(",", sort);
    }

    protected <T> StacFeatureCollection getStacFeatureCollection(CollectionReference collectionReference,
                                                                 String partitionFilter,
                                                                 Optional<String> columnFilter,
                                                                 SearchBody<T> body,
                                                                 List<String> filter,
                                                                 UriInfo uriInfo,
                                                                 String method,
                                                                 boolean isOgc) throws ArlasException {
        Search search = new Search();
        search.filter = ParamsParser.getFilter(collectionReference, filter, null, null, true);
        if (body != null) {
            String sortBy = null;
            if(body.getSortBy() instanceof String){
                sortBy = (String) body.getSortBy();
            }else if(body.getSortBy() instanceof List){
                sortBy = ((List<SortBy>) body.getSortBy()).stream()
                        .map(sb -> SortBy.getCharDirection(sb.getDirection()) + sb.getField()).collect(Collectors.joining(","));
            }
            search.page = ParamsParser.getPage(new IntParam(body.getLimit().toString()),
                    new IntParam(body.getFrom().toString()),
                    getCleanSortBy(collectionReference.params.idPath, sortBy),
                    body.getAfter(), body.getBefore());
        }
        exploreService.setValidGeoFilters(collectionReference, search);

        ColumnFilterUtil.assertRequestAllowed(columnFilter, collectionReference, search);

        Search searchHeader = new Search();
        searchHeader.partitionFilter = ParamsParser.getPartitionFilter(collectionReference, partitionFilter);
        MixedRequest request = new MixedRequest();
        request.basicRequest = search;
        request.headerRequest = searchHeader;
        request.columnFilter = ColumnFilterUtil.getCollectionRelatedColumnFilter(columnFilter, collectionReference);

        HashMap<String, Object> context = new HashMap<>();


        List<StacLink> links = new ArrayList<>(); // TODO what do we put in there?
        links.add(getRootLink(uriInfo));
        links.add(getParentLink(uriInfo));

        List<Item> items;
        if(collectionReference.params.isStacModel){
            Hits hits = exploreService.search(request, collectionReference, false, uriInfo, method);
            items = hits.hits.stream()
                    .map(hit ->  getItem(hit, collectionReference, uriInfo))
                    .collect(Collectors.toList());
            context.putAll(hits.links);
            context.put("matched",hits.totalnb);
        }else{
            FeatureCollection features = exploreService.getFeatures(request, collectionReference, false,
                    uriInfo, method, context);
            items = features.getFeatures().stream()
                    .map(feat ->  getItem(feat, collectionReference, uriInfo))
                    .collect(Collectors.toList());
        }
        if (context.get("self") == null) {
            links.add(getSelfLink(uriInfo));
        } else {
            Arrays.asList("self", "next", "previous").forEach(rel -> {
                if (context.containsKey(rel)) {
                    if (method.equals("POST")) {
                        // TODO : fix when body is null!!
                        links.add(getRawLink(((Link)context.get(rel)).href, rel, getSearchBody(body, (Search) ((Link)context.get(rel)).body)));
                    } else {
                        links.add(getRawLink(((Link)context.get(rel)).href, rel));
                    }
                }
            });
        }
        StacFeatureCollection response = new StacFeatureCollection();
        response.setFeatures(items);
        response.setLinks(links);
        if (isOgc) {
            response.setNumberMatched(((Long)context.get("matched")).intValue());
            response.setNumberReturned(items.size());
            response.setTimeStamp(ITU.formatUtc(OffsetDateTime.now()));
        } else {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("returned", (long) items.size());
            ctx.put("limit", body == null ? 10 : body.getLimit());
            ctx.put("matched", context.get("matched"));
            response.setContext(ctx);
        }
        return response;
    }

    private SearchBody getSearchBody(SearchBody body, Search search) {
        return new SearchBody()
                .after(search.page.after)
                .bbox(body == null ? null : body.getBbox())
                .before(search.page.before)
                .collections(body == null ? null : body.getCollections())
                .datetime(body == null ? null : body.getDatetime())
                .from(search.page.from)
                .ids(body == null ? null : body.getIds())
                .intersects(body == null ? null : body.getIntersects())
                .limit(search.page.size)
                .sortBy(search.page.sort);
    }

    protected String getDateFilter(String datetime, CollectionReference collectionReference) throws ArlasException {
        if (StringUtil.isNullOrEmpty(datetime)) {
            return null; // no date filter
        }
        if (collectionReference.params.timestampPath == null) {
            throw new ArlasException("No default timestamp path defined for the collection");
        }
        String dateField = collectionReference.params.timestampPath;

        String format =  collectionReference.params.customParams.get(CollectionReference.TIMESTAMP_FORMAT);
        Object dateFormatted;
        if (datetime.startsWith("/")) {
            datetime = ".." + datetime;
        }
        if (datetime.endsWith("/")) {
            datetime = datetime + "..";
        }
        if (datetime.endsWith(".Z")) {
            throw new InvalidParameterException("Datetime value is not RFC 3339 compatible (missing fractional seconds: " + datetime);
        }

        String[] parts = datetime.split("/");
        if (parts.length == 1) {
            try {
                Long millisecondValue = OffsetDateTime.parse(datetime).toInstant().toEpochMilli();
                dateFormatted = formatDate(millisecondValue,format);
            } catch (DateTimeParseException e) {
                throw new InvalidParameterException("Datetime value is not RFC 3339 compatible: " + datetime);
            }
            return StringUtil.concat(dateField, ":", OperatorEnum.eq.name(), ":", String.valueOf(dateFormatted));
        } else if (parts.length == 2) {
            if (parts[0].equals("..")) {
                return StringUtil.concat(dateField, ":", OperatorEnum.lte.name(), ":", getTimestamp(parts[1]));
            } else if (parts[1].equals("..")) {
                return StringUtil.concat(dateField, ":", OperatorEnum.gte.name(), ":", getTimestamp(parts[0]));
            } else {
                if (OffsetDateTime.parse(parts[0]).toInstant().toEpochMilli() >
                        OffsetDateTime.parse(parts[1]).toInstant().toEpochMilli()) {
                    throw new InvalidParameterException("Interval dates cannot be the same: " + datetime );
                }
                return StringUtil.concat(dateField, ":", OperatorEnum.range.name(), ":[", getTimestamp(parts[0]), "<", getTimestamp(parts[1]), "]");
            }
        } else {
            throw new InvalidParameterException("Invalid datetime format for value " + datetime);
        }
    }

    protected String getGeoFilter(GeoJsonObject geojson, CollectionReference collectionReference) throws ArlasException {
        if (geojson != null) {
            try {
                // righthand parameter is forced for STAC; therefore, passed righthand WKTs will be used correctly;
                Geometry geometry = reader.read(writer.writeValueAsString(geojson));
                return StringUtil.concat(collectionReference.params.geometryPath, ":", OperatorEnum.intersects.name(), ":",
                        geometry.toText());
            } catch (ParseException | JsonProcessingException e) {
                throw new InvalidParameterException("Invalid geojson:" +e.getMessage());
            }
        }
        return null;
    }

    protected List<Double> getBboxAsList(String bbox) throws InvalidParameterException {
        List<Double> bboxList = null;
        try {
            if (bbox != null) {
                bboxList = Stream.of(bbox.split(",")).map(Double::valueOf).collect(Collectors.toList());
            }
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid bbox definition: " + bbox);
        }
        return bboxList;
    }
    protected String getGeoFilter(List<Double> bbox, CollectionReference collectionReference) throws ArlasException {
        if (collectionReference.params.centroidPath == null) {
            throw new ArlasException("No default centroid path defined for the collection");
        }
        if (bbox == null || bbox.isEmpty()) {
            return null; // no bbox filter
        }

        if (bbox.size() != 4 && bbox.size() != 6) {
            throw new InvalidParameterException("Bbox should have 4 or 6 points.");
        }
        if (bbox.size() == 6) { // ignore 3D coordinates
            bbox.remove(2);
            bbox.remove(4);
        }
        return StringUtil.concat(collectionReference.params.centroidPath,
                ":within:",
                bbox.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(",")));
    }

    protected String getIdFilter(String id, CollectionReference collectionReference) {
        return getIdFilter(Collections.singletonList(id), collectionReference);
    }

    protected String getIdFilter(List<String> ids, CollectionReference collectionReference) {
        if (ids != null && !ids.isEmpty()) {
            return StringUtil.concat(collectionReference.params.idPath, ":", OperatorEnum.eq.name(), ":",
                    String.join(",", ids));
        }
        return null;
    }

    protected ExtentSpatial getSpatialExtent(CollectionReference collectionReference) throws ArlasException {
        ComputationRequest computationRequest = new ComputationRequest();
        computationRequest.field = collectionReference.params.centroidPath;
        computationRequest.metric = ComputationEnum.GEOBBOX;
        exploreService.setValidGeoFilters(collectionReference, computationRequest);

        MixedRequest request = new MixedRequest();
        request.basicRequest = computationRequest;
        request.headerRequest = new ComputationRequest();
        return new ExtentSpatial().bbox(getBbox(
                Optional.ofNullable((Polygon)exploreService.compute(request, collectionReference).geometry)
                        .orElseThrow(InvalidParameterException::new)
                )
        );
    }

    private List<List<Double>> getBbox(Polygon polygon) {
        return Collections.singletonList(Arrays.asList(
                polygon.getCoordinates().get(0).get(0).getLongitude(),
                polygon.getCoordinates().get(0).get(3).getLatitude(),
                polygon.getCoordinates().get(0).get(2).getLongitude(),
                polygon.getCoordinates().get(0).get(1).getLatitude()));
    }

    protected ExtentTemporal getTemporalExtent(CollectionReference collectionReference) throws ArlasException {
        ComputationRequest computationRequest = new ComputationRequest();
        computationRequest.field = collectionReference.params.timestampPath;
        computationRequest.metric = ComputationEnum.MIN;
        MixedRequest request = new MixedRequest();
        request.basicRequest = computationRequest;
        request.headerRequest = new ComputationRequest();
        Double minTime = exploreService.compute(request, collectionReference).value;

        computationRequest.metric = ComputationEnum.MAX;
        Double maxTime = exploreService.compute(request, collectionReference).value;

        if (minTime != null && maxTime != null) {
            return new ExtentTemporal().interval(Collections.singletonList(
                    Arrays.asList(ITU.formatUtc(OffsetDateTime.ofInstant(Instant.ofEpochMilli(minTime.longValue()), ZoneOffset.UTC)),
                            ITU.formatUtc(OffsetDateTime.ofInstant(Instant.ofEpochMilli(maxTime.longValue()), ZoneOffset.UTC)))));
        } else {
            return null;
        }
    }

    private String getTimestamp(String datetime) throws ArlasException {
        // RFC 3339 can have several date formats (with or without fractional seconds) so let's use a library capable of parsing them all
        if (ITU.isValid(datetime)) {
            return String.valueOf(ITU.parseDateTime(datetime).toInstant().toEpochMilli());
        } else {
            throw new InvalidParameterException("Datetime value is not RFC 3339 compatible: " + datetime);
        }
    }
}
