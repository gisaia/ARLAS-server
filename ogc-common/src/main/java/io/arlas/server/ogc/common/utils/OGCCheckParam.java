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

package io.arlas.server.ogc.common.utils;

import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.core.model.response.CollectionReferenceDescription;
import io.arlas.server.core.utils.MapExplorer;

public class OGCCheckParam {

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

    public static String formatValueReference(String valuereference, CollectionReferenceDescription collectionReferenceDescription) throws OGCException {
        if (valuereference == null || valuereference.equals("")) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value", "valuereference", Service.WFS);
        } else if (valuereference.equals("@gml:id")) {
            valuereference = collectionReferenceDescription.params.idPath;
        } else if (!OGCCheckParam.isFieldInMapping(collectionReferenceDescription, valuereference)) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid valuereference value, " + valuereference + " is not in queryable", "valuereference", Service.WFS);
        }
        return valuereference;
    }
}
