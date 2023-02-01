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

package io.arlas.server.ogc.wfs;

import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.OGCConfiguration;
import io.arlas.server.core.app.WFSConfiguration;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.ogc.common.OGCRESTService;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.OGCCheckParam;
import io.arlas.server.ogc.common.utils.RequestUtils;
import io.arlas.server.ogc.common.utils.Version;
import io.arlas.server.ogc.common.utils.VersionUtils;
import io.arlas.server.ogc.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.ogc.wfs.services.WFSToolService;
import io.arlas.server.ogc.wfs.utils.ExtendedWFSCapabilitiesType;
import io.arlas.server.ogc.wfs.utils.WFSCheckParam;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.WFSRequestType;
import io.swagger.annotations.*;
import net.opengis.wfs._2.DescribeStoredQueriesResponseType;
import net.opengis.wfs._2.ListStoredQueriesResponseType;
import net.opengis.wfs._2.ValueCollectionType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

@Path("/ogc/wfs")
@Api(value = "/ogc/wfs")
public class WFSRESTService extends OGCRESTService {

    public WFSHandler wfsHandler;
    private final WFSConfiguration wfsConfiguration;
    private final OGCConfiguration ogcConfiguration;
    private final String serverBaseUrl;
    protected WFSToolService wfsToolService;

    //TODO extends and create a wfsexploreService not inject it
    public WFSRESTService(WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
        this.wfsConfiguration = wfsHandler.wfsConfiguration;
        this.ogcConfiguration = wfsHandler.ogcConfiguration;
        this.serverBaseUrl = wfsHandler.baseUri;
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
            @ApiParam(
                    name = "language",
                    value = "language",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "language") String language,

            //header filters
            @ApiParam(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,
            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,
            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations


    ) throws IOException, ArlasException {

        Version requestVersion = VersionUtils.getVersion(version, Service.WFS);
        RequestUtils.checkRequestTypeByName(request, WFSConstant.SUPPORTED_WFS_REQUESTYPE, Service.WFS);
        WFSRequestType requestType = WFSRequestType.valueOf(request);

        WFSCheckParam.checkQuerySyntax(service, bbox, resourceid, filter, requestType, requestVersion);

        startindex = Optional.ofNullable(startindex).orElse(0);
        count = Optional.ofNullable(count).orElse(ogcConfiguration.queryMaxFeature.intValue());

        CollectionReference collectionReference = collectionReferenceService.getCollectionReference(collection, Optional.ofNullable(organisations));
        if (collectionReference == null) {
            throw new OGCException(OGCExceptionCode.NOT_FOUND, "Collection not found " + collection, Service.WFS);
        }

        ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), List.of(collectionReference));

        CollectionReferenceDescription collectionReferenceDescription = wfsToolService.getCollectionReferenceDescription(collectionReference);
        String collectionName = collectionReferenceDescription.collectionName;
        String serviceUrl = serverBaseUrl + "ogc/wfs/" + collectionName + "/?";
        QName featureQname = new QName(serviceUrl, collectionName, wfsConfiguration.featureNamespace);
        WFSCheckParam.checkTypeNames(collectionName, typenames);
        String[] excludes = null;
        if (collectionReference.params.excludeWfsFields != null) {
            excludes = collectionReference.params.excludeWfsFields.split(",");
        }

        switch (requestType) {
            case GetCapabilities -> {
                GetCapabilitiesHandler getCapabilitiesHandler = wfsHandler.getCapabilitiesHandler;
                getCapabilitiesHandler.setFeatureTypeListType(collectionReference, serviceUrl);
                getCapabilitiesHandler.setOperationsUrl(serviceUrl);
                if (wfsHandler.inspireConfiguration.enabled) {
                    getCapabilitiesHandler.addINSPIRECompliantElements(collectionReference, serviceUrl, language);
                }
                JAXBElement<ExtendedWFSCapabilitiesType> getCapabilitiesResponse = ExtendedWFSCapabilitiesType.createWFSCapabilities(getCapabilitiesHandler.getCapabilitiesType);
                return Response
                        .ok(getCapabilitiesResponse)
                        .type(MediaType.APPLICATION_XML)

                        .build();
            }
            case DescribeFeatureType -> {
                StreamingOutput describeFeatureTypeResponse = wfsHandler.describeFeatureTypeHandler.getDescribeFeatureTypeResponse(collectionReferenceDescription, serviceUrl, Optional.ofNullable(columnFilter));
                return Response.ok(describeFeatureTypeResponse).type(MediaType.APPLICATION_XML).build();
            }
            case ListStoredQueries -> {
                wfsHandler.listStoredQueriesHandler.setFeatureType(featureQname);
                JAXBElement<ListStoredQueriesResponseType> listStoredQueriesResponse = wfsHandler.listStoredQueriesHandler.getListStoredQueriesResponse();
                return Response.ok(listStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();
            }
            case DescribeStoredQueries -> {
                DescribeStoredQueriesResponseType describeStoredQueriesType = wfsHandler.wfsFactory.createDescribeStoredQueriesResponseType();
                wfsHandler.storedQueryManager.listStoredQueries().forEach(storedQuery -> {
                    storedQuery.setFeatureType(featureQname);
                    describeStoredQueriesType.getStoredQueryDescription().add(storedQuery.getStoredQueryDescription());
                });
                JAXBElement<DescribeStoredQueriesResponseType> describeStoredQueriesResponse = wfsHandler.wfsFactory.createDescribeStoredQueriesResponse(describeStoredQueriesType);
                return Response.ok(describeStoredQueriesResponse).type(MediaType.APPLICATION_XML).build();
            }
            case GetFeature -> {
                WFSCheckParam.checkSrsName(srsname);
                StreamingOutput getFeatureResponse = null;
                if (storedquery_id != null) {
                    Map<String, Object> response = wfsToolService.getFeature(id, bbox, filter, resourceid, storedquery_id, partitionFilter, collectionReference, excludes, Optional.ofNullable(columnFilter));
                    getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureByIdResponse(response, collectionReferenceDescription, serviceUrl);
                } else {
                    List<Map<String, Object>> featureList = wfsToolService.getFeatures(id, bbox, filter, resourceid, partitionFilter, collectionReference, excludes, startindex, count, Optional.ofNullable(columnFilter));
                    getFeatureResponse = wfsHandler.getFeatureHandler.getFeatureResponse(wfsHandler.ogcConfiguration, collectionReferenceDescription, startindex, count, featureList, serviceUrl);
                }
                return Response.ok(getFeatureResponse).type(MediaType.APPLICATION_XML).build();
            }
            case GetPropertyValue -> {
                String include = OGCCheckParam.formatValueReference(valuereference, collectionReferenceDescription);
                ColumnFilterUtil.assertFieldAvailable(Optional.ofNullable(columnFilter), collectionReference, include);
                ValueCollectionType valueCollectionType = wfsToolService.getPropertyValue(id, bbox, filter, resourceid, storedquery_id, partitionFilter,
                        collectionReference, include, excludes, startindex, count, Optional.ofNullable(columnFilter));
                return Response.ok(wfsHandler.wfsFactory.createValueCollection(valueCollectionType)).type(MediaType.APPLICATION_XML).build();
            }
            default ->
                    throw new OGCException(OGCExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.", Service.WFS);
        }
    }
}
