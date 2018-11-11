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

import net.opengis.wfs._2.WFSCapabilitiesType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;

@XmlAccessorType(XmlAccessType.FIELD)
public class ExtendedWFSCapabilitiesType extends WFSCapabilitiesType {

    private final static QName _WFSCapabilities_QNAME = new QName("http://www.opengis.net/wfs/2.0", "WFS_Capabilities");

    @XmlAttribute(name="xsi:schemaLocation")
    protected final String xsi_schemaLocation="http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd";

    @XmlAttribute(name="xmlns:xsi")
    protected final String xmlns_xsi="http://www.w3.org/2001/XMLSchema-instance";

    public ExtendedWFSCapabilitiesType() {
        super();
    }

    @XmlElementDecl(namespace = "http://www.opengis.net/wfs/2.0", name = "WFS_Capabilities")
    public static JAXBElement<ExtendedWFSCapabilitiesType> createWFSCapabilities(ExtendedWFSCapabilitiesType value) {
        return new JAXBElement<ExtendedWFSCapabilitiesType>(_WFSCapabilities_QNAME, ExtendedWFSCapabilitiesType.class, null, value);
    }
}
