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

package io.arlas.server.rest.collections;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.commons.rest.response.Error;
import io.arlas.commons.rest.response.Success;
import io.arlas.commons.rest.utils.ResponseFormatter;
import io.arlas.server.core.app.ArlasServerConfiguration;
import io.arlas.server.core.app.Documentation;
import io.arlas.server.core.exceptions.CollectionUnavailableException;
import io.arlas.server.core.model.*;
import io.arlas.server.core.services.CollectionReferenceService;
import io.arlas.server.core.utils.CheckParams;
import io.arlas.server.core.utils.ColumnFilterUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class CollectionService extends CollectionRESTServices {

    protected CollectionReferenceService collectionReferenceService;
    protected boolean inspireConfigurationEnabled;
    private static final String META_COLLECTION_NAME = "metacollection";

    public CollectionService(ArlasServerConfiguration configuration, CollectionReferenceService collectionReferenceService) throws ArlasException {
        super();
        this.collectionReferenceService = collectionReferenceService;
        this.inspireConfigurationEnabled = configuration.inspireConfiguration.enabled;
        collectionReferenceService.initCollectionDatabase();
    }

    @Timed
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Get all collection references",
            produces = UTF8JSON,
            notes = "Get all collection references in ARLAS",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response getAll(
            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty", value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        List<CollectionReference> collections = collectionReferenceService.getAllCollectionReferences(columnFilter, organisations);
        return ResponseFormatter.getResultResponse(collections);
    }

    @Timed
    @Path("/_export")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Get all collection references as a json file",
            produces = UTF8JSON,
            notes = "Get all collection references in ARLAS as json file",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response exportCollections(
            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations
            ) throws ArlasException {
        List<CollectionReference> collections = collectionReferenceService.getAllCollectionReferences(columnFilter, organisations);
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = "arlas-collections-export_" + date + ".json";
        removeMetacollection(collections);
        return ResponseFormatter.getFileResponse(collections, fileName);
    }

    @Timed
    @Path("/_import")
    @POST
    @Produces(UTF8JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            value = "Add collection references from a json file",
            produces = UTF8JSON,
            notes = "Add collection references in ARLAS from a json file",
            consumes = MediaType.MULTIPART_FORM_DATA
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response importCollections(
            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations,

            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws ArlasException {
        List<CollectionReference> collections = getCollectionsFromInputStream(inputStream);
        List<CollectionReference> savedCollections = new ArrayList<>();
        removeMetacollection(collections);
        Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(columnFilter);
        for (CollectionReference collection : collections) {
            collectionReferenceService.checkIfAllowedForOrganisations(collection, organisations, true);
            for (String c : allowedCollections) {
                if ((c.endsWith("*") && collection.collectionName.startsWith(c.substring(0, c.indexOf("*"))))
                        || collection.collectionName.equals(c)) {
                    try {
                        savedCollections.add(save(collection.collectionName, collection.params, true));
                    } catch (Exception e) {
                        throw new ArlasException(e.getMessage());
                    }
                } else {
                    throw new CollectionUnavailableException("Collection '" + collection.collectionName + "' not authorized by column filter");
                }
            }
        }
        return ResponseFormatter.getResultResponse(savedCollections);
    }

    // convert InputStream to List<CollectionReference>
    private static List<CollectionReference> getCollectionsFromInputStream(InputStream is) throws InvalidParameterException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        try {
            return Arrays.asList(mapper.readValue(is, CollectionReference[].class));
        } catch (IOException e) {
            throw new InvalidParameterException("Malformed json input file.");
        }
    }

    @Timed
    @Path("{collection}")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Get a collection reference",
            produces = UTF8JSON,
            notes = "Get a collection reference in ARLAS",
            consumes = UTF8JSON,
            response = CollectionReference.class

    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class),
            @ApiResponse(code = 404, message = "Collection not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response get(
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @ApiParam(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) Optional<String> columnFilter,

            @ApiParam(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) Optional<String> organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        CollectionReference cr = collectionReferenceService.getCollectionReference(collection, organisations);
        ColumnFilterUtil.assertCollectionsAllowed(columnFilter, List.of(cr));
        return ResponseFormatter.getResultResponse(cr);
    }

    @Timed
    @Path("{collection}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Add a collection reference",
            produces = UTF8JSON,
            notes = "Add a collection reference in ARLAS",
            consumes = UTF8JSON,
            response = CollectionReference.class
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = CollectionReference.class),
            @ApiResponse(code = 400, message = "JSON parameter malformed.", response = Error.class),
            @ApiResponse(code = 404, message = "Not Found Error.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})
    public Response put(
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            @ApiParam(name = "collectionParams",
                    value = "collectionParams",
                    required = true)
            @NotNull @Valid CollectionReferenceParameters collectionReferenceParameters,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty,

            @ApiParam(name = "checkfields", defaultValue = "true")
            @QueryParam(value = "checkfields") Boolean checkFields

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' is not allowed as a name for collections");
        }
        return ResponseFormatter.getResultResponse(save(collection, collectionReferenceParameters, checkFields == null ? Boolean.TRUE : checkFields));
    }

    public CollectionReference save(String collection, CollectionReferenceParameters collectionReferenceParameters, Boolean checkFields) throws ArlasException {
        CollectionReference collectionReference = new CollectionReference(collection, collectionReferenceParameters);
        setDefaultInspireParameters(collectionReference);
        if (inspireConfigurationEnabled) {
            CheckParams.checkMissingInspireParameters(collectionReference);
            CheckParams.checkInvalidDublinCoreElementsForInspire(collectionReference);
        }
        CheckParams.checkInvalidInspireParameters(collectionReference);
        return collectionReferenceService.putCollectionReference(collectionReference, checkFields);
    }

    @Timed
    @Path("{collection}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(
            value = "Delete a collection reference",
            produces = UTF8JSON,
            notes = "Delete a collection reference in ARLAS",
            consumes = UTF8JSON
    )
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful operation", response = Success.class),
            @ApiResponse(code = 404, message = "Collection not found.", response = Error.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class)})

    public Response delete(
            @ApiParam(name = "collection",
                    value = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @ApiParam(name = "pretty",
                    value = Documentation.FORM_PRETTY,
                    defaultValue = "false")
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("Forbidden operation on '" + META_COLLECTION_NAME + "'");
        }
        collectionReferenceService.deleteCollectionReference(collection);
        return ResponseFormatter.getSuccessResponse("Collection " + collection + " deleted.");
    }

    private void removeMetacollection(List<CollectionReference> collectionReferences) {
        if (collectionReferences != null) {
            collectionReferences.removeIf(collectionReference -> collectionReference.collectionName.equals(META_COLLECTION_NAME));
        }
    }

    private void setDefaultInspireParameters(CollectionReference collectionReference) {
        if (collectionReference.params.inspire == null) {
            collectionReference.params.inspire = new Inspire();
        }
        if (collectionReference.params.inspire.keywords == null ||collectionReference.params.inspire.keywords.size() == 0) {
            collectionReference.params.inspire.keywords = new ArrayList<>();
            Keyword k = new Keyword();
            k.value = collectionReference.collectionName;
            collectionReference.params.inspire.keywords.add(k);
        }
        if (collectionReference.params.inspire.inspireUseConditions == null || collectionReference.params.inspire.inspireUseConditions.equals("")) {
            collectionReference.params.inspire.inspireUseConditions = "no conditions apply";
        }
        if (collectionReference.params.inspire.inspireURI == null) {
            collectionReference.params.inspire.inspireURI = new InspireURI();
        }
        if (collectionReference.params.inspire.inspireURI.code == null || collectionReference.params.inspire.inspireURI.code.equals("")) {
            collectionReference.params.inspire.inspireURI.code = collectionReference.params.dublinCoreElementName.identifier;
        }
        if (collectionReference.params.inspire.inspireURI.namespace == null || collectionReference.params.inspire.inspireURI.namespace.equals("")) {
            collectionReference.params.inspire.inspireURI.namespace = "ARLAS." + collectionReference.collectionName.toUpperCase();
        }
        //a default language must be specified
        if (collectionReference.params.inspire.languages == null || collectionReference.params.inspire.languages.size() == 0) {
            collectionReference.params.inspire.languages = new ArrayList<>();
            collectionReference.params.inspire.languages.add("eng");
        }
    }
}
