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


import io.arlas.server.wfs.utils.WFSConstant;
import java.util.ArrayList;
import java.util.List;

public class WFSException extends ArlasException  {
    private static final long serialVersionUID = 1L;

    private List<WFSExceptionMessage> exceptionMessages = new ArrayList<>();
    private final String LANGUAGE = "en";

    public WFSException(String exceptionText) {
        super(exceptionText);
        exceptionMessages.add(new WFSExceptionMessage(WFSExceptionCode.NO_APPLICABLE_CODE, exceptionText));
    }

    public WFSException(WFSExceptionCode exceptionCode, String exceptionText, String locator) {
        super(exceptionText);
        exceptionMessages.add(new WFSExceptionMessage(exceptionCode, exceptionText, locator));
    }

    public WFSException(WFSExceptionCode exceptionCode, String exceptionText) {
        this(exceptionCode, exceptionText, null);
    }

    public List<WFSExceptionMessage> getExceptionMessages() {
        return exceptionMessages;
    }

    public WFSException(List<WFSExceptionMessage> exceptionMessages) {
        this.exceptionMessages = exceptionMessages;
    }

    public String getVersion() {
        return WFSConstant.SUPPORTED_WFS_VERSION;
    }

    public WFSException(WFSExceptionMessage exceptionMessage) {
        exceptionMessages.add(exceptionMessage);
    }

    public String getLanguage() {
        return LANGUAGE;
    }
}
