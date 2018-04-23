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
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.utils.ElementSetName;
import net.opengis.cat.csw._3.*;
import net.opengis.ows._2.BoundingBoxType;
import net.opengis.ows._2.WGS84BoundingBoxType;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Arrays;
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
                    JAXBElement<BriefRecordType> briefRecordTypeJAXBElement = getBriefResult(collectionReference, elements);
                    searchResultType.getAbstractRecord().add(briefRecordTypeJAXBElement);
                });
                break;
            case summary:
                collections.forEach(collectionReference -> {
                    JAXBElement<SummaryRecordType> summaryRecordTypeJAXBElement = getSummaryResult(collectionReference, elements);
                    searchResultType.getAbstractRecord().add(summaryRecordTypeJAXBElement);
                });
                break;
            case full:
                collections.forEach(collectionReference -> {
                    JAXBElement<RecordType> recordTypeJAXBElement = getFullResult(collectionReference, elements);
                    searchResultType.getAbstractRecord().add(recordTypeJAXBElement);
                });
                break;
            default:
                collections.forEach(collectionReference -> {
                    JAXBElement<SummaryRecordType> summaryRecordTypeJAXBElement = getSummaryResult(collectionReference, elements);
                    searchResultType.getAbstractRecord().add(summaryRecordTypeJAXBElement);
                });
                break;
        }
        getRecordsResponseType.setSearchResults(searchResultType);

        searchResultType.setNextRecord(BigInteger.valueOf(startPosition + collections.size()));
        searchResultType.setNumberOfRecordsReturned(BigInteger.valueOf(collections.size()));
        searchResultType.setNumberOfRecordsMatched(BigInteger.valueOf(recordsMatched));
        return  getRecordsResponseType;
    }

    private JAXBElement<BriefRecordType> getBriefResult(CollectionReference collectionReference, String[] elements) {
        BriefRecordType briefRecord = new BriefRecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        if (elements.length == 0 || elements == null) {
            // ADD ALL field
            if(dublinCoreElementName.title!=""){
                addTitle(briefRecord, dublinCoreElementName.title);
            }else{
                addTitle(briefRecord, collectionReference.collectionName);
            }            addType(briefRecord, dublinCoreElementName.type);
            addBbox(briefRecord, dublinCoreElementName.bbox);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        if(dublinCoreElementName.title!=""){
                            addTitle(briefRecord, dublinCoreElementName.title);
                        }else{
                            addTitle(briefRecord, collectionReference.collectionName);
                        }                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        addType(briefRecord, dublinCoreElementName.type);
                        break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(briefRecord, dublinCoreElementName.bbox);
                        break;
                }
            }
        }
        if(dublinCoreElementName.identifier!=""){
            addIdentifier(briefRecord, dublinCoreElementName.identifier);
        }else{
            addIdentifier(briefRecord, String.valueOf(collectionReference.collectionName.hashCode()));
        }
        JAXBElement<BriefRecordType> briefRecordType = cswHandler.cswFactory.createBriefRecord(briefRecord);
        return briefRecordType;

    }

    private JAXBElement<SummaryRecordType> getSummaryResult(CollectionReference collectionReference, String[] elements) {
        SummaryRecordType summaryRecord = new SummaryRecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        if (elements.length == 0 || elements == null) {
            // ADD ALL field
            if(dublinCoreElementName.title!=""){
                addTitle(summaryRecord, dublinCoreElementName.title);
            }else{
                addTitle(summaryRecord, collectionReference.collectionName);
            }
            if(dublinCoreElementName.subject!=""){
                addSubject(summaryRecord, dublinCoreElementName.subject);
            }else{
                addSubject(summaryRecord, collectionReference.collectionName);
            }
            addType(summaryRecord, dublinCoreElementName.type);
            addBbox(summaryRecord, dublinCoreElementName.bbox);
            addModified(summaryRecord, dublinCoreElementName.getDate());
            addFormat(summaryRecord, dublinCoreElementName.format);
            addAbstract(summaryRecord, dublinCoreElementName.description);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        if(dublinCoreElementName.title!=""){
                            addTitle(summaryRecord, dublinCoreElementName.title);
                        }else{
                            addTitle(summaryRecord, collectionReference.collectionName);
                        }                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        addType(summaryRecord, dublinCoreElementName.type);
                        break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(summaryRecord, dublinCoreElementName.bbox);
                        break;
                    case CSWConstant.DC_FIELD_SUBJECT:
                        if(dublinCoreElementName.subject!=""){
                            addSubject(summaryRecord, dublinCoreElementName.subject);
                        }else{
                            addSubject(summaryRecord, collectionReference.collectionName);
                        }
                        break;
                    case CSWConstant.DC_FIELD_MODIFIED:
                        addModified(summaryRecord, dublinCoreElementName.getDate());
                        break;
                    case CSWConstant.DC_FIELD_FORMAT:
                        addFormat(summaryRecord, dublinCoreElementName.format);
                        break;
                    case CSWConstant.DC_FIELD_ABSTRACT:
                        addAbstract(summaryRecord, dublinCoreElementName.description);
                        break;
                }
            }
        }
        if(dublinCoreElementName.identifier!=""){
            addIdentifier(summaryRecord, dublinCoreElementName.identifier);
        }else{
            addIdentifier(summaryRecord, String.valueOf(collectionReference.collectionName.hashCode()));
        }
        JAXBElement<SummaryRecordType> summaryRecordType = cswHandler.cswFactory.createSummaryRecord(summaryRecord);
        return summaryRecordType;
    }

    private JAXBElement<RecordType> getFullResult(CollectionReference collectionReference, String[] elements) {
        RecordType record = new RecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        if (elements.length == 0 || elements == null) {
            // ADD ALL field
            if(dublinCoreElementName.title!=""){
                addTitle(record, dublinCoreElementName.title);
            }else{
                addTitle(record, collectionReference.collectionName);
            }
            if(dublinCoreElementName.subject!=""){
                addSubject(record, dublinCoreElementName.subject);
            }else{
                addSubject(record, collectionReference.collectionName);
            }
            addType(record, dublinCoreElementName.type);
            addBbox(record, dublinCoreElementName.bbox);
            addModified(record, dublinCoreElementName.getDate());
            addFormat(record, dublinCoreElementName.format);
            addAbstract(record, dublinCoreElementName.description);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        if(dublinCoreElementName.title!=""){
                            addTitle(record, dublinCoreElementName.title);
                        }else{
                            addTitle(record, collectionReference.collectionName);
                        }
                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        addType(record, dublinCoreElementName.type);
                        break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(record, dublinCoreElementName.bbox);
                        break;
                    case CSWConstant.DC_FIELD_SUBJECT:
                        if(dublinCoreElementName.subject!=""){
                            addSubject(record, dublinCoreElementName.subject);
                        }else{
                            addSubject(record, collectionReference.collectionName);
                        }
                        break;
                    case CSWConstant.DC_FIELD_MODIFIED:
                        addModified(record, dublinCoreElementName.getDate());
                        break;
                    case CSWConstant.DC_FIELD_FORMAT:
                        addFormat(record, dublinCoreElementName.format);
                        break;
                    case CSWConstant.DC_FIELD_ABSTRACT:
                        addAbstract(record, dublinCoreElementName.description);
                        break;
                }
            }
        }
        if(dublinCoreElementName.identifier!=""){
            addIdentifier(record, dublinCoreElementName.identifier);
        }else{
            addIdentifier(record, String.valueOf(collectionReference.collectionName.hashCode()));
        }        JAXBElement<RecordType> recordType = cswHandler.cswFactory.createRecord(record);
        return recordType;
    }

    private void addTitle(AbstractRecordType abstractRecordType, String title) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(title);
        JAXBElement<SimpleLiteral> JAXBElementTitle = cswHandler.dcElementFactory.createTitle(simpleLiteral);
        switch(abstractRecordType.getClass().getSimpleName()){
            case "BriefRecordType":
                ((BriefRecordType)abstractRecordType).getTitle().add(JAXBElementTitle);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType)abstractRecordType).getTitle().add(JAXBElementTitle);
                break;
            case "RecordType":
                ((RecordType)abstractRecordType).getDCElement().add(JAXBElementTitle);
                break;
        }
    }

    private void addType(AbstractRecordType abstractRecordType, String type) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(type);
        switch(abstractRecordType.getClass().getSimpleName()){
            case "BriefRecordType":
                ((BriefRecordType)abstractRecordType).setType(simpleLiteral);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType)abstractRecordType).setType(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementType = cswHandler.dcElementFactory.createType(simpleLiteral);
                ((RecordType)abstractRecordType).getDCElement().add(JAXBElementType);
                break;
        }
    }

    private void addIdentifier(AbstractRecordType abstractRecordType, String identifier) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(identifier);
        JAXBElement<SimpleLiteral> JAXBElementIdentifier = cswHandler.dcElementFactory.createIdentifier(simpleLiteral);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "BriefRecordType":
                ((BriefRecordType) abstractRecordType).getIdentifier().add(JAXBElementIdentifier);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getIdentifier().add(JAXBElementIdentifier);
                break;
            case "RecordType":
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementIdentifier);
                break;
        }
    }

    private void addBbox(AbstractRecordType abstractRecordType, DublinCoreElementName.Bbox bbox) {
        WGS84BoundingBoxType boudingBox = new WGS84BoundingBoxType();
        boudingBox.getLowerCorner().add(0d);
        boudingBox.getLowerCorner().add(0d);
        boudingBox.getUpperCorner().add(0d);
        boudingBox.getUpperCorner().add(0d);
        JAXBElement<WGS84BoundingBoxType> JAXBElementBbox = cswHandler.owsFactory.createWGS84BoundingBox(boudingBox);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "BriefRecordType":
                //((BriefRecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
            case "SummaryRecordType":
                //((SummaryRecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
            case "RecordType":
                //((RecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
        }
    }

    private void addSubject(AbstractRecordType abstractRecordType, String subject) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(subject);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getSubject().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementSubject = cswHandler.dcElementFactory.createSubject(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementSubject);
                break;
        }

    }

    private void addModified(AbstractRecordType abstractRecordType, String modified) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(modified);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getModified().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementDate = cswHandler.dcElementFactory.createDate(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementDate);
                break;
        }
    }

    private void addFormat(AbstractRecordType abstractRecordType, String format) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(format);
        JAXBElement<SimpleLiteral> JAXBElementFormat = cswHandler.dcElementFactory.createFormat(simpleLiteral);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getFormat().add(JAXBElementFormat);
                break;
            case "RecordType":
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementFormat);
                break;
        }
    }

    private void addAbstract(AbstractRecordType abstractRecordType, String description) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(description);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getAbstract().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementAbstract = cswHandler.dcElementFactory.createDescription(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementAbstract);
                break;
        }
    }
}
