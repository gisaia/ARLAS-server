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

package io.arlas.server.wfs.operation.storedquery;

import net.opengis.wfs._2.QueryExpressionTextType;
import net.opengis.wfs._2.StoredQueryDescriptionType;
import net.opengis.wfs._2.Title;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoredQuery {

    private final StoredQueryDescriptionType description;

    StoredQuery(StoredQueryDescriptionType description) {
        this.description = description;
    }

    public String getId() {
        return description.getId();
    }

    public List<Title> getTitle() {
        return description.getTitle();
    }

    public List<QName> getReturnFeatureTypeNames() {
        Set<QName> featureTypeNames = new HashSet<>();
        for (QueryExpressionTextType queryExpression : description.getQueryExpressionText())
            featureTypeNames.addAll(queryExpression.getReturnFeatureTypes());
        return new ArrayList<>(featureTypeNames);
    }

    public void setFeatureType(QName featureQname){
        description.getQueryExpressionText().forEach(queryExpressionTextType -> queryExpressionTextType.getReturnFeatureTypes().add(featureQname));
    }

    public StoredQueryDescriptionType getStoredQueryDescription() {
        return description;
    }
}
