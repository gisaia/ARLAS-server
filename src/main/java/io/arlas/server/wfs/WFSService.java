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

import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.request.*;
import io.arlas.server.model.response.*;
import io.arlas.server.model.response.Error;
import io.arlas.server.rest.explore.ExploreServices;
import io.arlas.server.utils.ParamsParser;
import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.wfs.utils.RequestUtils;
import io.arlas.server.wfs.utils.VersionUtils;
import io.arlas.server.wfs.utils.WFSConstant;
import io.swagger.annotations.*;
import net.opengis.wfs._2.*;
import org.deegree.commons.ows.exception.OWSException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.ows.Version;


import org.deegree.protocol.wfs.WFSRequestType;

@Path("/wfs")

@Api(value = "/wfs")

public class WFSService {
    Logger LOGGER = LoggerFactory.getLogger(WFSService.class);

    public WFSHandler wfsHandler;
    public ExploreServices exploreServices;

    public WFSService(ExploreServices exploreServices, WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
        this.exploreServices = exploreServices;
    }

    @Context
    UriInfo uri;
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
                    name = "valuereference",
                    value = "valuereference",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "valuereference") String valuereference,
            @ApiParam(hidden = true)
            @HeaderParam(value = "Partition-Filter") String partitionFilter
    ) throws IOException, ArlasException, OWSException,WFSException {

        Version requestVersion = VersionUtils.getVersion(version);
        WFSRequestType requestType = RequestUtils.getRequestTypeByName(request);

        if (requestType == null) {
            String msg = "Request type '" + request + "' is not supported.";
            throw  new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED,msg,"request");
        }

        // check if requested version is supported and offered (except for GetCapabilities)
        if (requestType != WFSRequestType.GetCapabilities) {
            if (requestVersion == null) {
                String msg = "Missing version parameter.";
                throw  new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE,msg,"version");
            }
            VersionUtils.checkVersion(requestVersion, wfsHandler.arlasServerConfiguration);
        }else{
            if(service==null|| !service.equals("WFS")){
                String msg = "Missing service parameter.";
                throw  new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE,msg,"service");
            }
        }
        CollectionReference collectionReference = exploreServices.getDaoCollectionReference()
                .getCollectionReference(collection);
        if (collectionReference == null) {
            throw new NotFoundException(collection);
        }
        ElasticAdmin elasticAdmin = new ElasticAdmin(exploreServices.getClient());
        CollectionReferenceDescription collectionReferenceDescription = elasticAdmin.describeCollection(collectionReference);
        String collectionName = collectionReferenceDescription.collectionName;
        String idPath = collectionReferenceDescription.params.idPath;
        QName qname = new QName(uri.getBaseUri().toURL().toString() + uri.getPath().toString(), collectionName,"arlas");

        switch (requestType) {
            case GetCapabilities:
                wfsHandler.getCapabilitiesHandler.setFeatureTypeListType(collectionName,uri.getBaseUri().toURL().toString() + uri.getPath().toString());
                wfsHandler.getCapabilitiesHandler.setOperationsUrl(uri.getBaseUri().toURL().toString() + uri.getPath().toString());
                JAXBElement<WFSCapabilitiesType> getCapabilitiesResponse = wfsHandler.getCapabilitiesHandler.getWFSCapabilitiesResponse();
                return Response.ok(getCapabilitiesResponse).type(MediaType.APPLICATION_XML).build();
            case DescribeFeatureType:
                if(typenames!=null){
                    if (!typenames.equals(collectionName)){
                        throw  new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE,"FeatureType "+typenames+" not exist","typenames");
                    }
                }
                StreamingOutput describeFeatureTypeResponse = wfsHandler.describeFeatureTypeHandler.getDescribeFeatureTypeResponse(collectionName, uri.getBaseUri().toURL().toString() + uri.getPath().toString());
                return Response.ok(describeFeatureTypeResponse).type(MediaType.APPLICATION_XML).build();
            case ListStoredQueries:
                wfsHandler
                        .listStoredQueriesHandler
                        .listStoredQueriesResponseType
                        .getStoredQuery()
                        .forEach(storedQueryListItemType -> {
                                    if (storedQueryListItemType.getReturnFeatureType().indexOf(qname) < 0) {
                                        storedQueryListItemType.getReturnFeatureType().add(qname);
                                    }
                                }
                        );
                JAXBElement<ListStoredQueriesResponseType> listStoredQueriesResponse = wfsHandler.listStoredQueriesHandler.getListStoredQueriesResponse();
                return Response.ok(listStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();
            case DescribeStoredQueries:
                wfsHandler
                        .listStoredQueriesHandler
                        .listStoredQueriesResponseType
                        .getStoredQuery()
                        .forEach(storedQueryListItemType -> {
                                    if (storedQueryListItemType.getReturnFeatureType().indexOf(qname) < 0) {
                                        storedQueryListItemType.getReturnFeatureType().add(qname);
                                    }
                                }
                        );
                DescribeStoredQueriesResponseType describeStoredQueriesType = wfsHandler.wfsFactory.createDescribeStoredQueriesResponseType();
                wfsHandler.storedQueryManager.listStoredQueries().forEach(storedQuery -> {
                    storedQuery.getStoredQueryDescription().getQueryExpressionText().forEach(queryExpressionTextType -> queryExpressionTextType.getReturnFeatureTypes().add(qname));
                    describeStoredQueriesType.getStoredQueryDescription().add(storedQuery.getStoredQueryDescription());
                });
                JAXBElement<DescribeStoredQueriesResponseType> describeStoredQueriesResponse = wfsHandler.wfsFactory.createDescribeStoredQueriesResponse(describeStoredQueriesType);
                return Response.ok(describeStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();

            case GetFeature:
                if (storedquery_id != null) {
                    if (!storedquery_id.equals(WFSConstant.GET_FEATURE_BY_ID_NAME)){
                        throw  new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE,"StoredQuery "+storedquery_id+" not found","storedquery_id");
                    }
                    Search search = new Search();
                    Filter filter = new Filter();
                    Expression idExpression = new Expression();
                    idExpression.field = idPath;
                    idExpression.op = OperatorEnum.eq;
                    idExpression.value = id;
                    filter.f = new ArrayList<>();
                    MultiValueFilter<Expression> me = new MultiValueFilter<>();
                    me.add(idExpression);
                    filter.f.add(me);
                    search.filter = filter;
                    Size size = new Size();
                    size.size = 1;
                    size.from = 0;
                    search.size = size;
                    Search searchHeader = new Search();
                    searchHeader.filter = ParamsParser.getFilter(partitionFilter);
                    MixedRequest mixedRequest = new MixedRequest();
                    mixedRequest.basicRequest = search;
                    mixedRequest.headerRequest = searchHeader;
                    Hits hits = getArlasHits(mixedRequest, collectionReference);
                    if(hits.hits.size()==0){
                        throw  new WFSException(WFSExceptionCode.NOT_FOUND,"Data not found");
                    }
                    StreamingOutput getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureByIdResponse(wfsHandler.arlasServerConfiguration,hits.hits.get(0).md,collectionReference,uri.getBaseUri().toURL().toString() + uri.getPath().toString());
                    return Response.ok(getFeatureResponse).type(MediaType.APPLICATION_XML).build();

                } else {
                    Search search = new Search();
                    Filter filter = new Filter();
                    search.filter = filter;
                    Size size = new Size();
                    size.size = 100;
                    size.from = 0;
                    search.size = size;
                    Search searchHeader = new Search();
                    searchHeader.filter = ParamsParser.getFilter(partitionFilter);
                    MixedRequest mixedRequest = new MixedRequest();
                    mixedRequest.basicRequest = search;
                    mixedRequest.headerRequest = searchHeader;
                    Hits hits = getArlasHits(mixedRequest, collectionReference);
                    List<Object> featureList = new ArrayList<Object>();
                    hits.hits.forEach(hit -> {
                        featureList.add(hit.md);

                    });
                    StreamingOutput getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureResponse(wfsHandler.arlasServerConfiguration, collectionReference,0, null, featureList,uri.getBaseUri().toURL().toString() + uri.getPath().toString());
                    return Response.ok(getFeatureResponse).type(MediaType.APPLICATION_XML).build();
                }
            case GetPropertyValue:
                if(valuereference==null||valuereference.equals("")){
                    throw  new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE,"Invalid valuereference value","valuereference");
                }
            default:
                throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR,"Internal error: Unhandled request '" + request + "'.");
        }
    }

    protected Hits getArlasHits(MixedRequest request, CollectionReference collectionReference) throws ArlasException, IOException {
        SearchHits searchHits = exploreServices.search(request, collectionReference);
        Hits hits = new Hits();
        hits.totalnb = searchHits.totalHits();
        hits.nbhits = searchHits.getHits().length;
        hits.hits = new ArrayList<>((int) hits.nbhits);
        for (SearchHit hit : searchHits.getHits()) {
            hits.hits.add(new Hit(collectionReference, hit.getSource()));
        }
        return hits;
    }
}


