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

package io.arlas.server.ogc.wfs.operation.describefeaturetype;

import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.common.utils.XmlUtils;
import io.arlas.server.core.utils.ColumnFilterUtil;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;

public class DescribeFeatureTypeHandler {

    private WFSHandler wfsHandler;

    public DescribeFeatureTypeHandler(WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
    }

    public StreamingOutput getDescribeFeatureTypeResponse(CollectionReference collectionReference, String uri, Optional<String> columnFilter) {

        StreamingOutput streamingOutput = outputStream -> {
            try {
                writeArlasFeatureSchema(outputStream, collectionReference, uri, columnFilter);

            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        };
        return streamingOutput;
    }

    public void writeArlasFeatureSchema(OutputStream outputStream, CollectionReference collectionReference, String uri, Optional<String> columnFilter) throws XMLStreamException {

        String collectionName = collectionReference.collectionName;
        String geometryPath = collectionReference.params.geometryPath;

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

        writer.setPrefix(WFSConstant.XS_PREFIX, WFSConstant.XSNS);
        writer.writeStartElement(WFSConstant.XSNS, "schema");
        writer.writeNamespace(WFSConstant.XS_PREFIX, WFSConstant.XSNS);
        writer.writeAttribute("attributeFormDefault", "unqualified");
        writer.writeAttribute("elementFormDefault", "qualified");
        writer.writeAttribute("targetNamespace", uri);
        writer.writeNamespace(WFSConstant.WFS_PREFIX, WFSConstant.WFS_NAMESPACE_URI);
        writer.writeNamespace(WFSConstant.GML_PREFIX, WFSConstant.GML_NAMESPACE_URI);
        writer.writeNamespace(wfsHandler.wfsConfiguration.featureNamespace, uri);

        // import GML core schema
        String parentElement = null;
        String parentType = null;
        writer.writeEmptyElement(WFSConstant.XSNS, "import");

        writer.writeAttribute("namespace", WFSConstant.GML_NAMESPACE_URI);
        // there is no abstract FeatureCollection element in GML 3.2 anymore
        parentElement = WFSConstant.GML_PREFIX + ":AbstractFeature";
        parentType = WFSConstant.GML_PREFIX + ":AbstractFeatureType";
        writer.writeAttribute("schemaLocation", WFSConstant.GML_NAMESPACE_URI);

        // write wfs:FeatureCollection element declaration
        writer.writeStartElement(WFSConstant.XSNS, "element");
        writer.writeAttribute("name", collectionName);

        writer.writeAttribute("type", "arlas:" + collectionName + "FeatureType");
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
        writer.writeAttribute("name", XmlUtils.replacePointPath((geometryPath)));
        writer.writeAttribute(XmlUtils.TYPE, WFSConstant.GML_PREFIX + ":GeometryPropertyType");
        writer.writeAttribute("minOccurs", "1");
        writer.writeAttribute("maxOccurs", "1");

        ArrayList<Pattern> excludeFields = new ArrayList<>();
        if (collectionReference.params.excludeWfsFields != null) {
            Arrays.asList(collectionReference.params.excludeWfsFields.split(",")).forEach(field -> {
                excludeFields.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + "$"));
            });
        }
        XmlUtils.parsePropertiesXsd(
                ((CollectionReferenceDescription) collectionReference).properties,
                writer,
                new Stack<String>(),
                excludeFields,
                ColumnFilterUtil.getColumnFilterPredicates(columnFilter, collectionReference));


        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.flush();
    }
}
