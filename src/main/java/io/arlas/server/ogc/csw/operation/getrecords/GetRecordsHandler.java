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
import net.opengis.cat.csw._3.*;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;

public class GetRecordsHandler {

    public CSWHandler cswHandler;


    public GetRecordsHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;

    }

    public JAXBElement<GetRecordsResponseType> getCSWGetRecordsResponse(List<CollectionReference> collections,
                                                                        ElementSetName elementSetName,
                                                                        int startPosition,
                                                                        int maxRecords) {
        GetRecordsResponseType getRecordsResponseType = new GetRecordsResponseType();
        SearchResultsType searchResultType = new SearchResultsType();
        switch (elementSetName) {
            case brief:
                collections.forEach(collectionReference -> {
                    JAXBElement<BriefRecordType> briefRecordTypeJAXBElement = getBriefResult(collectionReference);
                    searchResultType.getAbstractRecord().add(briefRecordTypeJAXBElement);
                    getRecordsResponseType.setSearchResults(searchResultType);
                });
                break;
            case summary:
                collections.forEach(collectionReference -> {
                    JAXBElement<SummaryRecordType> summaryRecordTypeJAXBElement = getSummaryResult(collectionReference);
                    searchResultType.getAbstractRecord().add(summaryRecordTypeJAXBElement);
                    getRecordsResponseType.setSearchResults(searchResultType);
                });
                break;
            case full:
                collections.forEach(collectionReference -> {
                    JAXBElement<RecordType> recordTypeJAXBElement = getFullResult(collectionReference);
                    searchResultType.getAbstractRecord().add(recordTypeJAXBElement);
                    getRecordsResponseType.setSearchResults(searchResultType);
                });
                break;
            default:
                collections.forEach(collectionReference -> {
                    JAXBElement<SummaryRecordType> summaryRecordTypeJAXBElement = getSummaryResult(collectionReference);
                    searchResultType.getAbstractRecord().add(summaryRecordTypeJAXBElement);
                    getRecordsResponseType.setSearchResults(searchResultType);
                });
                break;
        }

        searchResultType.setNextRecord(BigInteger.valueOf(startPosition + maxRecords));
        searchResultType.setNumberOfRecordsReturned(BigInteger.valueOf(collections.size()));
        searchResultType.setNumberOfRecordsMatched(BigInteger.valueOf(collections.size()));

        return cswHandler.cswFactory.createGetRecordsResponse(getRecordsResponseType);
    }

    private JAXBElement<BriefRecordType> getBriefResult(CollectionReference collectionReference) {
        BriefRecordType briefRecord = new BriefRecordType();
        JAXBElement<BriefRecordType> briefRecordType = cswHandler.cswFactory.createBriefRecord(briefRecord);
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(collectionReference.collectionName);
        JAXBElement<SimpleLiteral> title = cswHandler.dcElementFactory.createTitle(simpleLiteral);
        briefRecordType.getValue().getTitle().add(title);
        return briefRecordType;

    }

    private JAXBElement<SummaryRecordType> getSummaryResult(CollectionReference collectionReference) {
        SummaryRecordType summaryRecord = new SummaryRecordType();
        JAXBElement<SummaryRecordType> summaryRecordType = cswHandler.cswFactory.createSummaryRecord(summaryRecord);
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(collectionReference.collectionName);
        JAXBElement<SimpleLiteral> title = cswHandler.dcElementFactory.createTitle(simpleLiteral);
        summaryRecordType.getValue().getTitle().add(title);
        summaryRecordType.getValue().getSubject().add(simpleLiteral);

        return summaryRecordType;
    }

    private JAXBElement<RecordType> getFullResult(CollectionReference collectionReference) {
        RecordType record = new RecordType();
        JAXBElement<RecordType> recordType = cswHandler.cswFactory.createRecord(record);
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(collectionReference.collectionName);
        JAXBElement<SimpleLiteral> title = cswHandler.dcElementFactory.createTitle(simpleLiteral);
        JAXBElement<SimpleLiteral> subject = cswHandler.dcElementFactory.createSubject(simpleLiteral);
        recordType.getValue().getDCElement().add(title);
        recordType.getValue().getDCElement().add(subject);
        recordType.getValue().getDCElement().add(subject);


        return recordType;
    }
}
