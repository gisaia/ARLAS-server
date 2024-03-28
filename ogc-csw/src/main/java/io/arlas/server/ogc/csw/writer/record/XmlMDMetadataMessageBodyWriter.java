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

import io.arlas.server.ogc.csw.operation.getrecordbyid.GetRecordByIdResponse;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBElement;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces(MediaType.APPLICATION_XML)
public class XmlMDMetadataMessageBodyWriter implements MessageBodyWriter<GetRecordByIdResponse> {

    private final static QName _GetRecordByIdResponse_QNAME = new QName("http://www.opengis.net/cat/csw/3.0", "GetRecordByIdResponse");

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass != null && GetRecordByIdResponse.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(GetRecordByIdResponse getRecordByIdResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(GetRecordByIdResponse getRecordByIdResponse, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
            throws WebApplicationException {

        JAXB.marshal(createGetRecordsResponse(getRecordByIdResponse),outputStream);
    }

    public JAXBElement<GetRecordByIdResponse> createGetRecordsResponse(GetRecordByIdResponse value) {
        return new JAXBElement<GetRecordByIdResponse>(_GetRecordByIdResponse_QNAME, GetRecordByIdResponse.class, null, value);
    }

}