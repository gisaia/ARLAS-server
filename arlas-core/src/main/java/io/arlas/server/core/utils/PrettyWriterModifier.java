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

package io.arlas.server.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

public class PrettyWriterModifier extends ObjectWriterModifier {

    private boolean pretty = false;

    public PrettyWriterModifier() {
    }

    public PrettyWriterModifier(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public ObjectWriter modify(
            EndpointConfigBase<?> endpoint,
            MultivaluedMap<String, Object> responseHeaders,
            Object valueToWrite,
            ObjectWriter w,
            JsonGenerator g) throws IOException {
        if (pretty) {
            g.useDefaultPrettyPrinter();
        }
        return w;
    }
}
