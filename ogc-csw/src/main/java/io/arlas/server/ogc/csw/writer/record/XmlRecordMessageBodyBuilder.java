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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXB;
import net.opengis.cat.csw._3.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class XmlRecordMessageBodyBuilder implements MessageBodyWriter<AbstractRecordType> {

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

        ObjectFactory cswFactory = new ObjectFactory();
        String recordType = abstractRecordType.getClass().getSimpleName();
        switch (recordType){
            case "BriefRecordType":
                JAXB.marshal(cswFactory.createBriefRecord((BriefRecordType) abstractRecordType),outputStream);
                break;
            case "SummaryRecordType":
                JAXB.marshal(cswFactory.createSummaryRecord((SummaryRecordType) abstractRecordType),outputStream);
                break;
            case "RecordType":
                JAXB.marshal(cswFactory.createRecord((RecordType) abstractRecordType),outputStream);
                break;
        }
    }
}