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

package io.arlas.server.exceptions;


import io.arlas.server.ogc.wfs.utils.WFSConstant;
import java.util.ArrayList;
import java.util.List;

public class OGCException extends ArlasException  {
    private static final long serialVersionUID = 1L;

    private List<OGCExceptionMessage> exceptionMessages = new ArrayList<>();
    private final String LANGUAGE = "en";

    public OGCException() {}

    public OGCException(String message, Throwable cause) {
        this(OGCExceptionCode.NO_APPLICABLE_CODE, message, cause);
    }

    public OGCException(OGCExceptionCode exceptionCode, String exceptionText, String locator, Throwable cause) {
        super(exceptionText, cause);
        OGCExceptionMessage exceptionMessage = new OGCExceptionMessage(exceptionCode);
        exceptionMessage.addExceptionText(exceptionText);

        String causeMessage = null;
        while (cause != null && (causeMessage = cause.getMessage()) == null)
            cause = cause.getCause();

        exceptionMessage.addExceptionText(causeMessage);
        exceptionMessage.setLocator(locator);
        exceptionMessages.add(exceptionMessage);
    }

    public OGCException(OGCExceptionCode exceptionCode, String message, Throwable cause) {
        this(exceptionCode, message, null, cause);
    }

    public OGCException(String exceptionText) {
        super(exceptionText);
        exceptionMessages.add(new OGCExceptionMessage(OGCExceptionCode.NO_APPLICABLE_CODE, exceptionText));
    }

    public OGCException(OGCExceptionCode exceptionCode, String exceptionText, String locator) {
        super(exceptionText);
        exceptionMessages.add(new OGCExceptionMessage(exceptionCode, exceptionText, locator));
    }

    public OGCException(OGCExceptionCode exceptionCode, String exceptionText) {
        this(exceptionCode, exceptionText, "null");
    }

    public List<OGCExceptionMessage> getExceptionMessages() {
        return exceptionMessages;
    }

    public OGCException(List<OGCExceptionMessage> exceptionMessages) {
        this.exceptionMessages = exceptionMessages;
    }

    public String getVersion() {
        return WFSConstant.SUPPORTED_WFS_VERSION;
    }

    public OGCException(OGCExceptionMessage exceptionMessage) {
        exceptionMessages.add(exceptionMessage);
    }

    public String getLanguage() {
        return LANGUAGE;
    }
}
