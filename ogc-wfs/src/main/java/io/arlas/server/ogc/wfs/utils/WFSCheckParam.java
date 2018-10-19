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

package io.arlas.server.ogc.wfs.utils;


import eu.europa.ec.inspire.schemas.common._1.KeywordValueEnum;
import io.arlas.server.exceptions.INSPIRE.INSPIREException;
import io.arlas.server.exceptions.INSPIRE.INSPIREExceptionCode;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.model.Keyword;
import io.arlas.server.inspire.common.enums.InspireSupportedLanguages;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.Version;
import io.arlas.server.ogc.common.utils.VersionUtils;
import io.arlas.server.utils.MapExplorer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WFSCheckParam {

    public static boolean isFieldInMapping(CollectionReferenceDescription collectionReference, String... fields) throws RuntimeException {
        String[] cleanField = new String[fields.length];
        boolean isFieldInMapping = true;
        for (int i = 0; i < fields.length; i++) {
            if (fields.clone()[i].contains(":")) {
                cleanField[i] = fields.clone()[i].split(":")[1];
            } else {
                cleanField[i] = fields.clone()[i];
            }
        }
        for (String field : cleanField) {
            Object data = MapExplorer.getObjectFromPath(field, collectionReference.properties);
            if (data == null) {
                isFieldInMapping = false;
            }
        }
        return isFieldInMapping;
    }

    public static void checkQuerySyntax(String service, String bbox, String resourceid, String filter, WFSRequestType requestType, Version requestVersion) throws OGCException {

        if (bbox != null && resourceid != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and RESOURCEID can't be used together", "bbox,resourceid", Service.WFS);
        } else if (bbox != null && filter != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and FILTER can't be used together", "bbox,filter", Service.WFS);
        } else if (resourceid != null && filter != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "RESOURCEID and FILTER can't be used together", "bbox,filter", Service.WFS);
        }

        if (requestType != WFSRequestType.GetCapabilities) {
            if (requestVersion == null) {
                String msg = "Missing version parameter.";
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, msg, "version", Service.WFS);
            }
            VersionUtils.checkVersion(requestVersion, WFSConstant.SUPPORTED_WFS_VERSION, Service.WFS);
        } else {
            if (service == null || !service.equals("WFS")) {
                String msg = "Missing service parameter.";
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, msg, "service", Service.WFS);
            }
        }
    }

    public static void checkTypeNames(String collectionName, String typenames) throws OGCException {
        if (typenames != null) {
            if (!typenames.contains(collectionName)) {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "FeatureType " + typenames + " not exist", "typenames", Service.WFS);
            }
        }
    }

    public static void checkSrsName(String srsname) throws OGCException {
        boolean isCrsUnSupported = false;
        if (srsname != null) {
            if (Arrays.asList(WFSConstant.SUPPORTED_CRS).indexOf(srsname) < 0) {
                isCrsUnSupported = true;
            }
        } else {
            isCrsUnSupported = false;
        }
        if (isCrsUnSupported) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid CRS :" + srsname, "srsname", Service.WFS);
        }
    }

    public static String formatValueReference(String valuereference, CollectionReferenceDescription collectionReferenceDescription) throws OGCException {
        if (valuereference == null || valuereference.equals("")) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value", "valuereference", Service.WFS);
        } else if (valuereference.equals("@gml:id")) {
            valuereference = collectionReferenceDescription.params.idPath;
        } else if (!WFSCheckParam.isFieldInMapping(collectionReferenceDescription, valuereference)) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value, " + valuereference + " is not in queryable", "valuereference", Service.WFS);
        }
        return valuereference;
    }

    public static void checkLanguageInspireCompliance(String language, Service service) throws OGCException {
        try {
            InspireSupportedLanguages.valueOf(language);
        } catch (IllegalArgumentException e) {
            throw new INSPIREException(INSPIREExceptionCode.INVALID_INSPIRE_PARAMETER_VALUE, language + " is not a valid language. Languages must be one of the 24 Official languages of the EU in ISO 639-2 (B)", service);
        }
    }

    public static void checkKeywordsInspireCompliance(List<Keyword> keywordList, Service service) throws OGCException {
        boolean atLeastOneCSDSKeyword = Arrays.stream(KeywordValueEnum.values()).map(KeywordValueEnum::value)
                .anyMatch(keywordList.stream()
                .map(k -> k.value)
                .collect(Collectors.toSet())::contains);
        if (!atLeastOneCSDSKeyword) {
            throw new INSPIREException(INSPIREExceptionCode.MISSING_INSPIRE_METADATA, "At least one keyword should be in the Inspire Classification of Spatial Data Services' Vocabulary", service);
        }
    }
}
