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

package io.arlas.server.rest.tag;

import com.codahale.metrics.annotation.Timed;
import io.arlas.server.app.Documentation;
import io.arlas.server.kafka.TagKafkaProducer;
import io.arlas.server.model.TagRefRequest;
import io.arlas.server.model.TaggingStatus;
import io.arlas.server.model.enumerations.Action;
import io.arlas.server.model.request.TagRequest;
import io.arlas.server.model.response.Error;
import io.arlas.server.model.response.UpdateResponse;
import io.arlas.server.services.UpdateServices;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static io.arlas.server.model.enumerations.Action.*;

@Path("/write")
@Api(value = "/write")

public class TagRESTService {
    protected static Logger LOGGER = LoggerFactory.getLogger(TagRESTService.class);
    public static final String UTF8JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    private TagKafkaProducer tagKafkaProducer;
    private Long statusTimeout;

    public TagRESTService(TagKafkaProducer tagKafkaProducer, Long statusTimeout) {
        this.tagKafkaProducer = tagKafkaProducer;
        this.statusTimeout = statusTimeout;
    }

    @Timed
    @Path("/{collection}/_tag")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Tag", produces = UTF8JSON, notes = Documentation.TAG_OPERATION, consumes = UTF8JSON, response = UpdateResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = UpdateResponse.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class) })
    public Response tagPost(
            // --------------------------------------------------------
            // ----------------------- PATH     -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- SEARCH   -----------------------
            // --------------------------------------------------------
            TagRequest tagRequest,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value="Partition-Filter") String partitionFilter,

            // --------------------------------------------------------
            // ----------------------- FORM     -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty
    ) {

        if (tagRequest.tag != null && tagRequest.tag.path != null && tagRequest.tag.value != null) {
            TagRefRequest tagRefRequest = TagRefRequest.fromTagRequest(tagRequest, collection, partitionFilter, ADD);
            tagKafkaProducer.sendToTagRefLog(tagRefRequest);
            UpdateResponse updateResponse = new UpdateResponse();
            updateResponse.id = tagRefRequest.id;
            TaggingStatus.getInstance().updateStatus(tagRefRequest.id, updateResponse, statusTimeout);
            return Response.ok(updateResponse).build();
        } else {
            throw new BadRequestException("Tag element is missing required data.");
        }
    }


    @Timed
    @Path("/{collection}/_untag")
    @POST
    @Produces(UTF8JSON)
    @Consumes(UTF8JSON)
    @ApiOperation(value = "Untag", produces = UTF8JSON, notes = Documentation.UNTAG_OPERATION, consumes = UTF8JSON, response = UpdateResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = UpdateResponse.class),
            @ApiResponse(code = 500, message = "Arlas Server Error.", response = Error.class), @ApiResponse(code = 400, message = "Bad request.", response = Error.class) })
    public Response untagPost(
            // --------------------------------------------------------
            // ----------------------- PATH     -----------------------
            // --------------------------------------------------------
            @ApiParam(
                    name = "collection",
                    value = "collection",
                    allowMultiple = false,
                    required = true)
            @PathParam(value = "collection") String collection,
            // --------------------------------------------------------
            // ----------------------- SEARCH   -----------------------
            // --------------------------------------------------------
            TagRequest tagRequest,

            // --------------------------------------------------------
            // -----------------------  FILTER  -----------------------
            // --------------------------------------------------------

            @ApiParam(hidden = true)
            @HeaderParam(value="Partition-Filter") String partitionFilter,

            // --------------------------------------------------------
            // ----------------------- FORM     -----------------------
            // --------------------------------------------------------
            @ApiParam(name ="pretty", value=Documentation.FORM_PRETTY,
                    allowMultiple = false,
                    defaultValue = "false",
                    required=false)
            @QueryParam(value="pretty") Boolean pretty
    ) {

        if (tagRequest.tag != null && tagRequest.tag.path != null) {
            TagRefRequest tagRefRequest = TagRefRequest.fromTagRequest(tagRequest, collection, partitionFilter,
                    tagRequest.tag.value != null ? REMOVE : REMOVEALL);
            UpdateResponse updateResponse = new UpdateResponse();
            updateResponse.id = tagRefRequest.id;
            tagKafkaProducer.sendToTagRefLog(tagRefRequest);

            return Response.ok(updateResponse).build();
        } else {
            throw new BadRequestException("Tag element is missing required data.");
        }
    }
}
