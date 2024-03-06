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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.xml.bind.JAXBElement;
import net.opengis.wfs._2.DescribeStoredQueriesResponseType;
import net.opengis.wfs._2.ListStoredQueriesResponseType;
import net.opengis.wfs._2.ValueCollectionType;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.*;

@Path("/ogc/wfs")
@Tag(name="ogc/wfs", description="OGC WFS API")
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
    @Operation(
            summary = "WFS",
            description = "WFS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response doKVP(
            @Parameter(
                    name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(
                    name = "version",
                    description = "version",
                    required = true)
            @QueryParam(value = "version") String version,
            @Parameter(
                    name = "service",
                    description = "service",
                    required = true)
            @QueryParam(value = "service") String service,
            @Parameter(
                    name = "request",
                    description = "request",
                    required = true)
            @QueryParam(value = "request") String request,
            @Parameter(
                    name = "storedquery_id",
                    description = "storedquery_id")
            @QueryParam(value = "storedquery_id") String storedquery_id,
            @Parameter(
                    name = "id",
                    description = "id")
            @QueryParam(value = "id") String id,
            @Parameter(
                    name = "typenames",
                    description = "typenames")
            @QueryParam(value = "typenames") String typenames,
            @Parameter(
                    name = "startindex",
                    description = "startindex")
            @QueryParam(value = "startindex") Integer startindex,
            @Parameter(
                    name = "count",
                    description = "count")
            @QueryParam(value = "count") Integer count,
            @Parameter(
                    name = "valuereference",
                    description = "valuereference")
            @QueryParam(value = "valuereference") String valuereference,
            @Parameter(
                    name = "filter",
                    description = "filter")
            @QueryParam(value = "filter") String filter,
            @Parameter(
                    name = "resourceid",
                    description = "resourceid")
            @QueryParam(value = "resourceid") String resourceid,
            @Parameter(
                    name = "srsname",
                    description = "srsname")
            @QueryParam(value = "srsname") String srsname,
            @Parameter(
                    name = "bbox",
                    description = "bbox")
            @QueryParam(value = "bbox") String bbox,
            @Parameter(
                    name = "language",
                    description = "language")
            @QueryParam(value = "language") String language,

            //header filters
            @Parameter(hidden = true)
            @HeaderParam(value = PARTITION_FILTER) String partitionFilter,
            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,
            @Parameter(hidden = true)
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
