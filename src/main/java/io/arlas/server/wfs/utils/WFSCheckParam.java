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

package io.arlas.server.wfs.utils;


import io.arlas.server.exceptions.WFSException;
import io.arlas.server.exceptions.WFSExceptionCode;
import io.arlas.server.model.response.CollectionReferenceDescription;
import io.arlas.server.utils.MapExplorer;

import java.util.Arrays;

public class WFSCheckParam {


    public static boolean isFieldInMapping(CollectionReferenceDescription collectionReference,String... fields) throws RuntimeException {
        String[] cleanField = new  String[ fields.length] ;
        boolean isFieldInMapping =true;
        for(int i=0; i<fields.length;i++){
            if(fields.clone()[i].contains(":")){
                cleanField[i] = fields.clone()[i].split(":")[1];
            }else{
                cleanField[i] = fields.clone()[i];
            }
        }
        for(String field : cleanField) {
            Object data = MapExplorer.getObjectFromPath(field,collectionReference.properties);
            if(data == null) {
                isFieldInMapping= false;
            }
        }
        return isFieldInMapping;
    }

    public static void checkQuerySyntax(String service, String bbox,String resourceid, String filter,WFSRequestType requestType,Version requestVersion) throws WFSException {

        if (bbox != null && resourceid != null) {
            throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and RESOURCEID can't be used together", "bbox,resourceid");
        } else if (bbox != null && filter != null) {
            throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and FILTER can't be used together", "bbox,filter");
        } else if (resourceid != null && filter != null) {
            throw new WFSException(WFSExceptionCode.OPERATION_NOT_SUPPORTED, "RESOURCEID and FILTER can't be used together", "bbox,filter");
        }

        if (requestType != WFSRequestType.GetCapabilities) {
            if (requestVersion == null) {
                String msg = "Missing version parameter.";
                throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, msg, "version");
            }
            VersionUtils.checkVersion(requestVersion);
        } else {
            if (service == null || !service.equals("WFS")) {
                String msg = "Missing service parameter.";
                throw new WFSException(WFSExceptionCode.MISSING_PARAMETER_VALUE, msg, "service");
            }
        }
    }

    public static void checkTypeNames(String collectionName,String typenames) throws WFSException {
        if (typenames != null) {
            if (!typenames.contains(collectionName)) {
                throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "FeatureType " + typenames + " not exist", "typenames");
            }
        }
    }

    public static void checkSrsName(String srsname) throws WFSException {
        boolean isCrsUnSupported = false;
        if (srsname != null) {
            if (Arrays.asList(WFSConstant.SUPPORTED_CRS).indexOf(srsname) < 0) {
                isCrsUnSupported = true;
            }
        } else {
            isCrsUnSupported = false;
        }
        if (isCrsUnSupported) {
            throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid CRS :" + srsname, "srsname");
        }
    }

    public static String formatValueReference(String valuereference,CollectionReferenceDescription collectionReferenceDescription) throws WFSException {
            if (valuereference == null || valuereference.equals("")) {
                throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value", "valuereference");
            }else if(valuereference.equals("@gml:id")){
                valuereference=collectionReferenceDescription.params.idPath;
            }else if(!WFSCheckParam.isFieldInMapping(collectionReferenceDescription,valuereference)){
                throw new WFSException(WFSExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value, " + valuereference + " is not in queryable", "valuereference");
            }
            return valuereference;
    }

    public static boolean isBboxLatLonInCorrectRanges(double[] tlbr) {
        return tlbr[0] >= -90 && tlbr[2] >= -90 && tlbr[0] <= 90 && tlbr[2] <= 90 &&
                tlbr[1] >= -180 && tlbr[3] >= -180 && tlbr[1] <= 180 && tlbr[3] <= 180;
    }


}
