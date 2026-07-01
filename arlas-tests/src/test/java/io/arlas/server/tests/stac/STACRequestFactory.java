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

package io.arlas.server.tests.stac;

import io.arlas.commons.exceptions.InvalidParameterException;
import io.arlas.server.stac.model.SearchBody;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.arlas.server.tests.stac.STACFilterModels.FilterLang;
import static io.arlas.server.tests.stac.STACFilterModels.StacFilterScenario;

public class STACRequestFactory {

    private final String collection;
    private final STACFilterSerializer serializer = new STACFilterSerializer();

    public STACRequestFactory(String collection) {
        this.collection = collection;
    }

    public List<Pair<String, String>> buildGetParams(
            StacFilterScenario scenario,
            FilterLang lang,
            STACFilterModels.TypeOfGet typeOfGet
    ) throws ParseException, IOException {
        List<Pair<String, String>> params = new ArrayList<>();
        if(typeOfGet.equals(STACFilterModels.TypeOfGet.STAC)){
            params.add(new ImmutablePair<>("collections", collection));
        }
        params.add(new ImmutablePair<>("filter", serializer.serialize(scenario.clauses(), lang)));
        params.add(new ImmutablePair<>("limit", String.valueOf(scenario.limit())));
        params.add(new ImmutablePair<>("filter-lang", lang.value()));

        if (scenario.bbox() != null) {
            params.add(new ImmutablePair<>("bbox", scenario.bbox()));
        }

        if (scenario.datetime() != null) {
            params.add(new ImmutablePair<>("datetime", scenario.datetime()));
        }

        if (scenario.ids() != null) {
            scenario.ids().forEach(id -> params.add(new ImmutablePair<>("ids", id)));
        }

        return params;
    }

    public SearchBody<Object> buildSearchBody(
            StacFilterScenario scenario,
            FilterLang lang
    ) throws InvalidParameterException, ParseException, IOException {
        SearchBody<Object> body = new SearchBody<>()
                .filterLang(lang.value())
                .collections(List.of(collection))
                .limit(scenario.limit())
                .filter(serializer.serialize(scenario.clauses(), lang));

        if (scenario.bbox() != null) {
            body.bbox(parseBbox(scenario.bbox()));
        }

        if (scenario.datetime() != null) {
            body.datetime(scenario.datetime());
        }

        if (scenario.ids() != null) {
            body.ids(scenario.ids());
        }

        return body;
    }

    private List<Double> parseBbox(String bbox) throws InvalidParameterException {
        try {
            return Stream.of(bbox.split(","))
                    .map(Double::valueOf)
                    .toList();
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Invalid bbox definition: " + bbox);
        }
    }
}