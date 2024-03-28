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


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import net.opengis.cat.csw._3.BriefRecordType;
import net.opengis.cat.csw._3.RecordType;
import net.opengis.cat.csw._3.SummaryRecordType;
import org.isotc211._2005.gmd.MDMetadataType;

import java.util.ArrayList;
import java.util.List;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetRecordByIdResponse", propOrder = {
        "summaryRecordTypes",
        "briefRecordTypes",
        "recordTypes",
        "mdMetadata"
}, namespace = "http://www.opengis.net/cat/csw/3.0")

public class GetRecordByIdResponse {
    @XmlElement(name = "SummaryRecordType", namespace = "http://www.opengis.net/cat/csw/3.0", required = false)
    protected List<SummaryRecordType> summaryRecordTypes;
    @XmlElement(name = "BriefRecordType", namespace = "http://www.opengis.net/cat/csw/3.0", required = false)
    protected List<BriefRecordType> briefRecordTypes;
    @XmlElement(name = "RecordType", namespace = "http://www.opengis.net/cat/csw/3.0",   required = false)
    protected List<RecordType> recordTypes;
    @XmlElement(name = "MD_Metadata", namespace = "http://www.isotc211.org/2005/gmd")
    protected List<MDMetadataType> mdMetadata;


    public List<SummaryRecordType> getSummaryRecordTypes() {
        return summaryRecordTypes;
    }

    public List<BriefRecordType> getBriefRecordTypes() {
        return briefRecordTypes;
    }

    public List<RecordType> getRecordTypes() {
        return recordTypes;
    }

    public List<MDMetadataType> getMdMetadata() {
        return mdMetadata;
    }

    public GetRecordByIdResponse() {
        summaryRecordTypes = new ArrayList<>();
        briefRecordTypes = new ArrayList<>();
        recordTypes = new ArrayList<>();
        mdMetadata = new ArrayList<>();
    }
}
