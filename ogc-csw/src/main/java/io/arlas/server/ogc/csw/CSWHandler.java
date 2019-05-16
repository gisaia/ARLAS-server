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

package io.arlas.server.ogc.csw;

import io.arlas.server.app.CSWConfiguration;
import io.arlas.server.app.InspireConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.ogc.csw.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.ogc.csw.operation.getrecordbyid.GetRecordsByIdHandler;
import io.arlas.server.ogc.csw.operation.getrecords.GetRecordsHandler;
import io.arlas.server.ogc.csw.operation.opensearch.OpenSearchHandler;
import io.arlas.server.utils.StringUtil;
import net.opengis.cat.csw._3.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSWHandler {

    protected static Logger LOGGER = LoggerFactory.getLogger(CSWHandler.class);

    public GetCapabilitiesHandler getCapabilitiesHandler;
    public GetRecordsHandler getRecordsHandler;
    public GetRecordsByIdHandler getRecordsByIdHandler;
    public OpenSearchHandler openSearchHandler;

    public OGCConfiguration ogcConfiguration;
    public CSWConfiguration cswConfiguration;
    public InspireConfiguration inspireConfiguration;
    public String baseUri;

    public ObjectFactory cswFactory = new ObjectFactory();
    public net.opengis.ows._2.ObjectFactory owsFactory = new net.opengis.ows._2.ObjectFactory();
    public org.purl.dc.elements._1.ObjectFactory dcElementFactory = new org.purl.dc.elements._1.ObjectFactory();
    public net.opengis.fes._2.ObjectFactory fesFactory = new net.opengis.fes._2.ObjectFactory();


    public CSWHandler(OGCConfiguration ogcConfiguration, CSWConfiguration cswConfiguration, InspireConfiguration inspireConfiguration, String baseUri) {
        this.ogcConfiguration = ogcConfiguration;
        this.cswConfiguration = cswConfiguration;
        this.inspireConfiguration = inspireConfiguration;
        if (StringUtil.isNullOrEmpty(baseUri)) {
            this.baseUri = ogcConfiguration.serverUri;
            LOGGER.warn("[arlas-ogc.serverUri] is deprecated. Use [arlas-base-uri] instead.");
        } else {
            this.baseUri = baseUri;
        }
        getCapabilitiesHandler = new GetCapabilitiesHandler(this);
        getRecordsHandler = new GetRecordsHandler(this);
        getRecordsByIdHandler = new GetRecordsByIdHandler(this);
        openSearchHandler = new OpenSearchHandler(this);

    }
}
