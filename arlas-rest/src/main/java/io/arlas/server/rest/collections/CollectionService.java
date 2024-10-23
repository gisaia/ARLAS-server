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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.arlas.commons.rest.utils.ServerConstants.ARLAS_ORGANISATION;
import static io.arlas.commons.rest.utils.ServerConstants.COLUMN_FILTER;

public class CollectionService extends CollectionRESTServices {

    protected CollectionReferenceService collectionReferenceService;
    protected ArlasServerConfiguration configuration;

    protected boolean inspireConfigurationEnabled;
    private static final String META_COLLECTION_NAME = "metacollection";

    public CollectionService(ArlasServerConfiguration configuration, CollectionReferenceService collectionReferenceService) throws ArlasException {
        super();
        this.configuration = configuration;
        this.collectionReferenceService = collectionReferenceService;
        this.inspireConfigurationEnabled = configuration.inspireConfiguration.enabled;
        collectionReferenceService.initCollectionDatabase();
    }

    @Timed
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Get all collection references",
            description = "Get all collection references in ARLAS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollectionReference.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    public Response getAll(
            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty", description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        List<CollectionReference> collections = collectionReferenceService
                .getAllCollectionReferences(Optional.ofNullable(columnFilter), Optional.ofNullable(organisations));
        return ResponseFormatter.getResultResponse(collections);
    }

    @Timed
    @Path("/_export")
    @GET
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Get all collection references as a json file",
            description = "Get all collection references in ARLAS as json file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CollectionReference.class)))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    public Response exportCollections(
            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations
            ) throws ArlasException {
        List<CollectionReference> collections = collectionReferenceService
                .getAllCollectionReferences(Optional.ofNullable(columnFilter), Optional.ofNullable(organisations));
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
    @Operation(
            summary = "Add collection references from a json file",
            description = "Add collection references in ARLAS from a json file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response importCollections(
            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            @FormDataParam("file") InputStream inputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail
    ) throws ArlasException {
        List<CollectionReference> collections = getCollectionsFromInputStream(inputStream);
        List<CollectionReference> savedCollections = new ArrayList<>();
        removeMetacollection(collections);
        Set<String> allowedCollections = ColumnFilterUtil.getAllowedCollections(Optional.ofNullable(columnFilter));
        for (CollectionReference collection : collections) {
            for (String c : allowedCollections) {
                if ((c.endsWith("*") && collection.collectionName.startsWith(c.substring(0, c.indexOf("*"))))
                        || collection.collectionName.equals(c)) {
                    try {
                        savedCollections.add(save(collection.collectionName, collection.params, true, organisations));
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
    @Operation(
            summary = "Get a collection reference",
            description = "Get a collection reference in ARLAS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    public Response get(
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty
    ) throws ArlasException {
        CollectionReference cr = collectionReferenceService.getCollectionReference(collection, Optional.ofNullable(organisations));
        ColumnFilterUtil.assertCollectionsAllowed(Optional.ofNullable(columnFilter), List.of(cr));
        return ResponseFormatter.getResultResponse(cr);
    }

    @Timed
    @Path("{collection}")
    @PUT
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Add a collection reference",
            description = "Add a collection reference in ARLAS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Not Found Error.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response put(
            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,

            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,
            @Parameter(name = "collectionParams",
                    description = "collectionParams",
                    required = true)
            @NotNull @Valid CollectionReferenceParameters collectionReferenceParameters,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty,

            @Parameter(name = "checkfields", schema = @Schema(defaultValue = "true"))
            @QueryParam(value = "checkfields") Boolean checkFields

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' is not allowed as a name for collections");
        }
        return ResponseFormatter.getResultResponse(save(collection, collectionReferenceParameters,
                checkFields == null ? Boolean.TRUE : checkFields, organisations));
    }

    @Timed
    @Path("{collection}/organisations")
    @PATCH
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Update a collection reference's organisations attribute.",
            description = "Update a collection reference's organisations attribute."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response patch(
            @Context HttpHeaders headers,
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "organisationsParamsUpdate",
                    description = "organisationsParamsUpdate",
                    required = true)
            @NotNull CollectionReferenceUpdateOrg opu,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' cannot be updated");
        }
        return ResponseFormatter.getResultResponse(collectionReferenceService.updateOrganisationsParamsCollectionReference(collection, organisations, columnFilter, opu.isPublic, opu.sharedWith));
    }


    @Timed
    @Path("{collection}/display_names/collection")
    @PATCH
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Update a collection reference's display collection name attribute.",
            description = "Update a collection reference's display collection name attribute."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response patchCollectionDisplayName(
            @Context HttpHeaders headers,
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "collectionDisplayName",
                    description = "collectionDisplayName",
                    required = true)
            @NotNull String collectionDisplayName,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' cannot be updated");
        }
        return ResponseFormatter.getResultResponse(collectionReferenceService.updateDisplayNamesCollectionReference(collection, organisations, columnFilter,collectionDisplayName, null, null ));
    }


    @Timed
    @Path("{collection}/display_names/fields")
    @PATCH
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Update a collection reference's display fields name attribute.",
            description = "Update a collection reference's display fields name attribute."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response patchFieldsDisplayNames(
            @Context HttpHeaders headers,
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "fieldsDisplayNames",
                    description = "fieldsDisplayNames",
                    required = true)
            @NotNull Map<String,String> fieldsDisplayNames,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' cannot be updated");
        }
        return ResponseFormatter.getResultResponse(collectionReferenceService.updateDisplayNamesCollectionReference(collection, organisations, columnFilter, null, fieldsDisplayNames, null ));
    }

    @Timed
    @Path("{collection}/display_names/shape_columns")
    @PATCH
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Update a collection reference's display shape columns name attribute.",
            description = "Update a collection reference's display shape columns name attribute."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = CollectionReference.class))),
            @ApiResponse(responseCode = "400", description = "JSON parameter malformed.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })
    public Response patchShapeColumnsDisplayNames(
            @Context HttpHeaders headers,
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            @Parameter(name = "shapeColumnsDisplayNames",
                    description = "shapeColumnsDisplayNames",
                    required = true)
            @NotNull Map<String,String> shapeColumnsDisplayNames,

            @Parameter(hidden = true)
            @HeaderParam(value = COLUMN_FILTER) String columnFilter,

            @Parameter(hidden = true)
            @HeaderParam(value = ARLAS_ORGANISATION) String organisations,
            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
            @QueryParam(value = "pretty") Boolean pretty

    ) throws ArlasException {
        if (collection != null && collection.equals(META_COLLECTION_NAME)) {
            throw new NotAllowedException("'" + META_COLLECTION_NAME + "' cannot be updated");
        }
        return ResponseFormatter.getResultResponse(collectionReferenceService.updateDisplayNamesCollectionReference(collection, organisations, columnFilter,null,null,shapeColumnsDisplayNames ));
    }

    public CollectionReference save(String collection, CollectionReferenceParameters collectionReferenceParameters,
                                    Boolean checkFields, String organisations) throws ArlasException {
        CollectionReference collectionReference = new CollectionReference(collection, collectionReferenceParameters);
        setDefaultInspireParameters(collectionReference);
        if (inspireConfigurationEnabled) {
            CheckParams.checkMissingInspireParameters(collectionReference);
            CheckParams.checkInvalidDublinCoreElementsForInspire(collectionReference);
        }
        CheckParams.checkInvalidInspireParameters(collectionReference);
        collectionReferenceService.checkIfAllowedForOrganisations(collectionReference, Optional.ofNullable(organisations), true);
        collectionReferenceService.checkIfIndexAllowedForOrganisations(collectionReference, Optional.ofNullable(organisations), Optional.ofNullable(configuration.arlasAuthPolicyClass));
        return collectionReferenceService.putCollectionReference(collectionReference, checkFields);
    }

    @Timed
    @Path("{collection}")
    @DELETE
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @Operation(
            summary = "Delete a collection reference",
            description = "Delete a collection reference in ARLAS"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation",
                    content = @Content(schema = @Schema(implementation = Success.class))),
            @ApiResponse(responseCode = "404", description = "Collection not found.",
                    content = @Content(schema = @Schema(implementation = Error.class))),
            @ApiResponse(responseCode = "500", description = "Arlas Server Error.",
                    content = @Content(schema = @Schema(implementation = Error.class)))
    })

    public Response delete(
            @Parameter(name = "collection",
                    description = "collection",
                    required = true)
            @PathParam(value = "collection") String collection,

            // --------------------------------------------------------
            // ----------------------- FORM -----------------------
            // --------------------------------------------------------
            @Parameter(name = "pretty",
                    description = Documentation.FORM_PRETTY,
                    schema = @Schema(defaultValue = "false"))
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
        if (collectionReference.params.inspire.keywords == null || collectionReference.params.inspire.keywords.isEmpty()) {
            collectionReference.params.inspire.keywords = new ArrayList<>();
            Keyword k = new Keyword();
            k.value = collectionReference.collectionName;
            collectionReference.params.inspire.keywords.add(k);
        }
        if (collectionReference.params.inspire.inspireUseConditions == null || collectionReference.params.inspire.inspireUseConditions.isEmpty()) {
            collectionReference.params.inspire.inspireUseConditions = "no conditions apply";
        }
        if (collectionReference.params.inspire.inspireURI == null) {
            collectionReference.params.inspire.inspireURI = new InspireURI();
        }
        if (collectionReference.params.inspire.inspireURI.code == null || collectionReference.params.inspire.inspireURI.code.isEmpty()) {
            collectionReference.params.inspire.inspireURI.code = collectionReference.params.dublinCoreElementName.identifier;
        }
        if (collectionReference.params.inspire.inspireURI.namespace == null || collectionReference.params.inspire.inspireURI.namespace.isEmpty()) {
            collectionReference.params.inspire.inspireURI.namespace = "ARLAS." + collectionReference.collectionName.toUpperCase();
        }
        //a default language must be specified
        if (collectionReference.params.inspire.languages == null || collectionReference.params.inspire.languages.isEmpty()) {
            collectionReference.params.inspire.languages = new ArrayList<>();
            collectionReference.params.inspire.languages.add("eng");
        }
    }
}
