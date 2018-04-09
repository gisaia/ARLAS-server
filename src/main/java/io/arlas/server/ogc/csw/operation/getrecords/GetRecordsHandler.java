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

import io.arlas.server.ogc.csw.CSWHandler;
import net.opengis.cat.csw._3.GetRecordsResponseType;
import net.opengis.cat.csw._3.RecordType;
import net.opengis.cat.csw._3.SearchResultsType;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBElement;

public class GetRecordsHandler {

    public CSWHandler cswHandler;


    public GetRecordsHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;

    }

    public JAXBElement<GetRecordsResponseType> getCSWGetRecordsResponse() {
        GetRecordsResponseType getRecordsResponseType = new GetRecordsResponseType();

        SearchResultsType searchResultType = new SearchResultsType();
        RecordType recordType = new RecordType();


        JAXBElement<RecordType> summaryRecord = cswHandler.cswFactory.createRecord(recordType);
        SimpleLiteral simpleLiteral = new SimpleLiteral();
        simpleLiteral.getContent().add("C-DIAS");
        JAXBElement<SimpleLiteral> title= cswHandler.dcElementFactory.createTitle(simpleLiteral);
        recordType.getDCElement().add(title);

        searchResultType.getAbstractRecord().add(summaryRecord);
        getRecordsResponseType.setSearchResults(searchResultType);
        return cswHandler.cswFactory.createGetRecordsResponse(getRecordsResponseType);
    }
}
