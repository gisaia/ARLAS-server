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

package io.arlas.server.wfs.operation.describefeaturetype;

import io.arlas.server.wfs.utils.WFSConstant;
import org.deegree.gml.GMLVersion;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;

public class DescribeFeatureTypeHandler {

    public DescribeFeatureTypeHandler() {}

    public StreamingOutput getDescribeFeatureTypeResponse(String collectionName, String uri) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException, IOException {
                try {
                    writeArlasFeatureSchema(outputStream, collectionName,uri);

                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        };
        return streamingOutput;
    }

    public void writeArlasFeatureSchema(OutputStream outputStream, String collectionName, String uri)throws XMLStreamException {

        GMLVersion gmlVersion = GMLVersion.GML_32;

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

        writer.setPrefix(WFSConstant.XS_PREFIX, WFSConstant.XSNS);
        writer.writeStartElement(WFSConstant.XSNS, "schema");
        writer.writeNamespace(WFSConstant.XS_PREFIX, WFSConstant.XSNS);
        writer.writeAttribute("attributeFormDefault", "unqualified");
        writer.writeAttribute("elementFormDefault", "qualified");
        writer.writeAttribute("targetNamespace",uri);
        writer.writeNamespace(WFSConstant.WFS_PREFIX, WFSConstant.WFS_200_NS);
        writer.writeNamespace(WFSConstant.GML_PREFIX, gmlVersion.getNamespace());
        writer.writeNamespace("arlas", uri);

        // import GML core schema
        String parentElement = null;
        String parentType = null;
        writer.writeEmptyElement(WFSConstant.XSNS, "import");
        writer.writeAttribute("namespace", gmlVersion.getNamespace());
        // there is no abstract FeatureCollection element in GML 3.2 anymore
        parentElement = WFSConstant.GML_PREFIX + ":AbstractFeature";
        parentType = WFSConstant.GML_PREFIX + ":AbstractFeatureType";
        writer.writeAttribute("schemaLocation", WFSConstant.GML_32_DEFAULT_INCLUDE);

        // write wfs:FeatureCollection element declaration
        writer.writeStartElement(WFSConstant.XSNS, "element");
        writer.writeAttribute("name", collectionName);
        writer.writeAttribute("type", "arlas:" + collectionName +"FeatureType");
        writer.writeAttribute("substitutionGroup", parentElement);
        writer.writeEndElement();

        // write wfs:FeatureCollectionType declaration
        writer.writeStartElement(WFSConstant.XSNS, "complexType");
        writer.writeAttribute("name", collectionName + "FeatureType");

        writer.writeStartElement(WFSConstant.XSNS, "complexContent");
        writer.writeStartElement(WFSConstant.XSNS, "extension");
        writer.writeAttribute("base", parentType);

        writer.writeStartElement(WFSConstant.XSNS, "sequence");
        writer.writeEmptyElement(WFSConstant.XSNS, "element");
        writer.writeAttribute("name", "id");
        writer.writeAttribute("type", WFSConstant.XS_PREFIX + ":string");
        writer.writeAttribute("minOccurs", "1");
        writer.writeAttribute("maxOccurs", "1");
        writer.writeEmptyElement(WFSConstant.XSNS, "element");
        writer.writeAttribute("name", "timestamp");
        writer.writeAttribute("type", WFSConstant.XS_PREFIX + ":long");
        writer.writeAttribute("minOccurs", "1");
        writer.writeAttribute("maxOccurs", "1");
        writer.writeEmptyElement(WFSConstant.XSNS, "element");
        writer.writeAttribute("name", "centroid");
        writer.writeAttribute("type", WFSConstant.GML_PREFIX + ":PointPropertyType");
        writer.writeAttribute("minOccurs", "1");
        writer.writeAttribute("maxOccurs", "1");

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.flush();
    }
}
