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

import net.opengis.cat.csw._3.AbstractRecordType;
import net.opengis.cat.csw._3.BriefRecordType;
import net.opengis.cat.csw._3.RecordType;
import net.opengis.cat.csw._3.SummaryRecordType;
import org.purl.dc.elements._1.SimpleLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3._2005.atom.*;

import javax.ws.rs.WebApplicationException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class AtomBuilder {
    public static Logger LOGGER = LoggerFactory.getLogger(AtomBuilder.class);

    public static void setEntryType(AbstractRecordType abstractRecordType, FeedType feedType, EntryType entryType) {
        String recordType = abstractRecordType.getClass().getSimpleName();
        switch (recordType) {
            case "BriefRecordType":
                setBriefRecordType((BriefRecordType) abstractRecordType, entryType);
                break;
            case "SummaryRecordType":
                setSummaryRecordType((SummaryRecordType) abstractRecordType, entryType);
                break;
            case "RecordType":
                setRecordType((RecordType) abstractRecordType, entryType);
                break;
        }
    }

    public static void setRecordType(RecordType recordType, EntryType entryType) {
        org.purl.dc.elements._1.ObjectFactory objectFactory = new org.purl.dc.elements._1.ObjectFactory();
        recordType.getDCElement().stream().forEach(simpleLiteralJAXBElement -> {
            LOGGER.info(simpleLiteralJAXBElement.getName().getLocalPart());
            String value = simpleLiteralJAXBElement.getValue().getContent().get(0);
            TextType textType = new TextType();
            textType.getContent().add(value);
            SimpleLiteral simpleLiteral = objectFactory.createSimpleLiteral();
            simpleLiteral.getContent().add(value);
            switch (simpleLiteralJAXBElement.getName().getLocalPart()) {
                case "title":
                    entryType.setTitle(textType);
                    break;
                case "subject":
                    entryType.setSubject(simpleLiteral);
                    break;
                case "type":
                    entryType.setType(simpleLiteral);
                    break;
                case "date":
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    try {
                        GregorianCalendar c = new GregorianCalendar();
                        c.setTime(simpleDateFormat.parse(value));
                        try {
                            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                            DateTimeType dateTimeType = new DateTimeType();
                            dateTimeType.setValue(date);
                            entryType.setPublished(dateTimeType);
                            entryType.setUpdated(dateTimeType);
                        } catch (DatatypeConfigurationException e) {
                            e.printStackTrace();
                            new WebApplicationException(e);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        new WebApplicationException(e);

                    }
                    break;
                case "format":
                    entryType.setFormat(objectFactory.createFormat(simpleLiteral));
                    break;
                case "description":
                    entryType.setSummary(textType);
                    break;
                case "identifier":
                    entryType.setIdentifier(objectFactory.createIdentifier(simpleLiteral));
                    break;
            }
        });
        String south = recordType.getBoundingBox().get(0).getValue().getLowerCorner().get(0);
        String west = recordType.getBoundingBox().get(0).getValue().getLowerCorner().get(1);
        String north = recordType.getBoundingBox().get(0).getValue().getUpperCorner().get(0);
        String east = recordType.getBoundingBox().get(0).getValue().getUpperCorner().get(1);
        entryType.getBox().add(south);
        entryType.getBox().add(west);
        entryType.getBox().add(north);
        entryType.getBox().add(east);
    }

    public static void setSummaryRecordType(SummaryRecordType summaryRecordType, EntryType entryType) {
        ObjectFactory objectFactory = new ObjectFactory();
        TextType title = objectFactory.createTextType();
        summaryRecordType.getTitle().forEach(t -> {
            t.getValue().getContent().forEach(o -> {
                title.getContent().add(o);
            });
        });
        IdType idTypeValue = new IdType();
        summaryRecordType.getIdentifier().forEach(id -> {
            entryType.setIdentifier(id);
            id.getValue().getContent().forEach(content -> {
                idTypeValue.setValue(content);

            });
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        summaryRecordType.getModified().forEach(d -> {
            d.getContent().forEach(v -> {
                try {
                    GregorianCalendar c = new GregorianCalendar();
                    c.setTime(simpleDateFormat.parse(v));
                    try {
                        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                        DateTimeType dateTimeType = new DateTimeType();
                        dateTimeType.setValue(date);
                        entryType.setUpdated(dateTimeType);
                    } catch (DatatypeConfigurationException e) {
                        e.printStackTrace();
                        new WebApplicationException(e);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    new WebApplicationException(e);
                }
            });
        });
        entryType.setId(idTypeValue);
        entryType.setTitle(title);
        String south = summaryRecordType.getBoundingBox().get(0).getValue().getLowerCorner().get(0);
        String west = summaryRecordType.getBoundingBox().get(0).getValue().getLowerCorner().get(1);
        String north = summaryRecordType.getBoundingBox().get(0).getValue().getUpperCorner().get(0);
        String east = summaryRecordType.getBoundingBox().get(0).getValue().getUpperCorner().get(1);
        entryType.getBox().add(south);
        entryType.getBox().add(west);
        entryType.getBox().add(north);
        entryType.getBox().add(east);
    }

    public static void setBriefRecordType(BriefRecordType briefRecordType, EntryType entryType) {
        ObjectFactory objectFactory = new ObjectFactory();
        TextType title = objectFactory.createTextType();
        briefRecordType.getTitle().forEach(t -> {
            t.getValue().getContent().forEach(o -> {
                title.getContent().add(o);
            });
        });
        IdType idTypeValue = new IdType();
        briefRecordType.getIdentifier().forEach(id -> {
            id.getValue().getContent().forEach(content -> {
                idTypeValue.setValue(content);
            });
        });
        entryType.setId(idTypeValue);
        entryType.setTitle(title);
        String south = briefRecordType.getBoundingBox().get(0).getValue().getLowerCorner().get(0);
        String west = briefRecordType.getBoundingBox().get(0).getValue().getLowerCorner().get(1);
        String north = briefRecordType.getBoundingBox().get(0).getValue().getUpperCorner().get(0);
        String east = briefRecordType.getBoundingBox().get(0).getValue().getUpperCorner().get(1);
        entryType.getBox().add(south);
        entryType.getBox().add(west);
        entryType.getBox().add(north);
        entryType.getBox().add(east);
    }
}
