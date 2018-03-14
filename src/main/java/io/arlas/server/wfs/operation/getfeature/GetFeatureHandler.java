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

package io.arlas.server.wfs.operation.getfeature;

import io.arlas.server.app.WFSConfiguration;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import io.arlas.server.model.response.ElasticType;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.TimestampTypeMapper;
import io.arlas.server.wfs.WFSHandler;
import io.arlas.server.wfs.utils.Version;
import io.arlas.server.wfs.utils.WFSConstant;
import io.arlas.server.wfs.utils.XmlUtils;
import org.elasticsearch.search.SearchHit;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.*;
import java.text.SimpleDateFormat;


public class GetFeatureHandler {

    private WFSHandler wfsHandler;
    private String featureNamespace;

    public GetFeatureHandler(WFSHandler wfsHandler) {
        this.wfsHandler=wfsHandler;
        this.featureNamespace=wfsHandler.wfsConfiguration.featureNamespace;
    }

    public StreamingOutput getFeatureResponse(WFSConfiguration configuration, CollectionReferenceDescription collectionReference, Integer start, Integer count, List<Object> rs, String uri) {

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
        writeFeature(rs, writer, collectionReference, uri);
        writer.flush();
    }

    public void doGetFeatureResults(WFSConfiguration configuration, OutputStream outputStream, Integer start, Integer count, List<Object> rs,
                                    CollectionReferenceDescription collectionReference, String uri)
            throws Exception {

        Version version = Version.parseVersion(WFSConstant.SUPPORTED_WFS_VERSION);

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

        writer.setPrefix(WFSConstant.WFS_PREFIX, WFSConstant.WFS_NAMESPACE_URI);
        writer.writeStartElement(WFSConstant.WFS_NAMESPACE_URI, "FeatureCollection");
        writeNamespaceIfNotBound(writer, "xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd");
        writer.writeNamespace(WFSConstant.WFS_PREFIX, WFSConstant.WFS_NAMESPACE_URI);
        writer.writeNamespace(featureNamespace, uri);

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
        writeFeature(member, xmlStream, collectionReference, uri);
        xmlStream.writeEndElement();
    }


    protected void writeFeature(Object member, XMLStreamWriter xmlStream, CollectionReferenceDescription
            collectionReference, String uri) throws XMLStreamException, ArlasException {

        String collectionName = collectionReference.collectionName;
        String idPath = collectionReference.params.idPath;
        String geometryPath = collectionReference.params.geometryPath;
        GeoJsonObject geometry = null;
        Object source = ((SearchHit) member).getSourceAsMap();
        String id = null;
        if (idPath != null) {
            id = ""+ MapExplorer.getObjectFromPath(idPath, source);
        }
        if (geometryPath != null) {
            Object m = MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source);
            if (m != null) {
                geometry = GeoTypeMapper.getGeoJsonObject(m);

            }
        }
        if (id != null & geometry != null) {
            xmlStream.writeStartElement(featureNamespace, collectionName, uri);
            xmlStream.writeNamespace(featureNamespace, uri);
            writeNamespaceIfNotBound(xmlStream, WFSConstant.GML_PREFIX, WFSConstant.GML_NAMESPACE_URI);
            xmlStream.writeAttribute(WFSConstant.GML_NAMESPACE_URI, "id", id);
            //Write polygon
            xmlStream.writeStartElement(featureNamespace, geometryPath, uri);
            writeNamespaceIfNotBound(xmlStream, featureNamespace, uri);
            xmlStream.writeStartElement(WFSConstant.GML_NAMESPACE_URI, "Polygon");
            xmlStream.writeAttribute(WFSConstant.GML_NAMESPACE_URI, "id", "Polygon_" + id);
            xmlStream.writeAttribute("srsName", "http://www.opengis.net/def/crs/epsg/0/4326");
            xmlStream.writeStartElement(WFSConstant.GML_NAMESPACE_URI, "exterior");
            xmlStream.writeStartElement(WFSConstant.GML_NAMESPACE_URI, "LinearRing");
            xmlStream.writeStartElement(WFSConstant.GML_NAMESPACE_URI, "posList");
            String posList = "";
            for (LngLatAlt lnglnglat : ((Polygon) geometry).getExteriorRing()) {
                posList = posList.concat(" ");
                posList = posList.concat(String.valueOf(lnglnglat.getLatitude()));
                posList = posList.concat(" ");
                posList = posList.concat(String.valueOf(lnglnglat.getLongitude()));
            }
            xmlStream.writeCharacters(posList.trim());
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
            //Write Attributes
            XmlUtils.parsePropertiesXml(collectionReference.properties,xmlStream,new Stack<String>(),uri,source,featureNamespace);
            xmlStream.writeEndElement();
        }

    }

    public static void writeNamespaceIfNotBound(XMLStreamWriter xmlStream, String prefix, String nsUri) throws XMLStreamException {
        if (!prefix.equals(xmlStream.getPrefix(nsUri))) {
            xmlStream.writeNamespace(prefix, nsUri);
        }
    }
}
