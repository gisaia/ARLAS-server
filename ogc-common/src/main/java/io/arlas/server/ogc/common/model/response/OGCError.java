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

package io.arlas.server.ogc.common.model.response;

import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionMessage;
import net.opengis.ows._1.ExceptionReport;
import net.opengis.ows._1.ExceptionType;
import net.opengis.ows._1.ObjectFactory;

public class OGCError {

    public Object exceptionReport;

    public OGCError(OGCException e){

        switch (e.ogcService){
            case WFS:
                ObjectFactory owsFactory = new ObjectFactory();
                ExceptionReport exceptionReport = owsFactory.createExceptionReport();
                exceptionReport.setLang(e.getLanguage());
                exceptionReport.setVersion(e.getVersion());
                for (OGCExceptionMessage message : e.getExceptionMessages()) {
                    ExceptionType exceptionType = owsFactory.createExceptionType();
                    exceptionType.setExceptionCode(message.getExceptionCode().getValue());
                    exceptionType.setLocator(message.getLocator());
                    for (String text : message.getExceptionTexts()) {
                        exceptionType.getExceptionText().add(text);
                    }
                    exceptionReport.getException().add(exceptionType);
                }
                this.exceptionReport=exceptionReport;
                break;
            case CSW:
                net.opengis.ows._2.ObjectFactory owsFactory2 = new net.opengis.ows._2.ObjectFactory();
                net.opengis.ows._2.ExceptionReport exceptionReport2 = owsFactory2.createExceptionReport();
                exceptionReport2.setLang(e.getLanguage());
                exceptionReport2.setVersion(e.getVersion());
                for (OGCExceptionMessage message : e.getExceptionMessages()) {
                    net.opengis.ows._2.ExceptionType exceptionType = owsFactory2.createExceptionType();
                    exceptionType.setExceptionCode(message.getExceptionCode().getValue());
                    exceptionType.setLocator(message.getLocator());
                    for (String text : message.getExceptionTexts()) {
                        exceptionType.getExceptionText().add(text);
                    }
                    exceptionReport2.getException().add(exceptionType);
                }
                this.exceptionReport=exceptionReport2;
                break;
        }


    }
}
