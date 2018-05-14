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

import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import io.arlas.server.ogc.csw.utils.RecordBuilder;
import net.opengis.cat.csw._3.AbstractRecordType;
import net.opengis.cat.csw._3.BriefRecordType;
import net.opengis.cat.csw._3.RecordType;
import net.opengis.cat.csw._3.SummaryRecordType;

import java.util.List;

public class GetRecordsByIdHandler {

    public CSWHandler cswHandler;


    public GetRecordsByIdHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
    }

    public AbstractRecordType getAbstractRecordTypeResponse(List<CollectionReference> collections, ElementSetName elementSetName) throws OGCException {
        if (collections.size() > 0) {
            switch (elementSetName) {
                case brief:
                    BriefRecordType briefRecord = RecordBuilder.getBriefResult(collections.get(0), new String[]{});
                    return briefRecord;
                case summary:
                    SummaryRecordType summaryRecord = RecordBuilder.getSummaryResult(collections.get(0), new String[]{});
                    return summaryRecord;
                case full:
                    RecordType record = RecordBuilder.getFullResult(collections.get(0), new String[]{});
                    return record;
                default:
                    SummaryRecordType summaryDefaultRecord = RecordBuilder.getSummaryResult(collections.get(0), new String[]{});
                    return summaryDefaultRecord;
            }
        }else{
            throw new OGCException(OGCExceptionCode.NOT_FOUND, "Document not Found", "id", Service.CSW);
        }
    }
}
