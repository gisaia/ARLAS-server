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

package io.arlas.server.services;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.core.FilteredUpdater;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.Action;
import io.arlas.server.model.request.MixedRequest;
import io.arlas.server.model.request.Search;
import io.arlas.server.model.request.Tag;
import io.arlas.server.model.response.UpdateResponse;
import org.elasticsearch.client.Client;

import java.io.IOException;

@Deprecated
public class UpdateServices extends ExploreServices{

    public enum ACTION {
        ADD,REMOVE, REMOVEALL;
    }

    public UpdateServices(Client client, ArlasServerConfiguration configuration) {
        super(client, configuration);
    }

    public UpdateResponse tag(CollectionReference collectionReference, MixedRequest request, Tag tag, int max_updates) throws IOException, ArlasException {
        return this.getFilteredTagger(collectionReference, request).doAction(Action.ADD,collectionReference, tag, max_updates);
    }

    public UpdateResponse unTag(CollectionReference collectionReference, MixedRequest request, Tag tag, int max_updates) throws IOException, ArlasException {
        return this.getFilteredTagger(collectionReference, request).doAction(Action.REMOVE,collectionReference, tag, max_updates);
    }

    public UpdateResponse removeAll(CollectionReference collectionReference, MixedRequest request, Tag tag, int max_updates) throws IOException, ArlasException {
        return this.getFilteredTagger(collectionReference, request).doAction(Action.REMOVEALL,collectionReference, tag, max_updates);
    }

    protected FilteredUpdater getFilteredTagger(CollectionReference collectionReference, MixedRequest request) throws IOException, ArlasException {
        FilteredUpdater updater = new FilteredUpdater(this.getClient());
        updater.setCollectionReference(collectionReference);
        applyFilter(request.headerRequest.filter, updater);
        if(request.basicRequest!=null){
            applyFilters(request.basicRequest.filter, request.columnFilter, updater);
            setPageSizeAndFrom(((Search)request.basicRequest).page, updater);
            sortPage(((Search) request.basicRequest).page, request.columnFilter, updater);
            applyProjection(((Search) request.basicRequest).projection, request.columnFilter, updater);
        }
        return updater;
    }
}