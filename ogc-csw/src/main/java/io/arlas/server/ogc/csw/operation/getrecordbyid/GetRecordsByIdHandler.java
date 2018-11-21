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

package io.arlas.server.ogc.csw.operation.getrecordbyid;

import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import io.arlas.server.ogc.csw.utils.MDMetadataBuilder;
import io.arlas.server.ogc.csw.utils.RecordBuilder;
import net.opengis.cat.csw._3.*;
import org.isotc211._2005.gmd.MDMetadataType;

import java.util.List;

public class GetRecordsByIdHandler {

    public CSWHandler cswHandler;


    public GetRecordsByIdHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
    }

    public GetRecordByIdResponse getMDMetadaTypeResponse(List<CollectionReference> collections, ElementSetName elementSetName) throws OGCException {
        if (collections.size() > 0) {
            GetRecordByIdResponse getRecordByIdResponse = new GetRecordByIdResponse();
            switch (elementSetName) {
                case brief:
                    MDMetadataType briefMDMetadata = MDMetadataBuilder.getBriefMDMetadata(collections.get(0));
                    getRecordByIdResponse.getMdMetadata().add(briefMDMetadata);
                    break;

                case summary:
                    MDMetadataType summaryMDMetadata = MDMetadataBuilder.getSummaryMDMetadata(collections.get(0),cswHandler.ogcConfiguration, cswHandler.inspireConfiguration);
                    getRecordByIdResponse.getMdMetadata().add(summaryMDMetadata);
                    break;

                case full:
                    MDMetadataType fullMDMetadata = MDMetadataBuilder.getFullMDMetadata(collections.get(0),cswHandler.ogcConfiguration, cswHandler.inspireConfiguration);
                    getRecordByIdResponse.getMdMetadata().add(fullMDMetadata);
                    break;

                default:
                    MDMetadataType defaultMDMetadata = MDMetadataBuilder.getSummaryMDMetadata(collections.get(0),cswHandler.ogcConfiguration, cswHandler.inspireConfiguration);
                    getRecordByIdResponse.getMdMetadata().add(defaultMDMetadata);
                    break;
            }
            return getRecordByIdResponse;
        }else{
            throw new OGCException(OGCExceptionCode.NOT_FOUND, "Document not Found", "id", Service.CSW);
        }
    }

    public AbstractRecordType getAbstractRecordTypeResponse(List<CollectionReference> collections, ElementSetName elementSetName) throws OGCException {
        if (collections.size() > 0) {
            switch (elementSetName) {
                case brief:
                    BriefRecordType briefRecord = RecordBuilder.getBriefResult(collections.get(0), new String[]{}, cswHandler.inspireConfiguration.enabled);
                    return briefRecord;
                case summary:
                    SummaryRecordType summaryRecord = RecordBuilder.getSummaryResult(collections.get(0), new String[]{}, cswHandler.ogcConfiguration.serverUri,cswHandler.inspireConfiguration.enabled);
                    return summaryRecord;
                case full:
                    RecordType record = RecordBuilder.getFullResult(collections.get(0), new String[]{}, cswHandler.ogcConfiguration, cswHandler.inspireConfiguration.enabled);
                    return record;
                default:
                    SummaryRecordType summaryDefaultRecord = RecordBuilder.getSummaryResult(collections.get(0), new String[]{}, cswHandler.ogcConfiguration.serverUri, cswHandler.inspireConfiguration.enabled);
                    return summaryDefaultRecord;
            }
        }else{
            throw new OGCException(OGCExceptionCode.NOT_FOUND, "Document not Found", "id", Service.CSW);
        }
    }

}
