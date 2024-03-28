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

package io.arlas.server.ogc.csw.writer.record;

import io.arlas.server.ogc.csw.utils.AtomBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.xml.bind.JAXB;
import net.opengis.cat.csw._3.AbstractRecordType;
import org.w3._2005.atom.EntryType;
import org.w3._2005.atom.FeedType;
import org.w3._2005.atom.ObjectFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class AtomRecordMessageBodyWriter implements MessageBodyWriter<AbstractRecordType> {


    public AtomRecordMessageBodyWriter() {
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass != null && AbstractRecordType.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(AbstractRecordType abstractRecordType, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(AbstractRecordType abstractRecordType, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

        FeedType feedType = new FeedType();
        ObjectFactory objectFactory = new ObjectFactory();
        EntryType entryType = new EntryType();
        AtomBuilder.setEntryType(abstractRecordType, feedType, entryType);
        JAXB.marshal(objectFactory.createEntry(entryType), outputStream);
    }
}
