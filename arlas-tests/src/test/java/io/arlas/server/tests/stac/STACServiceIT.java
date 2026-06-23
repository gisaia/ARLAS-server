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

import io.arlas.server.stac.model.SearchBody;
import io.restassured.http.ContentType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import io.arlas.server.tests.stac.STACFilterModels.HttpMethod;
public class STACServiceIT extends AbstractSTACServiceTest {


    @Test
    public void testConformance() throws Exception {
        givenFilterableRequestParams().get(getUrlStacPath() + "/conformance")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("conformsTo", contains(arlasConformsTo.toArray(new String[0])));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("collectionCases")
    void testSearchAccordingCollectionStatus(String label, List<String> collections, int expectedStatus) throws Exception {
        Arrays.stream(HttpMethod.values())
                .forEach(m -> assertSearchAccordingCollectionStatus(m, collections, expectedStatus));
    }

    private static Stream<Arguments> collectionCases() {
        return Stream.of(
                Arguments.of("No collection provided", List.of(), 400),
                Arguments.of("Multiple collections provided", List.of("collection1", "collection2"), 400),
                Arguments.of("Exactly one collection provided", List.of("geodata"), 200)
        );
    }

    private void assertSearchAccordingCollectionStatus(HttpMethod method, List<String> collections, int expectedStatus) {
        var request = givenFilterableRequestParams();
        var path = getUrlStacSearchPath();
        switch (method) {
            case GET -> {
                if (collections != null && !collections.isEmpty()) {
                    request.param("collections", collections.toArray());
                }
                request.get(path)
                        .then()
                        .statusCode(expectedStatus);
            }
            case POST -> request.body(new SearchBody<>().collections(collections))
                    .post(path)
                    .then()
                    .statusCode(expectedStatus);
            default -> throw new IllegalArgumentException("Invalid method: " + method);
        }
    }
}