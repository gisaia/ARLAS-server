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

package io.arlas.server.ogc.csw.operation.opensearch;


import com.a9.opensearch.ObjectFactory;
import com.a9.opensearch.OpenSearchDescription;
import com.a9.opensearch.Url;
import io.arlas.server.ogc.csw.CSWHandler;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

public class OpenSearchHandler {

    public CSWHandler cswHandler;

    public OpenSearchHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
    }

    public OpenSearchDescription getOpenSearchDescription(String serverUrl) {
        ObjectFactory objectFactory = new ObjectFactory();

        OpenSearchDescription description = objectFactory.createOpenSearchDescription();
        description.getOtherAttributes().put(new QName("http://a9.com/-/opensearch/extensions/geo/1.0/", "opensearchgeo"), "opensearchgeo");
        description.setDescription(cswHandler.cswConfiguration.openSearchDescription);
        description.setShortName(cswHandler.cswConfiguration.openSearchShortName);
        Url urlXMLBox = new Url();
        urlXMLBox.setTemplate(serverUrl + "ogc/csw/?" + "request=GetRecords&service=CSW" +
                "&version=3.0.0&q={searchTerms?}" +
                "&startPosition={startIndex?}" +
                "&maxRecords={count?}" +
                "&bbox={ns3:box?}" +
                "&elementSetName=full&outputschema=http://www.opengis.net/cat/csw/3.0" +
                "&outputFormat=application/xml");
        urlXMLBox.setType(MediaType.APPLICATION_XML);
        Url urlATOMBox = new Url();
        urlATOMBox.setTemplate(serverUrl + "ogc/csw/?" + "request=GetRecords&service=CSW" +
                "&version=3.0.0&q={searchTerms?}" +
                "&startPosition={startIndex?}" +
                "&maxRecords={count?}" +
                "&bbox={ns3:box?}" +
                "&elementSetName=full" +
                "&outputFormat=application/atom%2Bxml");
        urlATOMBox.setType(MediaType.APPLICATION_ATOM_XML);
        Url urlXMLId = new Url();
        urlXMLId.setTemplate(serverUrl + "ogc/csw/?" + "request=GetRecords&service=CSW" +
                "&version=3.0.0" +
                "&startPosition={startIndex?}" +
                "&maxRecords={count?}" +
                "&recordIds={ns3:uid?}" +
                "&elementSetName=full&outputschema=http://www.opengis.net/cat/csw/3.0" +
                "&outputFormat=application/xml");
        urlXMLId.setType(MediaType.APPLICATION_XML);
        Url urlATOMId = new Url();
        urlATOMId.setTemplate(serverUrl + "ogc/csw/?" + "request=GetRecords&service=CSW" +
                "&version=3.0.0" +
                "&startPosition={startIndex?}" +
                "&maxRecords={count?}" +
                "&recordIds={ns3:uid?}" +
                "&elementSetName=full" +
                "&outputFormat=application/atom%2Bxml");
        urlATOMId.setType(MediaType.APPLICATION_ATOM_XML);
        description.getUrl().add(urlXMLBox);
        description.getUrl().add(urlXMLId);
        description.getUrl().add(urlATOMBox);
        description.getUrl().add(urlATOMId);
        return description;
    }
}
