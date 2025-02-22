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

package io.arlas.server.opensearch.rest.explore;

import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.utils.StringUtil;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.Feed;
import io.arlas.server.core.model.response.*;
import io.arlas.server.core.ns.ATOM;
import io.arlas.server.core.ns.GEORSS;
import io.arlas.server.core.ns.GML;
import io.arlas.server.core.ns.OPENSEARCH;
import io.arlas.server.core.services.ExploreService;
import io.arlas.server.core.utils.MapExplorer;
import io.arlas.server.ogc.common.utils.GeoFormat;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

@Provider
@Produces(ATOM.APPLICATION_ATOM_XML)
public class AtomHitsMessageBodyWriter implements MessageBodyWriter<Hits> {

    private final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM.dd'T'hh:mm:ssZ");

    private ExploreService exploreService;

    public AtomHitsMessageBodyWriter(ExploreService exploreService) {
        this.exploreService = exploreService;
    }


    @Override
    public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass != null && Hits.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(Hits hits, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return -1;
    }

    @Override
    public void writeTo(Hits hits, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws WebApplicationException {
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(outputStream);
            writer.writeStartDocument();


            writer.setPrefix(ATOM.XML_PREFIX, ATOM.XML_NS);
            writer.writeStartElement(ATOM.XML_NS, "feed");
            writer.writeNamespace(ATOM.XML_PREFIX, ATOM.XML_NS);
            writer.writeNamespace(OPENSEARCH.XML_PREFIX, OPENSEARCH.XML_NS);
            writer.writeNamespace(GEORSS.XML_PREFIX, GEORSS.XML_NS);
            writer.writeNamespace(GML.XML_PREFIX, GML.XML_NS);

            writeElement(writer, ATOM.XML_NS, "id", hits.collection);
            writeElement(writer, ATOM.XML_NS, "title", hits.collection);
            writeElement(writer, ATOM.XML_NS, "updated", dateFormater.format(new Date()));

            writeElement(writer, OPENSEARCH.XML_NS, "totalResults", "" + hits.totalnb);

            CollectionReference cr;
            try {
                cr = exploreService.getCollectionReferenceService().getCollectionReference(hits.collection, Optional.empty());
            } catch (ArlasException e) {
                throw new WebApplicationException("Can not access collection metadata", e);
            }
            CollectionReferenceDescription fields = exploreService.describeCollection(cr, Optional.empty());

            if (cr.params.atomFeed != null) {
                Feed feed = cr.params.atomFeed;
                if (feed.author != null) {
                    writer.writeStartElement(ATOM.XML_NS, "author");
                    writeElement(writer, ATOM.XML_NS, "name", feed.author.name);
                    writeElement(writer, ATOM.XML_NS, "email", feed.author.email);
                    writeElement(writer, ATOM.XML_NS, "uri", feed.author.uri);
                    writer.writeEndElement();
                }
                if (feed.contributor != null) {
                    writer.writeStartElement(ATOM.XML_NS, "contributor");
                    writeElement(writer, ATOM.XML_NS, "name", feed.contributor.name);
                    writeElement(writer, ATOM.XML_NS, "email", feed.contributor.email);
                    writeElement(writer, ATOM.XML_NS, "uri", feed.contributor.uri);
                    writer.writeEndElement();
                }
                if (feed.generator != null) {
                    writer.writeStartElement(ATOM.XML_NS, "generator");
                    writeElement(writer, ATOM.XML_NS, "name", feed.generator.name);
                    writeElement(writer, ATOM.XML_NS, "uri", feed.generator.uri);
                    writeElement(writer, ATOM.XML_NS, "version", feed.generator.version);
                    writer.writeEndElement();
                }
                writeElement(writer, ATOM.XML_NS, "icon", feed.icon);
                writeElement(writer, ATOM.XML_NS, "logo", feed.logo);
                writeElement(writer, ATOM.XML_NS, "rights", feed.rights);
                writeElement(writer, ATOM.XML_NS, "subtitle", feed.subtitle);
            }
            for (ArlasHit hit : hits.hits) {
                writer.writeStartElement(ATOM.XML_NS, "entry");
                writeElement(writer, ATOM.XML_NS, "id", hit.md.id);
                writeElement(writer, ATOM.XML_NS, "title", hit.md.id);
                writeElement(writer, ATOM.XML_NS, "update", dateFormater.format(new Date(hit.md.timestamp)));
                writer.writeStartElement(ATOM.XML_NS, "content");
                if (hit.isFlat()) {
                    for (String key : hit.getDataAsMap().keySet()) {
                        writeElement(writer, ATOM.XML_NS, key, hit.getDataAsMap().get(key).toString());
                    }

                } else {
                    writeFields(writer, fields.properties, ATOM.XML_NS, new Stack<>(), hit.data);
                }
                writer.writeEndElement();
                writer.writeStartElement(GEORSS.XML_NS, "where");
                if (hit.md.geometry != null) {
                    GeoFormat.geojson2gml(hit.md.geometry, writer, hit.md.id);
                } else {
                    GeoFormat.geojson2gml(hit.md.centroid, writer, hit.md.id);
                }
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.flush();
        } catch (ArlasException | XMLStreamException e) {
            e.printStackTrace();
            throw new WebApplicationException(e);
        }
    }

    private void writeFields(XMLStreamWriter writer, Map<String, CollectionReferenceDescriptionProperty> properties, String xmlNamespace, Stack<String> namespace, Object data) throws XMLStreamException {
        if (properties == null || data == null) {
            return;
        }
        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            if (property.type == FieldType.OBJECT) {
                writer.writeStartElement(xmlNamespace, key);
                writeFields(writer, property.properties, xmlNamespace, namespace, data);
                writer.writeEndElement();
            } else {
                String fieldPath = String.join(".", new ArrayList<>(namespace));
                writeElement(writer, xmlNamespace, key, toString(MapExplorer.getObjectFromPath(fieldPath, data)));
            }
            namespace.pop();
        }
    }

    private String toString(Object o) {
        if (o == null) return null;
        return o.toString();
    }

    private void writeElement(XMLStreamWriter writer, String namespace, String tag, String value) {
        if (!StringUtil.isNullOrEmpty(value)) {
            try {
                if (Character.isDigit(tag.charAt(0))) {
                    tag = "_" + tag; // QNname cannot start with a digit
                }
                writer.writeStartElement(namespace, tag);
                writer.writeCharacters(value);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                throw new WebApplicationException(e);
            }
        }
    }
}
