package io.arlas.server.rest.explore.search.mvt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * MVT constants (mimetype and output formats)
 *
 */
interface MVT {

    String MIME_TYPE = "application/x-protobuf";

    Set<String> OUTPUT_FORMATS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(MIME_TYPE, "application/pbf",
                    "application/mvt")));

}
