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

package io.arlas.server.wfs;

import io.arlas.server.app.WFSConfiguration;
import io.arlas.server.wfs.operation.describefeaturetype.DescribeFeatureTypeHandler;
import io.arlas.server.wfs.operation.getcapabilities.GetCapabilitiesHandler;
import io.arlas.server.wfs.operation.getfeature.GetFeatureHandler;
import io.arlas.server.wfs.operation.storedquery.ListStoredQueriesHandler;
import io.arlas.server.wfs.operation.storedquery.StoredQueryManager;
import net.opengis.wfs._2.ObjectFactory;
import javax.xml.parsers.ParserConfigurationException;

public class WFSHandler {

    public WFSConfiguration wfsConfiguration;
    public GetCapabilitiesHandler getCapabilitiesHandler;
    public DescribeFeatureTypeHandler describeFeatureTypeHandler;
    public ListStoredQueriesHandler listStoredQueriesHandler;
    public GetFeatureHandler getFeatureHandler;
    public ObjectFactory wfsFactory = new ObjectFactory();
    public net.opengis.ows._1.ObjectFactory owsFactory = new net.opengis.ows._1.ObjectFactory();
    public net.opengis.fes._2.ObjectFactory fesFactory = new net.opengis.fes._2.ObjectFactory();
    public StoredQueryManager storedQueryManager = new StoredQueryManager();

    public WFSHandler(WFSConfiguration configuration) throws  ParserConfigurationException {
        wfsConfiguration =configuration;
        getCapabilitiesHandler = new GetCapabilitiesHandler(configuration,this);
        describeFeatureTypeHandler = new DescribeFeatureTypeHandler(this);
        listStoredQueriesHandler = new ListStoredQueriesHandler(this);
        getFeatureHandler = new GetFeatureHandler(this);
    }
}
