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

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.response.MD;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.tom.ows.Version;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;


import static org.deegree.commons.xml.stax.XMLStreamUtils.writeNamespaceIfNotBound;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

public class GetFeatureHandler {

    Logger LOGGER = LoggerFactory.getLogger(GetFeatureHandler.class);

    public StreamingOutput getFeatureResponse(ArlasServerConfiguration configuration, CollectionReference collectionReference, Integer start, BigInteger count, List<Object> rs,String uri) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException, IOException {
                try {
                    doGetFeatureResults( configuration, outputStream,  start, count, rs,collectionReference,uri);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return streamingOutput;
    }

    public StreamingOutput getFeatureByIdResponse(ArlasServerConfiguration configuration,Object rs,CollectionReference collectionReference,String uri) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws WebApplicationException, IOException {
                try {
                    doGetFeatureByIdResults( configuration, outputStream, rs,collectionReference,uri);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return streamingOutput;
    }


    public void doGetFeatureByIdResults(ArlasServerConfiguration configuration,OutputStream outputStream, Object rs,CollectionReference collectionReference,String uri) throws XMLStreamException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
        writeFeature(rs,writer,collectionReference,uri);
        writer.flush();
    }

    public void doGetFeatureResults(ArlasServerConfiguration configuration,OutputStream outputStream, Integer start,BigInteger count,List<Object> rs,
                                    CollectionReference collectionReference,String uri)
            throws Exception {

        GMLVersion gmlVersion = GMLVersion.GML_32;
        Version version = new Version(2,0,0);

        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);

        writer.setPrefix( "wfs", WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "FeatureCollection" );
        writer.writeNamespace( "wfs", WFS_200_NS );
        writer.writeNamespace("arlas",uri);

        writer.writeAttribute( "timeStamp", getTimestamp() );
        QName memberElementName = new QName( WFS_200_NS, "member", "wfs" );

        // ensure that namespace for feature member elements is bound
        writeNamespaceIfNotBound( writer, memberElementName.getPrefix(), memberElementName.getNamespaceURI() );

        // ensure that namespace for gml (e.g. geometry elements) is bound
        writeNamespaceIfNotBound( writer, "gml", "http://www.opengis.net/gml/3.2" );

        int returnMaxFeatures = (int) configuration.wfsConfiguration.queryMaxFeature;
        if ( count != null && ( returnMaxFeatures < 1 || count.intValue() < returnMaxFeatures ) ) {
            returnMaxFeatures = count.intValue();
        }

        int startIndex = 0;
        if ( start != null ) startIndex = start;

        GMLStreamWriter gmlStream = createGMLStreamWriter(  gmlVersion, writer );
        writeFeatureMembersStream( version, gmlStream, rs, gmlVersion, returnMaxFeatures,startIndex, memberElementName ,collectionReference,uri);
        writer.flush();
    }

    protected String getTimestamp() {
        DateTime dateTime = getCurrentDateTimeWithoutMilliseconds();
        return ISO8601Converter.formatDateTime( dateTime );
    }

    protected DateTime getCurrentDateTimeWithoutMilliseconds() {
        long msSince1970 = new Date().getTime();
        msSince1970 = msSince1970 / 1000 * 1000;
        return new DateTime( new Date( msSince1970 ), TimeZone.getTimeZone( "GMT" ) );
    }

    private void writeFeatureMembersStream( Version wfsVersion, GMLStreamWriter gmlStream, List<Object> rs,
                                            GMLVersion outputFormat, int maxFeatures, int startIndex,
                                            QName featureMemberEl,CollectionReference collectionReference,String uri)
            throws XMLStreamException,
            FactoryConfigurationError {

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();

        if ( wfsVersion.equals( VERSION_200 ) ) {
            xmlStream.writeAttribute( "numberMatched", "1" );
            xmlStream.writeAttribute( "numberReturned", "1" );
        }

        // retrieve and write result features
        int featuresAdded = 0;
        int featuresSkipped = 0;
                if(rs.size()>0){
                    for ( Object member : rs ) {
                        if ( featuresAdded == maxFeatures ) {
                            // limit the number of features written to maxfeatures
                            break;
                        }
                        if ( featuresSkipped < startIndex ) {
                            featuresSkipped++;
                        } else {
                            writeMemberFeature( member, xmlStream, featureMemberEl,collectionReference,uri );
                            featuresAdded++;
                        }
                    }
                }
        xmlStream.writeEndElement();
    }

    protected void writeMemberFeature( Object member,  XMLStreamWriter xmlStream,QName featureMemberEl,CollectionReference collectionReference,String uri )
            throws XMLStreamException{
        xmlStream.writeStartElement( featureMemberEl.getNamespaceURI(), featureMemberEl.getLocalPart() );
        writeFeature(member,xmlStream,collectionReference,uri);
        xmlStream.writeEndElement();
    }

    protected void writeFeature(Object member,  XMLStreamWriter xmlStream,CollectionReference collectionReference,String uri) throws XMLStreamException{

        xmlStream.writeStartElement("arlas",collectionReference.collectionName,uri);
        writeNamespaceIfNotBound( xmlStream, "arlas", uri );
        writeNamespaceIfNotBound( xmlStream, "gml", "http://www.opengis.net/gml/3.2" );

        xmlStream.writeAttribute("http://www.opengis.net/gml/3.2","id",((MD)member).id);
        xmlStream.writeStartElement("centroid");
        xmlStream.writeStartElement("http://www.opengis.net/gml/3.2","Point");
        xmlStream.writeAttribute("http://www.opengis.net/gml/3.2","id",((MD)member).id);
        xmlStream.writeAttribute("srsName","urn:ogc:def:crs:EPSG::4326");
        xmlStream.writeStartElement("http://www.opengis.net/gml/3.2","pos");
        String lat = String.valueOf(((Point)((MD)member).centroid).getCoordinates().getLatitude());
        String lng = String.valueOf(((Point)((MD)member).centroid).getCoordinates().getLongitude());
        xmlStream.writeCharacters(lat.concat(" ").concat(lng));
        xmlStream.writeEndElement();
        xmlStream.writeEndElement();
        xmlStream.writeEndElement();
        xmlStream.writeStartElement("id");
        xmlStream.writeCharacters(((MD) member).id);
        xmlStream.writeEndElement();
        if(((MD) member).timestamp!=null){
            xmlStream.writeStartElement("timestamp");
            xmlStream.writeCharacters(((MD) member).timestamp.toString());
            xmlStream.writeEndElement();
        }
        xmlStream.writeEndElement();
    }
}
