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

import com.a9.opensearch.OpenSearchDescription;
import com.codahale.metrics.annotation.Timed;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.rest.response.Error;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.CollectionReferences;
import io.arlas.server.core.ns.ATOM;
import io.arlas.server.core.services.FluidSearchService;
import io.arlas.server.core.utils.BoundingBox;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.arlas.server.ogc.common.OGCRESTService;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.common.utils.RequestUtils;
import io.arlas.server.ogc.csw.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.ogc.csw.operation.getrecordbyid.GetRecordByIdResponse;
import io.arlas.server.ogc.csw.operation.getrecordbyid.GetRecordsByIdHandler;
import io.arlas.server.ogc.csw.operation.getrecords.GetRecordsHandler;
import io.arlas.server.ogc.csw.operation.opensearch.OpenSearchHandler;
import io.arlas.server.ogc.csw.utils.CSWCheckParam;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.opengis.cat.csw._3.AbstractRecordType;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.cat.csw._3.GetRecordsResponseType;
import org.apache.commons.collections4.CollectionUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;
import static io.arlas.server.core.utils.CheckParams.isBboxLatLonInCorrectRanges;

@Path("/ogc")
@Tag(name="ogc", description="OGC API")
public class CSWRESTService extends OGCRESTService {

    public static final String MIME_TYPE__OPENSEARCH_XML = "application/opensearchdescription+xml";

    public CSWHandler cswHandler;

    private final String serverBaseUri;

    public CSWRESTService(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
        this.serverBaseUri = cswHandler.baseUri;
    }

    @Timed
    @Path("/csw")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, ATOM.APPLICATION_ATOM_XML, MIME_TYPE__OPENSEARCH_XML})
    @Operation(
            summary = "CSW",
            description = "CSW"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response doKVP(
            @Parameter(
                    name = "version",
                    description = "version",
                    required = true)
            @QueryParam(value = "version") String version,
            @Parameter(
                    name = "acceptversions",
                    description = "acceptversions",
                    required = true)
            @QueryParam(value = "acceptversions") String acceptVersions,
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
                    name = "elementname",
                    description = "elementname",
                    required = true)
            @QueryParam(value = "elementname") String elementName,
            @Parameter(
                    name = "elementsetname",
                    description = "elementsetname",
                    required = true)
            @QueryParam(value = "elementsetname") String elementSetName,
            @Parameter(
                    name = "filter",
                    description = "filter",
                    required = true)
            @QueryParam(value = "filter") String filter,

            @Parameter(
                    name = "constraint",
                    description = "constraint",
                    required = true)
            @QueryParam(value = "constraint") String constraint,
            @Parameter(
                    name = "constraintLanguage",
                    description = "constraintLanguage",
                    required = true)
            @QueryParam(value = "constraintLanguage") String constraintLanguage,
            @Parameter(
                    name = "startposition",
                    description = "startposition")
            @QueryParam(value = "startposition") Integer startPosition,
            @Parameter(
                    name = "maxrecords",
                    description = "maxrecords")
            @QueryParam(value = "maxrecords") Integer maxRecords,
            @Parameter(
                    name = "sections",
                    description = "sections")
            @QueryParam(value = "sections") String sections,
            @Parameter(
                    name = "acceptformats",
                    description = "acceptformats")
            @QueryParam(value = "acceptformats") String acceptFormats,
            @Parameter(
                    name = "q",
                    description = "q")
            @QueryParam(value = "q") String query,
            @Parameter(
                    name = "bbox",
                    description = "bbox")
            @QueryParam(value = "bbox") String bbox,
            @Parameter(
                    name = "outputformat",
                    description = "outputformat")
            @QueryParam(value = "outputformat") String outputFormat,
            @Parameter(
                    name = "outputschema",
                    description = "outputschema")
            @QueryParam(value = "outputschema") String outputSchema,
            @Parameter(
                    name = "typenames",
                    description = "typenames")
            @QueryParam(value = "typenames") String typeNames,
            @Parameter(
                    name = "recordids",
                    description = "recordids")
            @QueryParam(value = "recordids") String recordIds,
            @Parameter(
                    name = "id",
                    description = "id")
            @QueryParam(value = "id") String id,
            @Parameter(
                    name = "language",
                    description = "language")
            @QueryParam(value = "language") String language,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,


            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,
            @Context HttpHeaders headers
    ) throws ArlasException, DatatypeConfigurationException, IOException {
        String acceptFormatMediaType = MediaType.APPLICATION_XML;
        String outputFormatMediaType = MediaType.APPLICATION_XML;
        for (MediaType mediaType : headers.getAcceptableMediaTypes()) {
            if (mediaType.getSubtype().contains("opensearchdescription")) {
                OpenSearchHandler openSearchHandler = cswHandler.openSearchHandler;
                OpenSearchDescription description = openSearchHandler.getOpenSearchDescription(serverBaseUri);
                return Response.ok(description).build();
            } else if (mediaType.getSubtype().contains("atom")) {
                outputFormatMediaType = MediaType.APPLICATION_ATOM_XML;
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
            for (String section : sectionList) {
                if (!Arrays.asList(CSWConstant.SECTION_NAMES).contains(section)) {
                    throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid sections", "sections", Service.CSW);
                }
            }
        }
        if (acceptFormats != null) {
            if (acceptFormats.equals("text/xml")) {
                acceptFormatMediaType = MediaType.TEXT_XML;
            } else if (acceptFormats.equals("application/xml")) {
                acceptFormatMediaType = MediaType.APPLICATION_XML;
            } else {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid acceptFormats", "acceptFormats", Service.CSW);
            }
        }

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
        CSWRequestType requestType = CSWRequestType.valueOf(request);
        CSWCheckParam.checkQuerySyntax(requestType, elementName, elementSetName, acceptVersions, version, service, outputSchema, typeNames, bbox, recordIds, query, id, constraintLanguage);

        String[] ids = null;
        if (recordIds != null && recordIds.length() > 0) {
            ids = recordIds.split(",");
        } else if (id != null) {
            ids = new String[]{id};
        }
        BoundingBox boundingBox = null;
        if (bbox != null && bbox.length() > 0) {
            // west, south, east, north CSW spec
            double[] bboxList = GeoFormat.toDoubles(bbox, Service.CSW);
            if (!(isBboxLatLonInCorrectRanges(bboxList) && bboxList[3] > bboxList[1]) && bboxList[0] != bboxList[2]) {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, FluidSearchService.INVALID_BBOX, "bbox", Service.CSW);
            }
            boundingBox = new BoundingBox(bboxList[3], bboxList[1], bboxList[0], bboxList[2]);
        }
        startPosition = Optional.ofNullable(startPosition).orElse(1);
        maxRecords = Optional.ofNullable(maxRecords).orElse(cswHandler.ogcConfiguration.queryMaxFeature.intValue());
        elementSetName = Optional.ofNullable(elementSetName).orElse("summary");
        String[] elements = new String[]{};

        if (elementName != null) {
            elements = new String[elementName.split(",").length];
            int i = 0;
            for (String element : elementName.split(",")) {
                if (element.contains(":")) {
                    elements[i] = elementName.split(":")[1];
                    element = elements[i];
                } else {
                    elements[i] = element;
                }
                if (!Arrays.asList(CSWConstant.DC_FIELDS).contains(element.toLowerCase()))
                    throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid elementName", "elementName", Service.CSW);
                i++;
            }
        }

        List<CollectionReference> collections;

        switch (requestType) {
            case GetCapabilities -> {
                GetCapabilitiesHandler getCapabilitiesHandler = cswHandler.getCapabilitiesHandler;
                List<String> responseSections = Arrays.asList(sectionList);
                String serviceUrl = serverBaseUri + "ogc/csw/?";
                getCapabilitiesHandler.setCapabilitiesType(responseSections, serviceUrl, serverBaseUri + "ogc/csw/opensearch");
                if (cswHandler.inspireConfiguration.enabled) {
                    collections = collectionReferenceService.getAllCollectionReferences(Optional.ofNullable(columnFilter), Optional.ofNullable(organisations));

                    collections.removeIf(collectionReference -> collectionReference.collectionName.equals(getMetacollectionName()));
                    filterCollectionsByColumnFilter(columnFilter, collections);

                    if (CollectionUtils.isNotEmpty(collections)) {
                        getCapabilitiesHandler.addINSPIRECompliantElements(collections, responseSections, serviceUrl, language);
                    }
                }
                JAXBElement<CapabilitiesType> getCapabilitiesResponse = getCapabilitiesHandler.getCSWCapabilitiesResponse();
                return Response.ok(getCapabilitiesResponse).type(acceptFormatMediaType).build();
            }
            case GetRecords -> {
                GetRecordsHandler getRecordsHandler = cswHandler.getRecordsHandler;
                CollectionReferences collectionReferences = getCollectionReferencesForGetRecords(elements, null, maxRecords, startPosition, ids, query, constraint, boundingBox);
                collections = new ArrayList<>(collectionReferences.collectionReferences);
                filterCollectionsByColumnFilter(columnFilter, collections);
                long recordsMatched = collectionReferences.totalCollectionReferences;
                if (recordIds != null && recordIds.length() > 0) {
                    if (collections.size() == 0) {
                        throw new OGCException(OGCExceptionCode.NOT_FOUND, "Document not Found", "id", Service.CSW);
                    }
                }
                GetRecordsResponseType getRecordsResponse = getRecordsHandler.getCSWGetRecordsResponse(collections,
                        ElementSetName.valueOf(elementSetName), startPosition - 1, recordsMatched, elements, outputSchema);
                return Response.ok(getRecordsResponse).type(outputFormatMediaType).build();
            }
            case GetRecordById -> {
                GetRecordsByIdHandler getRecordsByIdHandler = cswHandler.getRecordsByIdHandler;
                CollectionReferences recordCollectionReferences = ogcDao.getCollectionReferences(elements, null, maxRecords, startPosition - 1, ids, query, constraint, boundingBox);
                collections = new ArrayList<>(recordCollectionReferences.collectionReferences);
                ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), collections);
                if (outputSchema != null && outputSchema.equals(CSWConstant.SUPPORTED_CSW_OUTPUT_SCHEMA[2])) {
                    GetRecordByIdResponse getRecordByIdResponse = getRecordsByIdHandler.getMDMetadaTypeResponse(collections, ElementSetName.valueOf(elementSetName));
                    return Response.ok(getRecordByIdResponse).type(outputFormatMediaType).build();
                } else {
                    AbstractRecordType abstractRecordType = getRecordsByIdHandler.getAbstractRecordTypeResponse(collections, ElementSetName.valueOf(elementSetName));
                    return Response.ok(abstractRecordType).type(outputFormatMediaType).build();
                }
            }
            default ->
                    throw new OGCException(OGCExceptionCode.INTERNAL_SERVER_ERROR, "Internal error: Unhandled request '" + request + "'.", Service.CSW);
        }
    }

    private void filterCollectionsByColumnFilter(
            @HeaderParam(COLUMN_FILTER)
            @Parameter(hidden = true) String columnFilter,

            List<CollectionReference> collections
    ) throws CollectionUnavailableException {
        ColumnFilterUtil.cleanColumnFilter(Optional.ofNullable(columnFilter));
        collections.removeIf(collection ->
        {
            try {
                return ColumnFilterUtil.cleanColumnFilter(Optional.ofNullable(columnFilter)).isPresent()
                        && ColumnFilterUtil.getCollectionRelatedColumnFilter(Optional.ofNullable(columnFilter), collection).isEmpty();
            } catch (CollectionUnavailableException ignored) {
                // already checked with the first line of this method
                return true;
            }
        });
    }


    @Timed
    @Path("/csw/opensearch")
    @GET
    @Produces({MediaType.APPLICATION_XML, MIME_TYPE__OPENSEARCH_XML})
    @Operation(
            summary = "OpenSearch CSW Description Document",
            description = Documentation.OPENSEARCH_CSW_OPERATION
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response opensearch(
            // --------------------------------------------------------
            // -----------------------  EXTRA   -----------------------
            // --------------------------------------------------------
            @Parameter(description = "max-age-cache", required = false)
            @QueryParam(value = "max-age-cache") Integer maxagecache
    ) {
        OpenSearchHandler openSearchHandler = cswHandler.openSearchHandler;
        OpenSearchDescription description = openSearchHandler.getOpenSearchDescription(serverBaseUri);
        return Response.ok(description).build();
    }

    private CollectionReferences getCollectionReferencesForGetRecords(String[] elements, String[]
            excludes, int maxRecords, int startPosition, String[] ids, String q, String constraint, BoundingBox boundingBox) throws IOException, ArlasException {
        CollectionReference metacollection = collectionReferenceService.getCollectionReference(getMetacollectionName(), Optional.empty());

        // First we check if there is only "metacollection" that is returned. If this is the case, it means that the queried param is a config param. Thus all collections should be returned
        CollectionReferences collectionReferences = ogcDao.getCollectionReferences(elements, null, 2, startPosition - 1, ids, q, constraint, boundingBox);

        if (collectionReferences.totalCollectionReferences == 1 && collectionReferences.collectionReferences.size() == 1 && collectionReferences.collectionReferences.get(0).collectionName.equals(getMetacollectionName())) {
            return ogcDao.getAllCollectionReferencesExceptOne(elements, excludes, maxRecords, startPosition - 1,  metacollection);
        } else {
            return ogcDao.getCollectionReferencesExceptOne(elements, excludes, maxRecords, startPosition - 1, ids, q, constraint, boundingBox, metacollection);
        }
    }
}
