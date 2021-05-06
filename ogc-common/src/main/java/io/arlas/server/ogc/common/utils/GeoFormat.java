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

import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ns.GML;
import io.arlas.server.ogc.common.model.Service;
import org.geojson.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.List;


public class GeoFormat {

    public static void geojson2gml(GeoJsonObject geojson, XMLStreamWriter writer, String id) throws XMLStreamException {
        geojson2gml(geojson, writer, id, "urn:ogc:def:crs:EPSG::4326");
    }

    public static void geojson2gml(GeoJsonObject geojson, XMLStreamWriter writer, String id, String srsName) throws XMLStreamException {
        if (geojson == null) {
            return;
        }
        if (geojson instanceof Point) {
            Point g = (Point) geojson;
            writer.writeStartElement(GML.XML_NS, "Point");
            writer.writeAttribute(GML.XML_NS, "id", id);
            writer.writeAttribute("srsName", srsName);
            if (g.getCoordinates() != null) {
                writer.writeStartElement(GML.XML_NS, "pos");
                writer.writeCharacters(g.getCoordinates().getLatitude() + " " + g.getCoordinates().getLongitude());
                writer.writeEndElement();
            }
            writer.writeEndElement();
            return;
        }

        if(geojson instanceof MultiPoint){
            MultiPoint multiPoint = (MultiPoint) geojson;
            writer.writeStartElement(GML.XML_NS, "MultiPoint");
            writer.writeAttribute(GML.XML_NS, "id", id);
            writer.writeAttribute("srsName", srsName);
            int i =0;
            for(LngLatAlt lngLatAlt :multiPoint.getCoordinates()){
                writer.writeStartElement(GML.XML_NS, "pointMember");
                writer.writeStartElement(GML.XML_NS, "Point");
                writer.writeAttribute(GML.XML_NS, "id", id.concat("-").concat(String.valueOf(i)));
                writer.writeAttribute("srsName", srsName);
                if (lngLatAlt != null) {
                    writer.writeStartElement(GML.XML_NS, "pos");
                    writer.writeCharacters(lngLatAlt.getLatitude() + " " + lngLatAlt.getLongitude());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.writeEndElement();

            }
            writer.writeEndElement();
            return;
        }

        if (geojson instanceof LineString) {
            LineString p = (LineString) geojson;
            writer.writeStartElement(GML.XML_NS, "LineString");
            writer.writeAttribute(GML.XML_NS, "id", id);
            writer.writeAttribute("srsName", srsName);
            if (p.getCoordinates() != null && p.getCoordinates().size() > 0) {
                {
                    writer.writeStartElement(GML.XML_NS, "posList");
                    String posList = "";
                    for (LngLatAlt lnglnglat : p.getCoordinates()) {
                        posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                    }
                    writer.writeCharacters(posList.trim());
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
            }
            return;
        }

        if(geojson instanceof MultiLineString){
            MultiLineString multiLineString = (MultiLineString) geojson;
            writer.writeStartElement(GML.XML_NS, "MultiLineString");
            writer.writeAttribute(GML.XML_NS, "id", id);
            writer.writeAttribute("srsName", srsName);
            int i =0;
            for(List<LngLatAlt> coordinates :multiLineString.getCoordinates()){
                writer.writeStartElement(GML.XML_NS, "lineStringMember");
                writer.writeStartElement(GML.XML_NS, "LineString");
                writer.writeAttribute(GML.XML_NS, "id", id.concat("-").concat(String.valueOf(i)));
                writer.writeAttribute("srsName", srsName);
                if (coordinates != null && coordinates.size()>0 ) {
                    {
                        writer.writeStartElement(GML.XML_NS, "posList");
                        String posList = "";
                        for (LngLatAlt lnglnglat : coordinates) {
                            posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                        }
                        writer.writeCharacters(posList.trim());
                        writer.writeEndElement();
                    }
                }
                writer.writeEndElement();
                writer.writeEndElement();

            }
            writer.writeEndElement();
            return;
        }


        if (geojson instanceof Polygon) {
            Polygon p = (Polygon) geojson;

            if (p.getCoordinates() != null && p.getCoordinates().size() > 0) {
                writer.writeStartElement(GML.XML_NS, "Polygon");
                writer.writeAttribute(GML.XML_NS, "id", id);
                writer.writeAttribute("srsName", srsName);
                    writer.writeStartElement(GML.XML_NS, "exterior");
                    writer.writeStartElement(GML.XML_NS, "LinearRing");
                    writer.writeStartElement(GML.XML_NS, "posList");
                    String posList = "";
                    for (LngLatAlt lnglnglat : p.getExteriorRing()) {
                        posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                    }
                    writer.writeCharacters(posList.trim());
                    writer.writeEndElement();
                    writer.writeEndElement();
                    writer.writeEndElement();


                for (List<LngLatAlt> ring : p.getInteriorRings()) {
                    writer.writeStartElement(GML.XML_NS, "interior");
                    writer.writeStartElement(GML.XML_NS, "LinearRing");
                    writer.writeStartElement(GML.XML_NS, "posList");
                    posList = "";
                    for (LngLatAlt lnglnglat : ring) {
                        posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                    }
                    writer.writeCharacters(posList.trim());
                    writer.writeEndElement();
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            return;
        }
        if(geojson instanceof MultiPolygon){
            MultiPolygon multiPolygon = (MultiPolygon) geojson;
            writer.writeStartElement(GML.XML_NS, "MultiPolygon");
            writer.writeAttribute(GML.XML_NS, "id", id);
            writer.writeAttribute("srsName", srsName);
            int i =0;
            for(List<List<LngLatAlt>> coords : multiPolygon.getCoordinates()){
                Polygon p = new Polygon();
                p.setCoordinates(coords);
                if (p.getCoordinates() != null && p.getCoordinates().size() > 0) {
                    writer.writeStartElement(GML.XML_NS, "polygonMember");
                    writer.writeStartElement(GML.XML_NS, "Polygon");
                    writer.writeAttribute(GML.XML_NS, "id", id.concat("-").concat(String.valueOf(i)));
                    writer.writeAttribute("srsName", srsName);
                    writer.writeStartElement(GML.XML_NS, "exterior");
                    writer.writeStartElement(GML.XML_NS, "LinearRing");
                    writer.writeStartElement(GML.XML_NS, "posList");
                    String posList = "";
                    for (LngLatAlt lnglnglat : p.getExteriorRing()) {
                        posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                    }
                    writer.writeCharacters(posList.trim());
                    writer.writeEndElement();
                    writer.writeEndElement();
                    writer.writeEndElement();
                    for (List<LngLatAlt> ring : p.getInteriorRings()) {
                        writer.writeStartElement(GML.XML_NS, "interior");
                        writer.writeStartElement(GML.XML_NS, "LinearRing");
                        writer.writeStartElement(GML.XML_NS, "posList");
                        posList = "";
                        for (LngLatAlt lnglnglat : ring) {
                            posList += String.valueOf(lnglnglat.getLatitude()) + " " + String.valueOf(lnglnglat.getLongitude()) + " ";
                        }
                        writer.writeCharacters(posList.trim());
                        writer.writeEndElement();
                        writer.writeEndElement();
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
                i++;
            }
            writer.writeEndElement();
            return;
        }
        throw new RuntimeException("Unsupported geometry type for " + geojson.toString());
    }

    public static double[] toDoubles(String bbox, Service service) throws OGCException {
        try {
            return Arrays.stream(bbox.split(",")).limit(4).mapToDouble(Double::parseDouble).toArray();
        } catch (Exception e) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid BBOX", "BBOX", service);
        }
    }
}
