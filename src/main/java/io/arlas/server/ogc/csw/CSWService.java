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
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.model.response.Error;
import io.arlas.server.ogc.common.utils.CSWConstant;
import io.arlas.server.ogc.common.utils.RequestUtils;
import io.arlas.server.ogc.common.utils.Version;
import io.arlas.server.ogc.common.utils.VersionUtils;
import io.arlas.server.ogc.csw.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import io.arlas.server.rest.collections.CollectionRESTServices;
import io.arlas.server.rest.explore.Documentation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.opengis.cat.csw._3.CapabilitiesType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;


public class CSWService extends CollectionRESTServices {


    public CSWHandler cswHandler;
    private OGCConfiguration ogcConfiguration;

    private String serverUrl;

    public CSWService(CSWHandler cswHandler) {

        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;
        this.serverUrl = ogcConfiguration.serverUri;

    }

    @Timed
    @Path("/csw")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @ApiOperation(
            value = "CSW",
            produces = MediaType.APPLICATION_XML,
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
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty
    ) throws OGCException {

        Version requestVersion = VersionUtils.getVersion(version);
        VersionUtils.checkVersion(requestVersion, CSWConstant.SUPPORTED_CSW_VERSION);
        RequestUtils.checkRequestTypeByName(request, CSWConstant.SUPPORTED_CSW_REQUESTYPE);
        CSWRequestType requestType = CSWRequestType.valueOf(request);

        switch (requestType) {
            case GetCapabilities:
                GetCapabilitiesHandler getCapabilitiesHandler = cswHandler.getCapabilitiesHandler;
                getCapabilitiesHandler.setOperationsUrl(serverUrl + "collections/csw/?");
                JAXBElement<CapabilitiesType> getCapabilitiesResponse = getCapabilitiesHandler.getCSWCapabilitiesResponse();
                return Response.ok(getCapabilitiesResponse).type(MediaType.APPLICATION_XML).build();
            case GetRecords:
                return null;
            case GetRecordById:
                return null;
            default:
                throw new OGCException(OGCExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.");
        }
    }
}
