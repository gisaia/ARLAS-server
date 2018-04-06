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

import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;
import io.arlas.server.utils.MapExplorer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

public class XmlUtils {


    public static String pointPathSubstitute;

    public static final String ELEMENT = "element";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String MIN_OCCURS = "minOccurs";

    public static void parsePropertiesXsd(Map<String, CollectionReferenceDescriptionProperty> properties, XMLStreamWriter writer, Stack<String> namespace, ArrayList<Pattern> excludeFields) throws XMLStreamException {
        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            String path = String.join(".", new ArrayList<>(namespace));
            boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
            if (!excludePath) {
                if (property.type == ElasticType.OBJECT) {
                    parsePropertiesXsd(property.properties, writer, namespace, excludeFields);
                } else {
                    writeElementForType(writer, String.join(".", new ArrayList<>(namespace)), property.type);
                }
            }
            namespace.pop();
        }
    }

    public static void parsePropertiesXml(Map<String, CollectionReferenceDescriptionProperty> properties, XMLStreamWriter writer, Stack<String> namespace, String uri, Object source, String prefix, ArrayList<Pattern> excludeFields) throws XMLStreamException {
        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            String path = String.join(".", new ArrayList<>(namespace));
            boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
            if (!excludePath) {
                if (property.type == ElasticType.OBJECT) {
                    parsePropertiesXml(property.properties, writer, namespace, uri, source, prefix, excludeFields);
                } else {
                    Object valueObject = MapExplorer.getObjectFromPath(String.join(".", new ArrayList<>(namespace)), source);
                    if(valueObject!=null && property.type!= ElasticType.GEO_POINT&& property.type!= ElasticType.GEO_SHAPE){
                        String value = valueObject.toString();
                        writeElement(writer, String.join(".", new ArrayList<>(namespace)), value, uri, prefix);
                    }
                }
            }
            namespace.pop();
        }
    }

    //Function used to avoid point in XML name element, replace point in path property of object json
    public static String replacePointPath(String originalName) {
        if (pointPathSubstitute == null) {
            pointPathSubstitute = "_";
        }
        return originalName.replace(pointPathSubstitute, pointPathSubstitute.concat(pointPathSubstitute)).replace(".", pointPathSubstitute);
    }

    //Function used to retrieve original path property of object json after replacePointPath
    public static String retrievePointPath(String originalName) {
        if (pointPathSubstitute == null) {
            pointPathSubstitute = "_";
        }
        return originalName.replace(pointPathSubstitute, ".").replace("..", pointPathSubstitute);
    }

    private static void writeEmptyElement(XMLStreamWriter writer, String nameToDisplay, String type, Integer minoccurs) throws XMLStreamException {
        writer.writeEmptyElement(WFSConstant.XSNS, ELEMENT);
        writer.writeAttribute(NAME, nameToDisplay);
        writer.writeAttribute(TYPE, WFSConstant.XS_PREFIX + ":" + type);
        writer.writeAttribute(MIN_OCCURS, String.valueOf(minoccurs));
    }

    private static void writeElementForType(XMLStreamWriter writer, String nameToDisplay, ElasticType type) throws XMLStreamException {
        nameToDisplay = replacePointPath(nameToDisplay);
        switch (type) {
            case KEYWORD:
            case TEXT:
                writeEmptyElement(writer, nameToDisplay, "string", 0);
                break;
            case LONG:
                writeEmptyElement(writer, nameToDisplay, "long", 0);
                break;
            case INTEGER:
                writeEmptyElement(writer, nameToDisplay, "integer", 0);
                break;
            case DOUBLE:
                writeEmptyElement(writer, nameToDisplay, "double", 0);
                break;
            case BOOLEAN:
                writeEmptyElement(writer, nameToDisplay, "boolean", 0);
                break;
            case DATE:
                writeEmptyElement(writer, nameToDisplay, "long", 0);
        }
    }

    private static void writeElement(XMLStreamWriter xmlStream, String nameToDisplay, String value, String uri, String prefix) throws XMLStreamException {
        nameToDisplay = replacePointPath(nameToDisplay);
        xmlStream.writeStartElement(prefix, nameToDisplay, uri);
        xmlStream.writeCharacters(value);
        xmlStream.writeEndElement();
    }
}
