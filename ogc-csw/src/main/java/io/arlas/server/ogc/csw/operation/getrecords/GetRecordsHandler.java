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

package io.arlas.server.ogc.csw.operation.getrecords;

import io.arlas.server.model.CollectionReference;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import io.arlas.server.ogc.csw.utils.RecordBuilder;
import net.opengis.cat.csw._3.*;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class GetRecordsHandler {

    public CSWHandler cswHandler;

    public GetRecordsHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
    }

    public GetRecordsResponseType getCSWGetRecordsResponse(List<CollectionReference> collections,
                                                           ElementSetName elementSetName,
                                                           int startPosition,
                                                           long recordsMatched, String[] elements) throws DatatypeConfigurationException {
        GetRecordsResponseType getRecordsResponseType = new GetRecordsResponseType();
        SearchResultsType searchResultType = new SearchResultsType();
        RequestStatusType searchStatus = new RequestStatusType();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        searchStatus.setTimestamp(date);
        getRecordsResponseType.setSearchStatus(searchStatus);
        switch (elementSetName) {
            case brief:
                collections.forEach(collectionReference -> {
                    BriefRecordType briefRecord = RecordBuilder.getBriefResult(collectionReference, elements);
                    JAXBElement<BriefRecordType> briefRecordType = cswHandler.cswFactory.createBriefRecord(briefRecord);
                    searchResultType.getAbstractRecord().add(briefRecordType);

                });
                break;
            case summary:
                collections.forEach(collectionReference -> {
                    SummaryRecordType summaryRecord = RecordBuilder.getSummaryResult(collectionReference, elements);
                    JAXBElement<SummaryRecordType> summaryRecordType = cswHandler.cswFactory.createSummaryRecord(summaryRecord);
                    searchResultType.getAbstractRecord().add(summaryRecordType);
                });
                break;
            case full:
                collections.forEach(collectionReference -> {
                    RecordType record = RecordBuilder.getFullResult(collectionReference, elements, cswHandler.ogcConfiguration);
                    JAXBElement<RecordType> recordType = cswHandler.cswFactory.createRecord(record);
                    searchResultType.getAbstractRecord().add(recordType);
                });
                break;
            default:
                collections.forEach(collectionReference -> {
                    SummaryRecordType summaryRecord = RecordBuilder.getSummaryResult(collectionReference, elements);
                    JAXBElement<SummaryRecordType> summaryRecordType = cswHandler.cswFactory.createSummaryRecord(summaryRecord);
                    searchResultType.getAbstractRecord().add(summaryRecordType);
                });
                break;
        }
        getRecordsResponseType.setSearchResults(searchResultType);
        searchResultType.setNextRecord(BigInteger.valueOf(startPosition + collections.size() + 1));
        searchResultType.setNumberOfRecordsReturned(BigInteger.valueOf(collections.size()));
        searchResultType.setNumberOfRecordsMatched(BigInteger.valueOf(recordsMatched));
        return getRecordsResponseType;
    }
}
