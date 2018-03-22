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

package io.arlas.server.wfs;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.WFSConfiguration;
import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreRESTServices;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.wfs.filter.WFSQueryBuilder;
import io.arlas.server.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.wfs.utils.*;
import io.swagger.annotations.*;
import net.opengis.wfs._2.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Path("/wfs")
@Api(value = "/wfs")
public class WFSService extends ExploreRESTServices {
    Logger LOGGER = LoggerFactory.getLogger(WFSService.class);

    public WFSHandler wfsHandler;
    private WFSConfiguration wfsConfiguration;
    private String serverUrl;

    //TODO extends and create a wfsexploreService not inject it
    public WFSService(ExploreServices exploreServices, WFSHandler wfsHandler) {
        super(exploreServices);
        this.wfsHandler = wfsHandler;
        this.wfsConfiguration = wfsHandler.wfsConfiguration;
        this.serverUrl = wfsConfiguration.serverUri;
    }

    @Timed
    @Path("{collection}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @ApiOperation(
            value = "WFS",
            produces = MediaType.APPLICATION_XML,
            notes = "WFS"
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"
    ), @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response doKVP(
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "version",
                    value = "version",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "version") String version,
            @ApiParam(
                    name = "service",
                    value = "service",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "service") String service,
            @ApiParam(
                    name = "request",
                    value = "request",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "request") String request,
            @ApiParam(
                    name = "storedquery_id",
                    value = "storedquery_id",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "storedquery_id") String storedquery_id,
            @ApiParam(
                    name = "id",
                    value = "id",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "id") String id,
            @ApiParam(
                    name = "typenames",
                    value = "typenames",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "typenames") String typenames,
            @ApiParam(
                    name = "startindex",
                    value = "startindex",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "startindex") Integer startindex,
            @ApiParam(
                    name = "count",
                    value = "count",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "count") Integer count,
            @ApiParam(
                    name = "valuereference",
                    value = "valuereference",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "valuereference") String valuereference,
            @ApiParam(
                    name = "filter",
                    value = "filter",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "filter") String filter,
            @ApiParam(
                    name = "resourceid",
                    value = "resourceid",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "resourceid") String resourceid,
            @ApiParam(
                    name = "srsname",
                    value = "srsname",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "srsname") String srsname,
            @ApiParam(
                    name = "bbox",
                    value = "bbox",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "bbox") String bbox,
            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter
    ) throws IOException, ArlasException, ParserConfigurationException, SAXException, ExecutionException, InterruptedException {

        Version requestVersion = VersionUtils.getVersion(version);
        WFSRequestType requestType = RequestUtils.getRequestTypeByName(request);

        WFSCheckParam.checkQuerySyntax(service, bbox, resourceid, filter, requestType, requestVersion);

        startindex = Optional.ofNullable(startindex).orElse(0);
        count = Optional.ofNullable(count).orElse(wfsConfiguration.queryMaxFeature.intValue());

        CollectionReference collectionReference = exploreServices.getDaoCollectionReference().getCollectionReference(collection);
        if (collectionReference == null) {
            throw new WFSException(WFSExceptionCode.NOT_FOUND, "Collection not found " + collection);
        }
        ElasticAdmin elasticAdmin = new ElasticAdmin(exploreServices.getClient());
        // TODO add cache in describeCollection method
        CollectionReferenceDescription collectionReferenceDescription = elasticAdmin.describeCollection(collectionReference);
        String collectionName = collectionReferenceDescription.collectionName;
        String serviceUrl = serverUrl + "wfs/" + collectionName + "/?";
        QName featureQname = new QName(serviceUrl, collectionName, wfsConfiguration.featureNamespace);
        WFSCheckParam.checkTypeNames(collectionName, typenames);
        WFSQueryBuilder wfsQueryBuilder = new WFSQueryBuilder(requestType,id,bbox,filter,resourceid,storedquery_id,collectionReferenceDescription,partitionFilter,exploreServices);

        switch (requestType) {
            case GetCapabilities:
                GetCapabilitiesHandler getCapabilitiesHandler = wfsHandler.getCapabilitiesHandler;
                getCapabilitiesHandler.setFeatureTypeListType(collectionName, serviceUrl);
                getCapabilitiesHandler.setOperationsUrl(serviceUrl);
                JAXBElement<WFSCapabilitiesType> getCapabilitiesResponse = getCapabilitiesHandler.getWFSCapabilitiesResponse();
                return Response.ok(getCapabilitiesResponse).type(MediaType.APPLICATION_XML).build();
            case DescribeFeatureType:
                StreamingOutput describeFeatureTypeResponse = wfsHandler.describeFeatureTypeHandler.getDescribeFeatureTypeResponse(collectionReferenceDescription, serviceUrl);
                return Response.ok(describeFeatureTypeResponse).type(MediaType.APPLICATION_XML).build();
            case ListStoredQueries:
                wfsHandler.listStoredQueriesHandler.setFeatureType(featureQname);
                JAXBElement<ListStoredQueriesResponseType> listStoredQueriesResponse = wfsHandler.listStoredQueriesHandler.getListStoredQueriesResponse();
                return Response.ok(listStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();
            case DescribeStoredQueries:
                DescribeStoredQueriesResponseType describeStoredQueriesType = wfsHandler.wfsFactory.createDescribeStoredQueriesResponseType();
                wfsHandler.storedQueryManager.listStoredQueries().forEach(storedQuery -> {
                    storedQuery.setFeatureType(featureQname);
                    describeStoredQueriesType.getStoredQueryDescription().add(storedQuery.getStoredQueryDescription());
                });
                JAXBElement<DescribeStoredQueriesResponseType> describeStoredQueriesResponse = wfsHandler.wfsFactory.createDescribeStoredQueriesResponse(describeStoredQueriesType);
                return Response.ok(describeStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();
            case GetFeature:
                WFSCheckParam.checkSrsName(srsname);
                StreamingOutput getFeatureResponse = null;
                SearchHits hitsGetFeature;
                if (wfsQueryBuilder.isStoredQuey) {
                    hitsGetFeature = exploreServices.getClient()
                            .prepareSearch(collectionReference.params.indexName)
                            .setQuery(wfsQueryBuilder.wfsQuery)
                            .execute()
                            .get()
                            .getHits();
                    SearchHit response;
                    if (hitsGetFeature.getHits().length > 0) {
                        response = hitsGetFeature.getAt(0);
                    } else {
                        throw new WFSException(WFSExceptionCode.NOT_FOUND, "Data not found", "resourceid");
                    }
                    getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureByIdResponse(response, collectionReferenceDescription, serviceUrl);

                } else {
                    hitsGetFeature = exploreServices
                            .getClient()
                            .prepareSearch(collectionReference.params.indexName)
                            .setQuery(wfsQueryBuilder.wfsQuery)
                            .setFrom(startindex)
                            .setSize(count)
                            .execute()
                            .get()
                            .getHits();
                    List<Object> featureList = new ArrayList<>();
                    for (int i = 0; i < hitsGetFeature.getHits().length; i++) {
                        featureList.add(hitsGetFeature.getAt(i));
                    }
                    getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureResponse(wfsHandler.wfsConfiguration, collectionReferenceDescription, startindex, count, featureList, serviceUrl);
                }
                return Response.ok(getFeatureResponse).type(MediaType.APPLICATION_XML).build();

            case GetPropertyValue:
                String include = WFSCheckParam.formatValueReference(valuereference, collectionReferenceDescription);
                ValueCollectionType valueCollectionType = new ValueCollectionType();
                SearchHits hitsGetPropertyValue = exploreServices.getClient()
                        .prepareSearch(collectionReference.params.indexName)
                        .setFetchSource(include, null)
                        .setQuery(wfsQueryBuilder.wfsQuery)
                        .setFrom(startindex)
                        .setSize(count)
                        .execute()
                        .get()
                        .getHits();
                for (int i = 0; i < hitsGetPropertyValue.getHits().length; i++) {
                    MemberPropertyType e = new MemberPropertyType();
                    e.getContent().add(MapExplorer.getObjectFromPath(include, hitsGetPropertyValue.getAt(i).getSourceAsMap()));
                    valueCollectionType.getMember().add(e);
                }
                return Response.ok(wfsHandler.wfsFactory.createValueCollection(valueCollectionType)).type(MediaType.APPLICATION_XML).build();
            default:
                throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.");
        }
    }
}
