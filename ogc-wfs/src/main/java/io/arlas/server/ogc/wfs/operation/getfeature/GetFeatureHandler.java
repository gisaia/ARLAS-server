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

package io.arlas.server.ogc.wfs.operation.getfeature;

import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.ogc.wfs.WFSHandler;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import io.arlas.server.ogc.wfs.utils.XmlUtils;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.MapExplorer;
import org.elasticsearch.search.SearchHit;
import org.geojson.GeoJsonObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


public class GetFeatureHandler {

    private WFSHandler wfsHandler;
    private String featureNamespace;

    public GetFeatureHandler(WFSHandler wfsHandler) {
        this.wfsHandler = wfsHandler;
        this.featureNamespace = wfsHandler.wfsConfiguration.featureNamespace;
    }

    public StreamingOutput getFeatureResponse(OGCConfiguration configuration, CollectionReferenceDescription collectionReference, Integer start, Integer count, List<Object> rs, String uri) {

        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
                try {
                    doGetFeatureResults(configuration, outputStream, start, count, rs, collectionReference, uri);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return streamingOutput;
    }

    public StreamingOutput getFeatureByIdResponse(Object rs, CollectionReferenceDescription collectionReference, String uri) {

        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException {
                try {
                    doGetFeatureByIdResults(outputStream, rs, collectionReference, uri);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return streamingOutput;
    }

    public void doGetFeatureByIdResults(OutputStream outputStream, Object rs, CollectionReferenceDescription collectionReference, String uri) throws XMLStreamException, ArlasException {

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
        writeFeature(rs, writer, collectionReference, uri, true);
        writer.flush();
    }

    public void doGetFeatureResults(OGCConfiguration configuration, OutputStream outputStream, Integer start, Integer count, List<Object> rs,
                                    CollectionReferenceDescription collectionReference, String uri)
            throws Exception {


        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

        writer.setPrefix(WFSConstant.WFS_PREFIX, WFSConstant.WFS_NAMESPACE_URI);
        writer.writeStartElement(WFSConstant.WFS_NAMESPACE_URI, "FeatureCollection");
        writer.writeNamespace(WFSConstant.WFS_PREFIX, WFSConstant.WFS_NAMESPACE_URI);
        writer.writeNamespace(featureNamespace, uri);
        writeNamespaceIfNotBound(writer, "xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
                uri + " " + uri + "service=WFS&version=2.0.0&request=DescribeFeatureType http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd");
        writer.writeAttribute("timeStamp", getCurrentDateTimeWithoutMilliseconds());
        QName memberElementName = new QName(WFSConstant.WFS_NAMESPACE_URI, "member", WFSConstant.WFS_PREFIX);

        // ensure that namespace for feature member elements is bound
        writeNamespaceIfNotBound(writer, memberElementName.getPrefix(), memberElementName.getNamespaceURI());

        // ensure that namespace for gml (e.g. geometry elements) is bound
        writeNamespaceIfNotBound(writer, WFSConstant.GML_PREFIX, WFSConstant.GML_NAMESPACE_URI);

        int returnMaxFeatures = (int) configuration.queryMaxFeature;
        if (count != null && (returnMaxFeatures < 1 || count.intValue() < returnMaxFeatures)) {
            returnMaxFeatures = count.intValue();
        }

        int startIndex = 0;
        if (start != null) startIndex = start;

        writeFeatureMembersStream(writer, rs, returnMaxFeatures, startIndex, memberElementName, collectionReference, uri);
        writer.flush();


    }

    protected String getCurrentDateTimeWithoutMilliseconds() {
        long msSince1970 = new Date().getTime();
        msSince1970 = msSince1970 / 1000 * 1000;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return simpleDateFormat.format(new Date(msSince1970));
    }

    private void writeFeatureMembersStream(XMLStreamWriter xmlStream, List<Object> rs,
                                           int maxFeatures, int startIndex,
                                           QName featureMemberEl, CollectionReferenceDescription collectionReference, String uri)
            throws XMLStreamException, FactoryConfigurationError, ArlasException {

        xmlStream.writeAttribute("numberMatched", String.valueOf(rs.size()));
        xmlStream.writeAttribute("numberReturned", String.valueOf(rs.size()));
        // retrieve and write result features
        int featuresAdded = 0;
        if (rs.size() > 0) {
            for (Object member : rs) {
                if (featuresAdded == maxFeatures) {
                    // limit the number of features written to maxfeatures
                    break;
                }
                writeMemberFeature(member, xmlStream, featureMemberEl, collectionReference, uri);
                featuresAdded++;
            }
        }
        xmlStream.writeEndElement();
    }

    protected void writeMemberFeature(Object member, XMLStreamWriter xmlStream, QName featureMemberEl, CollectionReferenceDescription collectionReference, String uri)
            throws XMLStreamException, ArlasException {
        xmlStream.writeStartElement(featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart());
        writeFeature(member, xmlStream, collectionReference, uri, false);
        xmlStream.writeEndElement();
    }


    protected void writeFeature(Object member, XMLStreamWriter xmlStream, CollectionReferenceDescription
            collectionReference, String uri, Boolean isByID) throws XMLStreamException, ArlasException {

        String collectionName = collectionReference.collectionName;
        String idPath = collectionReference.params.idPath;
        String geometryPath = collectionReference.params.geometryPath;
        String timestampPath = collectionReference.params.timestampPath;

        GeoJsonObject geometry = null;
        Object source = ((SearchHit) member).getSourceAsMap();
        String id = null;
        if (idPath != null) {
            id = "" + MapExplorer.getObjectFromPath(idPath, source);
        }
        if (geometryPath != null) {
            Object m = MapExplorer.getObjectFromPath(geometryPath, source);
            if (m != null) {
                geometry = GeoTypeMapper.getGeoJsonObject(m);

            }
        }
        String timestamp = null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");
        if (timestampPath != null) {
            timestamp = "" + MapExplorer.getObjectFromPath(timestampPath, source);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        if (id != null & geometry != null) {
            xmlStream.writeStartElement(featureNamespace, collectionName, uri);
            if (isByID) {
                xmlStream.writeNamespace(featureNamespace, uri);

            }
            writeNamespaceIfNotBound(xmlStream, featureNamespace, uri);
            writeNamespaceIfNotBound(xmlStream, WFSConstant.GML_PREFIX, WFSConstant.GML_NAMESPACE_URI);
            xmlStream.writeAttribute(WFSConstant.GML_NAMESPACE_URI, "id", id);

            //Write polygon
            xmlStream.writeStartElement(featureNamespace, XmlUtils.replacePointPath((geometryPath)), uri);
            writeNamespaceIfNotBound(xmlStream, featureNamespace, uri);
            GeoFormat.geojson2gml(geometry, xmlStream, "Geom_" + id);
            xmlStream.writeEndElement();

            //Write Attributes
            ArrayList<Pattern> excludeFields = new ArrayList<>();
            if (collectionReference.params.excludeWfsFields != null) {
                Arrays.asList(collectionReference.params.excludeWfsFields.split(",")).forEach(field -> {
                    excludeFields.add(Pattern.compile("^" + field.replace(".", "\\.").replace("*", ".*") + "$"));
                });
            }
            XmlUtils.parsePropertiesXml(collectionReference.properties, xmlStream, new Stack<>(), uri, source, featureNamespace, excludeFields);
            //Write time
            xmlStream.writeStartElement(featureNamespace, XmlUtils.replacePointPath((timestampPath)).concat("_time"), uri);
            xmlStream.writeCharacters(f.format(new Date(Long.parseLong(timestamp))));
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }
    }

    public static void writeNamespaceIfNotBound(XMLStreamWriter xmlStream, String prefix, String nsUri) throws XMLStreamException {
        if (!prefix.equals(xmlStream.getPrefix(nsUri))) {
            xmlStream.writeNamespace(prefix, nsUri);
        }
    }
}
