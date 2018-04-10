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

package io.arlas.server.ogc.csw;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.Error;
import io.arlas.server.ns.ATOM;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.RequestUtils;
import io.arlas.server.ogc.csw.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.ogc.csw.operation.getrecords.GetRecordsHandler;
import io.arlas.server.ogc.csw.utils.CSWCheckParam;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import io.arlas.server.rest.collections.CollectionRESTServices;
import io.arlas.server.rest.explore.Documentation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.cat.csw._3.GetRecordsResponseType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class CSWService extends CollectionRESTServices {

    protected CollectionReferenceDao dao = null;


    public CSWHandler cswHandler;
    private OGCConfiguration ogcConfiguration;

    private String serverUrl;

    public CSWService(CSWHandler cswHandler, ArlasServerConfiguration configuration) {

        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;
        this.serverUrl = ogcConfiguration.serverUri;

    }

    @Timed
    @Path("/csw")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, ATOM.APPLICATION_ATOM_XML})
    @ApiOperation(
            value = "CSW",

            produces = MediaType.APPLICATION_XML + "," + MediaType.TEXT_XML + "," + ATOM.APPLICATION_ATOM_XML,
            notes = "CSW"
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response doKVP(
            @ApiParam(
                    name = "version",
                    value = "version",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "version") String version,
            @ApiParam(
                    name = "acceptversions",
                    value = "acceptversions",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "acceptversions") String acceptVersions,
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
                    name = "elementname",
                    value = "elementname",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "elementname") String elementName,
            @ApiParam(
                    name = "elementsetname",
                    value = "elementsetname",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "elementsetname") String elementSetName,
            @ApiParam(
                    name = "filter",
                    value = "filter",
                    allowMultiple = false,
                    required = true)
            @QueryParam(value = "filter") String filter,
            @ApiParam(
                    name = "startposition",
                    value = "startposition",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "startposition") Integer startPosition,
            @ApiParam(
                    name = "maxrecords",
                    value = "maxrecords",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "maxrecords") Integer maxRecords,
            @ApiParam(
                    name = "sections",
                    value = "sections",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "sections") String sections,
            @ApiParam(
                    name = "acceptformats",
                    value = "acceptformats",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "acceptformats") String acceptFormats,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {

        if (request == null & version == null & service == null) {
            request = "GetCapabilities";
            version = CSWConstant.SUPPORTED_CSW_VERSION;
            service = CSWConstant.CSW;
        }
        String[] sectionList;
        if (sections == null) {
            sectionList = new String[]{"All"};
        } else {
            sectionList = sections.split(",");
            for (String section : Arrays.asList(sectionList)) {
                if (!Arrays.asList(CSWConstant.SECTION_NAMES).contains(section)) {
                    throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid sections", "sections", Service.CSW);
                }
            }
        }
        String mediaType = MediaType.APPLICATION_XML;
        if (acceptFormats != null) {
            if (acceptFormats.equals("text/xml")) {
                mediaType = MediaType.TEXT_XML;
            } else if (acceptFormats.equals("application/xml")) {
                mediaType = MediaType.APPLICATION_XML;
            } else {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid acceptFormats", "acceptFormats", Service.CSW);
            }
        }
        RequestUtils.checkRequestTypeByName(request, CSWConstant.SUPPORTED_CSW_REQUESTYPE, Service.CSW);
        CSWCheckParam.checkQuerySyntax(elementName, elementSetName, acceptVersions, version, service);
        CSWRequestType requestType = CSWRequestType.valueOf(request);

        startPosition = Optional.ofNullable(startPosition).orElse(0);
        maxRecords = Optional.ofNullable(maxRecords).orElse(ogcConfiguration.queryMaxFeature.intValue());

        switch (requestType) {
            case GetCapabilities:
                GetCapabilitiesHandler getCapabilitiesHandler = cswHandler.getCapabilitiesHandler;
                JAXBElement<CapabilitiesType> getCapabilitiesResponse = getCapabilitiesHandler.getCSWCapabilitiesResponse(Arrays.asList(sectionList), serverUrl + "collections/csw/?");
                return Response.ok(getCapabilitiesResponse).type(mediaType).build();
            case GetRecords:
                GetRecordsHandler getRecordsHandler = cswHandler.getRecordsHandler;
                List<CollectionReference> collections = dao.getCollectionReferences(null, null, maxRecords, startPosition);
                JAXBElement<GetRecordsResponseType> getRecordsResponse = getRecordsHandler.getCSWGetRecordsResponse(collections,
                        ElementSetName.valueOf(elementSetName), startPosition, maxRecords);
                return Response.ok(getRecordsResponse).type(mediaType).build();
            case GetRecordById:
                return Response.ok("").type(mediaType).build();
            default:
                throw new OGCException(OGCExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.", Service.CSW);
        }
    }
}
