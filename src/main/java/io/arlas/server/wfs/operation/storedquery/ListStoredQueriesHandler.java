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

import io.arlas.server.wfs.WFSHandler;
import net.opengis.wfs._2.ListStoredQueriesResponseType;
import net.opengis.wfs._2.StoredQueryListItemType;

import javax.xml.bind.JAXBElement;
import java.util.List;

public class ListStoredQueriesHandler {

    public ListStoredQueriesResponseType listStoredQueriesResponseType;
    private WFSHandler wfsHandler;

    public ListStoredQueriesHandler(WFSHandler wfsHandler){
        this.wfsHandler = wfsHandler;
        listStoredQueriesResponseType = wfsHandler.wfsFactory.createListStoredQueriesResponseType();
        List<StoredQuery> storedQueries = wfsHandler.storedQueryManager.listStoredQueries();
        storedQueries.forEach(sq -> {
            StoredQueryListItemType storedQueryListItemType = wfsHandler.wfsFactory.createStoredQueryListItemType();
            storedQueryListItemType.setId(sq.getId());
            sq.getTitle().forEach(title -> storedQueryListItemType.getTitle().add(title));
            sq.getReturnFeatureTypeNames().forEach(ftn -> storedQueryListItemType.getReturnFeatureType().add(ftn));
            if(listStoredQueriesResponseType.getStoredQuery().indexOf(storedQueryListItemType)<0){
                listStoredQueriesResponseType.getStoredQuery().add(storedQueryListItemType);
            }
        });
    }

    public JAXBElement<ListStoredQueriesResponseType> getListStoredQueriesResponse() {
        return  wfsHandler.wfsFactory.createListStoredQueriesResponse(listStoredQueriesResponseType);
    }
}
