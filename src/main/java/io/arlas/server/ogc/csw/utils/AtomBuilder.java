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
import org.w3._2005.atom.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class AtomBuilder {

    public static void setEntryType(AbstractRecordType abstractRecordType, FeedType feedType, EntryType entryType){
        String recordType = abstractRecordType.getClass().getSimpleName();
        switch (recordType){
            case "BriefRecordType":
                setBriefRecordType((BriefRecordType)abstractRecordType,entryType);
                break;
            case "SummaryRecordType":
                setSummaryRecordType((SummaryRecordType)abstractRecordType,entryType);
                break;
            case "RecordType":
                setRecordType((RecordType)abstractRecordType,entryType);
                break;
        }
    }

    public static void setRecordType(RecordType recordType, EntryType entryType) {
        recordType.getDCElement().stream().forEach(simpleLiteralJAXBElement -> {
            entryType.getOtherAttributes().put(
                    simpleLiteralJAXBElement.getName(),simpleLiteralJAXBElement.getValue().toString());
        });
    }

    public static void setSummaryRecordType(SummaryRecordType summaryRecordType, EntryType entryType) {
        ObjectFactory objectFactory = new ObjectFactory();
        TextType title = objectFactory.createTextType();
        summaryRecordType.getTitle().forEach(t->{
            t.getValue().getContent().forEach(o->{
                title.getContent().add(o);
            });
        });
        IdType idTypeValue = new IdType();
        summaryRecordType.getIdentifier().forEach(id->{
            entryType.setIdentifier(id);
            id.getValue().getContent().forEach(content->{
                idTypeValue.setValue(content);

            });
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        summaryRecordType.getModified().forEach(d->{
            d.getContent().forEach(v->{
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
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        });
        entryType.setId(idTypeValue);
        entryType.setTitle(title);
    }

    public static void setBriefRecordType(BriefRecordType briefRecordType,EntryType entryType) {
        ObjectFactory objectFactory = new ObjectFactory();
        TextType title = objectFactory.createTextType();
        briefRecordType.getTitle().forEach(t->{
            t.getValue().getContent().forEach(o->{
                title.getContent().add(o);
            });
        });
        IdType idTypeValue = new IdType();
        briefRecordType.getIdentifier().forEach(id->{
            id.getValue().getContent().forEach(content->{
                idTypeValue.setValue(content);
            });
        });
        entryType.setId(idTypeValue);
        entryType.setTitle(title);
    }
}
