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

package io.arlas.server.ogc.csw.utils;

import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.model.Inspire;
import io.arlas.server.ogc.common.utils.OGCConstant;
import io.arlas.server.utils.StringUtil;
import net.opengis.cat.csw._3.AbstractRecordType;
import net.opengis.cat.csw._3.BriefRecordType;
import net.opengis.cat.csw._3.RecordType;
import net.opengis.cat.csw._3.SummaryRecordType;
import net.opengis.ows._2.WGS84BoundingBoxType;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBElement;
import java.util.Arrays;

public class RecordBuilder {

    private static final String RESOURCE_TYPE = "dataset";


    public static final org.purl.dc.elements._1.ObjectFactory dcObjectFactory = new org.purl.dc.elements._1.ObjectFactory();
    public static final org.purl.dc.terms.ObjectFactory dctObjectFactory = new org.purl.dc.terms.ObjectFactory();

    public static final net.opengis.ows._2.ObjectFactory owsObjectFactory = new net.opengis.ows._2.ObjectFactory();

    public static BriefRecordType getBriefResult(CollectionReference collectionReference, String[] elements, boolean inspireIsEnabled) {
        BriefRecordType briefRecord = new BriefRecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addIdentifier(briefRecord, dublinCoreElementName.identifier);
        if (elements.length == 0 || elements == null) {
            // ADD ALL field
            addTitle(briefRecord, dublinCoreElementName.title);
            if (inspireIsEnabled) {
                addType(briefRecord, RESOURCE_TYPE);
            } else {
                addType(briefRecord, dublinCoreElementName.type);
            }
            addBbox(briefRecord, dublinCoreElementName.bbox);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        addTitle(briefRecord, dublinCoreElementName.title);
                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        if (inspireIsEnabled) {
                            addType(briefRecord, RESOURCE_TYPE);
                        } else {
                            addType(briefRecord, dublinCoreElementName.type);
                        }                         break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(briefRecord, dublinCoreElementName.bbox);
                        break;
                }
            }
        }
        return briefRecord;
    }

    public static SummaryRecordType getSummaryResult(CollectionReference collectionReference, String[] elements, String serverUri, boolean inspireIsEnabled) {
        SummaryRecordType summaryRecord = new SummaryRecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        Inspire inspire = collectionReference.params.inspire;
        addIdentifier(summaryRecord, dublinCoreElementName.identifier);
        if (elements.length == 0) {
            // ADD ALL field
            addTitle(summaryRecord, dublinCoreElementName.title);
            if (inspire != null && inspire.keywords != null) {
                inspire.keywords.forEach(keyword -> {
                    addSubject(summaryRecord, keyword.value);

                });
            }
            if (!StringUtil.isNullOrEmpty(dublinCoreElementName.subject)) {
                addSubject(summaryRecord, dublinCoreElementName.subject);
            }
            if (inspireIsEnabled) {
                addType(summaryRecord, RESOURCE_TYPE);
            } else {
                addType(summaryRecord, dublinCoreElementName.type);
            }             addBbox(summaryRecord, dublinCoreElementName.bbox);
            addModified(summaryRecord, dublinCoreElementName.getDate());
            addFormat(summaryRecord, dublinCoreElementName.format);
            addAbstract(summaryRecord, dublinCoreElementName.description);
            addUrlToWFS(summaryRecord, serverUri + "ogc/wfs/" + collectionReference.collectionName + "/?" + OGCConstant.WFS_GET_CAPABILITIES_PARAMETERS);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        addTitle(summaryRecord, dublinCoreElementName.title);
                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        if (inspireIsEnabled) {
                            addType(summaryRecord, RESOURCE_TYPE);
                        } else {
                            addType(summaryRecord, dublinCoreElementName.type);
                        }                        break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(summaryRecord, dublinCoreElementName.bbox);
                        break;
                    case CSWConstant.DC_FIELD_SUBJECT:
                        if (inspire.keywords != null) {
                            inspire.keywords.forEach(keyword -> addSubject(summaryRecord, keyword.value));
                        }
                        if (!StringUtil.isNullOrEmpty(dublinCoreElementName.subject)) {
                            addSubject(summaryRecord, dublinCoreElementName.subject);
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
        return summaryRecord;
    }

    public static RecordType getFullResult(CollectionReference collectionReference, String[] elements, OGCConfiguration ogcConfiguration, boolean inspireIsEnabled) {
        RecordType record = new RecordType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        Inspire inspire = collectionReference.params.inspire;
        addIdentifier(record, dublinCoreElementName.identifier);
        if (elements.length == 0 || elements == null) {
            // ADD ALL field
            addTitle(record, dublinCoreElementName.title);
            if (inspire != null && inspire.keywords != null) {
                inspire.keywords.forEach(keyword -> {
                    if (keyword.value != null) {
                        addSubject(record, keyword.value);
                    }
                });
            }
            if (!StringUtil.isNullOrEmpty(dublinCoreElementName.subject)) {
                addSubject(record, dublinCoreElementName.subject);
            }
            if (inspireIsEnabled) {
                addType(record, RESOURCE_TYPE);
            } else {
                addType(record, dublinCoreElementName.type);
            }
            addBbox(record, dublinCoreElementName.bbox);
            addModified(record, dublinCoreElementName.getDate());
            addFormat(record, dublinCoreElementName.format);
            addAbstract(record, dublinCoreElementName.description);
            addUrlToWFS(record, ogcConfiguration.serverUri + "ogc/wfs/" + collectionReference.collectionName + "/?" + OGCConstant.WFS_GET_CAPABILITIES_PARAMETERS);
        } else {
            for (String element : Arrays.asList(elements)) {
                switch (element.toLowerCase()) {
                    case CSWConstant.DC_FIELD_TITLE:
                        addTitle(record, dublinCoreElementName.title);
                        break;
                    case CSWConstant.DC_FIELD_TYPE:
                        if (inspireIsEnabled) {
                            addType(record, RESOURCE_TYPE);
                        } else {
                            addType(record, dublinCoreElementName.type);
                        }                        break;
                    case CSWConstant.DC_FIELD_BBOX:
                        addBbox(record, dublinCoreElementName.bbox);
                        break;
                    case CSWConstant.DC_FIELD_SUBJECT:
                        if (inspire.keywords != null) {
                            inspire.keywords.forEach(keyword -> addSubject(record, keyword.value));
                        }
                        if (!StringUtil.isNullOrEmpty(dublinCoreElementName.subject)) {
                            addSubject(record, dublinCoreElementName.subject);
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
        return record;
    }

    public static void addTitle(AbstractRecordType abstractRecordType, String title) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(title);
        JAXBElement<SimpleLiteral> JAXBElementTitle = dcObjectFactory.createTitle(simpleLiteral);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "BriefRecordType":
                ((BriefRecordType) abstractRecordType).getTitle().add(JAXBElementTitle);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getTitle().add(JAXBElementTitle);
                break;
            case "RecordType":
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementTitle);
                break;
        }
    }

    public static void addType(AbstractRecordType abstractRecordType, String type) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(type);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "BriefRecordType":
                ((BriefRecordType) abstractRecordType).setType(simpleLiteral);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).setType(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementType = dcObjectFactory.createType(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementType);
                break;
        }
    }

    public static void addUrlToWFS(AbstractRecordType abstractRecordType, String url) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(url);
        simpleLiteral.setScheme("http://www.opengis.net/wfs/2.0");

        JAXBElement<SimpleLiteral> JAXBElementIdentifier = dctObjectFactory.createReferences((simpleLiteral));
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

    public static void addIdentifier(AbstractRecordType abstractRecordType, String identifier) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(identifier);
        JAXBElement<SimpleLiteral> JAXBElementIdentifier = dcObjectFactory.createIdentifier(simpleLiteral);
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

    public static void addBbox(AbstractRecordType abstractRecordType, DublinCoreElementName.Bbox bbox) {
        WGS84BoundingBoxType boudingBox = new WGS84BoundingBoxType();
        boudingBox.getLowerCorner().add(String.valueOf(bbox.west));
        boudingBox.getLowerCorner().add(String.valueOf(bbox.south));
        boudingBox.getUpperCorner().add(String.valueOf(bbox.east));
        boudingBox.getUpperCorner().add(String.valueOf(bbox.north));
        JAXBElement<WGS84BoundingBoxType> JAXBElementBbox = owsObjectFactory.createWGS84BoundingBox(boudingBox);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "BriefRecordType":
                ((BriefRecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
            case "RecordType":
                ((RecordType) abstractRecordType).getBoundingBox().add(JAXBElementBbox);
                break;
        }
    }

    public static void addSubject(AbstractRecordType abstractRecordType, String subject) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(subject);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getSubject().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementSubject = dcObjectFactory.createSubject(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementSubject);
                break;
        }

    }

    public static void addModified(AbstractRecordType abstractRecordType, String modified) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(modified);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getModified().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementDate = dcObjectFactory.createDate(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementDate);
                break;
        }
    }

    public static void addFormat(AbstractRecordType abstractRecordType, String format) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        if (format == null || format.equals("")) {
            format = "unknown";
        }
        simpleLiteral.getContent().add(format);
        JAXBElement<SimpleLiteral> JAXBElementFormat = dcObjectFactory.createFormat(simpleLiteral);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getFormat().add(JAXBElementFormat);
                break;
            case "RecordType":
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementFormat);
                break;
        }
    }

    public static void addAbstract(AbstractRecordType abstractRecordType, String description) {
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add(description);
        switch (abstractRecordType.getClass().getSimpleName()) {
            case "SummaryRecordType":
                ((SummaryRecordType) abstractRecordType).getAbstract().add(simpleLiteral);
                break;
            case "RecordType":
                JAXBElement<SimpleLiteral> JAXBElementAbstract = dcObjectFactory.createDescription(simpleLiteral);
                ((RecordType) abstractRecordType).getDCElement().add(JAXBElementAbstract);
                break;
        }
    }
}
