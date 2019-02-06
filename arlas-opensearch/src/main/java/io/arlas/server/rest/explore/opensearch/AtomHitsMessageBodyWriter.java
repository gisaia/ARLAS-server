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

package io.arlas.server.rest.explore.opensearch;

import io.arlas.server.core.ElasticAdmin;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.Feed;
import io.arlas.server.model.response.*;
import io.arlas.server.ns.ATOM;
import io.arlas.server.ns.GEORSS;
import io.arlas.server.ns.GML;
import io.arlas.server.ns.OPENSEARCH;
import io.arlas.server.ogc.common.utils.GeoFormat;
import io.arlas.server.services.ExploreServices;
import io.arlas.server.utils.MapExplorer;
import io.arlas.server.utils.StringUtil;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

@Provider
@Produces(ATOM.APPLICATION_ATOM_XML)
public class AtomHitsMessageBodyWriter implements MessageBodyWriter<Hits> {

    public static final SimpleDateFormat dateFormater =
            new SimpleDateFormat("yyyy-MM.dd'T'hh:mm:ssZ");

    private ExploreServices exploration;

    public AtomHitsMessageBodyWriter(ExploreServices exploration) {
        this.exploration = exploration;
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
    public void writeTo(Hits hits, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
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

            CollectionReference cr = null;
            try {
                cr = exploration.getDaoCollectionReference().getCollectionReference(hits.collection);
            } catch (ArlasException e) {
                throw new WebApplicationException("Can not access collection metadata", e);
            }
            CollectionReferenceDescription fields = new ElasticAdmin(exploration.getClient()).describeCollection(cr);

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
            if (hits != null) {
                for (Hit hit : hits.hits) {
                    writer.writeStartElement(ATOM.XML_NS, "entry");
                    writeElement(writer, ATOM.XML_NS, "id", hit.md.id);
                    writeElement(writer, ATOM.XML_NS, "title", hit.md.id);
                    writeElement(writer, ATOM.XML_NS, "update", dateFormater.format(new Date(hit.md.timestamp)));
                    writer.writeStartElement(ATOM.XML_NS, "content");
                    if (hit.isFlat()) {
                        Iterator<String> keysIt = hit.getDataAsMap().keySet().iterator();
                        while (keysIt.hasNext()) {
                            String key = keysIt.next();
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
            }
            writer.writeEndElement();
            writer.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
            new WebApplicationException(e);
        }
    }

    private void writeFields(XMLStreamWriter writer, Map<String, CollectionReferenceDescriptionProperty> properties, String xmlNamespace, Stack<String> namespace, Object data) throws XMLStreamException {
        if (properties == null || data == null) {
            return;
        }
        for (String key : properties.keySet()) {
            CollectionReferenceDescriptionProperty property = properties.get(key);
            namespace.push(key);
            if (property.type == ElasticType.OBJECT) {
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
                writer.writeStartElement(namespace, tag);
                writer.writeCharacters(value);
                writer.writeEndElement();
            } catch (XMLStreamException e) {
                new WebApplicationException(e);
            }
        }
    }
}
