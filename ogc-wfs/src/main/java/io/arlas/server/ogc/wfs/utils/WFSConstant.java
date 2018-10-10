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

package io.arlas.server.ogc.wfs.utils;

import io.arlas.server.ns.GML;

public class WFSConstant {

    public static final String GET_FEATURE_BY_ID_NAME = "urn:ogc:def:query:OGC-WFS::GetFeatureById";
    public final static String DEFAULT_LANGUAGE = "urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression";
    public static final String WFS_NAMESPACE_URI = "http://www.opengis.net/wfs/2.0";
    public static final String FES_NAMESPACE_URI = "http://www.opengis.net/fes/2.0";
    public static final String GML_NAMESPACE_URI = GML.XML_NS;
    public static final String XS_PREFIX = "xs";
    public static final String WFS_PREFIX = "wfs";
    public static final String GML_PREFIX = GML.XML_PREFIX;
    public static final String XSNS = "http://www.w3.org/2001/XMLSchema";
    public static final String WFS = "WFS";
    public static final String SUPPORTED_WFS_VERSION = "2.0.0";
    public static final String[] SUPPORTED_CRS = {"http://www.opengis.net/def/crs/epsg/0/4326", "urn:ogc:def:crs:EPSG::4326"};
    public static final String[] FEATURE_GML_FORMAT = {"application/gml+xml; version=3.2", "text/xml; subtype=gml/3.2"};
    public static final String[] SUPPORTED_WFS_REQUESTYPE = {"GetCapabilities", "DescribeFeatureType", "GetFeature", "GetPropertyValue", "ListStoredQueries", "DescribeStoredQueries"};
}
