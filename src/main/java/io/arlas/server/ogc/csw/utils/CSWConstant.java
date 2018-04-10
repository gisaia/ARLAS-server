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

public class CSWConstant {

    public static final String CSW = "CSW";
    public static final String SUPPORTED_CSW_VERSION = "3.0.0";
    public static final String[] SUPPORTED_CSW_REQUESTYPE = {"GetCapabilities", "GetRecords", "GetRecordById"};
    public static final String[] SUPPORTED_CSW_OUTPUT_SCHEMA = {"http://www.opengis.net/cat/csw/3.0", "http://www.w3.org/2005/Atom"};
    public static final String[] SUPPORTED_CSW_OUTPUT_FORMAT = {"application/xml", "application/atom+xml"};
    public static final String[] SUPPORTED_CSW_ACCEPT_FORMATS = {"text/xml","application/xml"};
    public static final String[] SECTION_NAMES = {"ServiceIdentification", "ServiceProvider", "OperationsMetadata","Filter_Capabilities","All"};



}
