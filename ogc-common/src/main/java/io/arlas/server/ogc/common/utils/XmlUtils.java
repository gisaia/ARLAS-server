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

package io.arlas.server.ogc.common.utils;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;
import io.arlas.server.utils.FilterMatcherUtil;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.StringUtil;
import io.arlas.server.utils.TimestampTypeMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlUtils {

    protected static Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);
    public static String pointPathSubstitute;

    public static final String ELEMENT = "element";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String MIN_OCCURS = "minOccurs";

    /** These regex are taken from W3C Recommendation https://www.w3.org/TR/REC-xml/#sec-common-syn. This link https://stackoverflow.com/questions/4618757/why-is-my-regex-failing helped adapting the regex to Java **/
    private static final String ELEMENT_NAME_START_CHAR = ":|[A-Z]|_|[a-z]|[\\xC0-\\xD6]|[\\xD8-\\xF6]|[\\u00F8-\\u02FF]|[\\u0370-\\u037D]|[\\u037F-\\u1FFF]|[\\u200C-\\u200D]|[\\u2070-\\u218F]|[\\u2C00-\\u2FEF]|[\\u3001-\\uD7FF]|[\\uF900-\\uFDCF]|[\\uFDF0-\\uFFFD]|[\\uD800-\\uDB7F][\\uDC00-\\uDFFF]";
    private static final Pattern ELEMENT_NAME_START_CHAR_PATTERN = Pattern.compile(ELEMENT_NAME_START_CHAR);
    private static final Pattern ELEMENT_NAME_CHAR_PATTERN = Pattern.compile("(" + ELEMENT_NAME_START_CHAR + "|-|\\.|[0-9]|\\xB7|[\\u0300-\\u036F]|[\\u203F-\\u2040])*");

    public static void parsePropertiesXsd(Map<String, CollectionReferenceDescriptionProperty> properties, XMLStreamWriter writer, Stack<String> namespace, ArrayList<Pattern> excludeFields,
                                          Optional<Set<String>> columnFilterPredicates) throws XMLStreamException {

        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            String path = String.join(".", new ArrayList<>(namespace));
            boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
            boolean isAllowed = FilterMatcherUtil.matchesOrWithin(columnFilterPredicates, path, property.type == ElasticType.OBJECT);
            if (!excludePath && isAllowed && property.indexed) {
                if (property.type == ElasticType.OBJECT && property.properties != null) {
                    parsePropertiesXsd(property.properties, writer, namespace, excludeFields, columnFilterPredicates);
                } else {
                    writeElementForType(writer, String.join(".", new ArrayList<>(namespace)), property);
                }
            }
            namespace.pop();
        }
    }

    public static void parsePropertiesXml(Map<String, CollectionReferenceDescriptionProperty> properties, XMLStreamWriter writer, Stack<String> namespace, String uri, Object source, String prefix, ArrayList<Pattern> excludeFields) throws XMLStreamException, ArlasException {
           if (properties != null) {
               for (String key : properties.keySet()) {
                   CollectionReferenceDescriptionProperty property = properties.get(key);
                   namespace.push(key);
                   String path = String.join(".", new ArrayList<>(namespace));
                   boolean excludePath = excludeFields.stream().anyMatch(pattern -> pattern.matcher(path).matches());
                   if (!excludePath && property.indexed) {
                       if (property.type == ElasticType.OBJECT) {
                           parsePropertiesXml(property.properties, writer, namespace, uri, source, prefix, excludeFields);
                       } else {
                           Object valueObject = MapExplorer.getObjectFromPath(String.join(".", new ArrayList<>(namespace)), source);
                           if (valueObject != null && property.type != ElasticType.DATE && property.type != ElasticType.GEO_POINT && property.type != ElasticType.GEO_SHAPE) {
                               String value = valueObject.toString();
                               writeElement(writer, String.join(".", new ArrayList<>(namespace)), value, uri, prefix);
                           }else if(valueObject != null && property.type == ElasticType.DATE ){
                               SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
                               f.setTimeZone(TimeZone.getTimeZone("UTC"));
                               if (property.format == null) {
                                   property.format = CollectionReference.DEFAULT_TIMESTAMP_FORMAT;
                               }
                               Long timestamp = TimestampTypeMapper.getTimestamp(valueObject,property.format);
                               writeElement(writer, String.join(".", new ArrayList<>(namespace)), f.format(new Date(timestamp)), uri, prefix);
                           }
                       }
                   }
                   namespace.pop();
               }
           } else {
               writeElement(writer, String.join(".", new ArrayList<>(namespace)), null, uri, prefix);
           }
       }


    //Function used to avoid point in XML name element, replace point in path property of object json
    public static String replacePointPath(String originalName) {
        if (pointPathSubstitute == null) {
            pointPathSubstitute = ArlasServerConfiguration.FLATTEN_CHAR;
        }
        return originalName.replace(pointPathSubstitute, pointPathSubstitute.concat(pointPathSubstitute)).replace(".", pointPathSubstitute);
    }

    //Function used to retrieve original path property of object json after replacePointPath
    public static String retrievePointPath(String originalName) {
        if (pointPathSubstitute == null) {
            pointPathSubstitute = ArlasServerConfiguration.FLATTEN_CHAR;
        }
        return originalName.replace(pointPathSubstitute, ".").replace("..", pointPathSubstitute);
    }

    private static void writeEmptyElement(XMLStreamWriter writer, String nameToDisplay, String type, Integer minoccurs) throws XMLStreamException {
        writer.writeEmptyElement(OGCConstant.XSNS, ELEMENT);
        writer.writeAttribute(NAME, nameToDisplay);
        writer.writeAttribute(TYPE, OGCConstant.XS_PREFIX + ":" + type);
        writer.writeAttribute(MIN_OCCURS, String.valueOf(minoccurs));
    }

    private static void writeElementForType(XMLStreamWriter writer, String nameToDisplay, CollectionReferenceDescriptionProperty property) throws XMLStreamException {
        nameToDisplay = replacePointPath(nameToDisplay);
        switch (property.type) {
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
                writeEmptyElement(writer, nameToDisplay, "dateTime", 0);
        }
    }

    private static void writeElement(XMLStreamWriter xmlStream, String nameToDisplay, String value, String uri, String prefix) throws XMLStreamException, ArlasException {
        nameToDisplay = replacePointPath(nameToDisplay);
        if (!StringUtils.isBlank(nameToDisplay)) {
            /** check if the name to display respects the w3c recommendation**/
            String startChar = nameToDisplay.substring(0,1);
            Matcher sm = ELEMENT_NAME_START_CHAR_PATTERN.matcher(startChar);
            Matcher m = ELEMENT_NAME_CHAR_PATTERN.matcher(nameToDisplay);
            if (!sm.matches() || !m.matches()) {
                /** This error is thrown after response.ok() has been sent. Therefore it's written in the XML itself**/
                xmlStream.writeCharacters("\n \n");
                xmlStream.writeCharacters("ERROR WHILE WRITING XML. Element name : '" + nameToDisplay + "' is invalid");
                xmlStream.flush();
                xmlStream.close();
                throw new InternalServerErrorException("Element name : '" + nameToDisplay + "' is invalid");
            }
        }
        xmlStream.writeStartElement(prefix, nameToDisplay, uri);
        xmlStream.writeCharacters(value);
        xmlStream.writeEndElement();
    }
}
