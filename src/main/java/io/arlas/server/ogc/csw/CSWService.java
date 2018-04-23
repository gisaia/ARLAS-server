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
import io.arlas.server.app.CSWConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.dao.CollectionReferenceDao;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.model.Feed;
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
import io.arlas.server.rest.explore.opensearch.model.OpenSearchDescription;
import io.arlas.server.rest.explore.opensearch.model.Url;
import io.arlas.server.utils.BoundingBox;
import io.arlas.server.utils.CheckParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.cat.csw._3.GetDomainResponseType;
import net.opengis.cat.csw._3.GetRecordsResponseType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.ObjectFactory;


import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class CSWService extends CollectionRESTServices {

    protected CollectionReferenceDao dao = null;
    public static final String MIME_TYPE__OPENSEARCH_XML = "application/opensearchdescription+xml";


    public CSWHandler cswHandler;
    private OGCConfiguration ogcConfiguration;
    private CSWConfiguration cswConfiguration;

    private String serverUrl;

    public CSWService(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;
        this.cswConfiguration = cswHandler.cswConfiguration;
        this.serverUrl = ogcConfiguration.serverUri;
    }
    @Context
    UriInfo uri;
    @Timed
    @Path("/csw")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, ATOM.APPLICATION_ATOM_XML,MIME_TYPE__OPENSEARCH_XML})
    @ApiOperation(
            value = "CSW",

            produces = MediaType.APPLICATION_XML + "," + MediaType.TEXT_XML + "," + ATOM.APPLICATION_ATOM_XML +","+MIME_TYPE__OPENSEARCH_XML,
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
            @ApiParam(
                    name = "q",
                    value = "q",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "q") String query,
            @ApiParam(
                    name = "bbox",
                    value = "bbox",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "bbox") String bbox,
            @ApiParam(
                    name = "outputformat",
                    value = "outputformat",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "outputformat") String outputFormat,
            @ApiParam(
                    name = "outputschema",
                    value = "outputschema",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "outputschema") String outputSchema,
            @ApiParam(
                    name = "typenames",
                    value = "typenames",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "typenames") String typeNames,
            @ApiParam(
                    name = "recordids",
                    value = "recordids",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "recordids") String recordIds,
            @ApiParam(
                    name = "id",
                    value = "id",
                    allowMultiple = false,
                    required = false)
            @QueryParam(value = "id") String id,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required = false)
            @QueryParam(value = "pretty") Boolean pretty,
            @Context HttpHeaders headers
    ) throws ArlasException, DatatypeConfigurationException {
        LOGGER.info(uri.getRequestUri().toString());
        for(MediaType mediaType:headers.getAcceptableMediaTypes()){
            if(mediaType.getSubtype().contains("opensearchdescription")){
                OpenSearchDescription description = new OpenSearchDescription();
                description.description=cswConfiguration.openSearchDescription;
                description.shortName=cswConfiguration.openSearchShortName;
                Url url= new Url();
                url.type = MediaType.APPLICATION_XML;
                url.template = serverUrl + "collections/csw/?"+"request=GetRecords&service=CSW\n" +
                        "&version=3.0&q={searchTerms}&maxRecords={count}\n" +
                        "&startPosition={startIndex}&bbox={geo:box}\n" +
                        "&outputFormat=application/xml";
                description.url = new ArrayList<Url>();
                description.url.add(url);
                return Response.ok(description).build();
            }
        }
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
        String acceptFormatMediaType = MediaType.APPLICATION_XML;
        if (acceptFormats != null) {
            if (acceptFormats.equals("text/xml")) {
                acceptFormatMediaType = MediaType.TEXT_XML;
            } else if (acceptFormats.equals("application/xml")) {
                acceptFormatMediaType = MediaType.APPLICATION_XML;
            } else {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid acceptFormats", "acceptFormats", Service.CSW);
            }
        }

        String outputFormatMediaType = MediaType.APPLICATION_XML;
        if (outputFormat != null) {
            if (outputFormat.equals("application/xml")) {
                outputFormatMediaType = MediaType.APPLICATION_XML;
            } else if (outputFormat.equals("application/atom+xml")) {
                outputFormatMediaType = MediaType.APPLICATION_ATOM_XML;
            } else {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid outputFormat", "outputFormat", Service.CSW);
            }
        }

        RequestUtils.checkRequestTypeByName(request, CSWConstant.SUPPORTED_CSW_REQUESTYPE, Service.CSW);
        CSWCheckParam.checkQuerySyntax(elementName, elementSetName, acceptVersions, version, service, outputSchema,typeNames,bbox,recordIds,query);
        CSWRequestType requestType = CSWRequestType.valueOf(request);

        String[] ids = null;
        if(recordIds!=null){
            ids = recordIds.split(",");
        }else if(id!=null){
            ids=new String[]{id};
        }
        BoundingBox boundingBox = null;
        if(bbox!=null){
            double[] bboxList = CheckParams.toDoubles(bbox);
            // west, south, east, north
            boundingBox = new BoundingBox(bboxList[3],bboxList[1],bboxList[0],bboxList[2]);
        }
        startPosition = Optional.ofNullable(startPosition).orElse(0);
        maxRecords = Optional.ofNullable(maxRecords).orElse(ogcConfiguration.queryMaxFeature.intValue());
        elementSetName = Optional.ofNullable(elementSetName).orElse("summary");
        String[] elements = new String[]{};

        if(elementName!=null){
            elements = new String[elementName.split(",").length];
            int i = 0;
            for(String element : elementName.split(",") ){
                if(element.contains(":")){
                    elements[i]=elementName.split(":")[1];
                    element=elements[i];
                }else{
                    elements[i]=element;
                }
                if(!Arrays.asList(CSWConstant.DC_FIELDS).contains(element.toLowerCase()))
                        throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid elementName", "elementName", Service.CSW);
                i++;
            }
        }

        switch (requestType) {
            case GetCapabilities:
                GetCapabilitiesHandler getCapabilitiesHandler = cswHandler.getCapabilitiesHandler;
                JAXBElement<CapabilitiesType> getCapabilitiesResponse = getCapabilitiesHandler.getCSWCapabilitiesResponse(Arrays.asList(sectionList),
                        serverUrl + "collections/csw/?",serverUrl+"collections/csw/_opensearch");
                return Response.ok(getCapabilitiesResponse).type(acceptFormatMediaType).build();
            case GetRecords:
            case GetRecordById:
                GetRecordsHandler getRecordsHandler = cswHandler.getRecordsHandler;
                List<CollectionReference> collections = dao.getCollectionReferences(elements, null,
                        maxRecords, startPosition,ids);
                GetRecordsResponseType getRecordsResponse = getRecordsHandler.getCSWGetRecordsResponse(collections,
                        ElementSetName.valueOf(elementSetName), startPosition, maxRecords,elements);
                return Response.ok(getRecordsResponse).type(outputFormatMediaType).build();
            default:
                throw new OGCException(OGCExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.", Service.CSW);
        }
    }
    @Timed
    @Path("/csw/_opensearch")
    @GET
    @Produces({MIME_TYPE__OPENSEARCH_XML})
    @ApiOperation(value = "OpenSearch CSW Description Document", produces =MIME_TYPE__OPENSEARCH_XML, notes = Documentation.OPENSEARCH_CSW_OPERATION)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class)})
    public Response opensearch(
            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @ApiParam(value = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) throws ArlasException {
        OpenSearchDescription description = new OpenSearchDescription();
        description.description=cswConfiguration.openSearchDescription;
        description.shortName=cswConfiguration.openSearchShortName;
        Url url= new Url();
        url.type = MediaType.APPLICATION_XML;
        url.template = serverUrl + "collections/csw/?"+"request=GetRecords&service=CSW\n" +
                "&version=3.0&q={searchTerms}&maxRecords={count}\n" +
                "&startPosition={startIndex}&bbox={geo:box}\n" +
                "&outputFormat=application/xml";
        description.url = new ArrayList<Url>();
        description.url.add(url);
        return Response.ok(description).build();
    }
}
