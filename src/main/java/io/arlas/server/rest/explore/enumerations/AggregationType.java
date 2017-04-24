package io.arlas.server.rest.explore.enumerations;

import java.util.ArrayList;
import java.util.List;

public enum AggregationType {
    datehistogram, geohash, histogram, term;

    public static final String DATEHISTOGRAM = "datehistogram";
    public static final String GEOHASH = "geohash";
    public static final String HISTOGRAM = "histogram";
    public static final String TERM = "term";

    public static final String allowableAggregationTypes = "datehistogram,geohash,histogram,terms";

    public static List<String> aggregationTypes() {
        List<String> aggregationTypes = new ArrayList<>();
        aggregationTypes.add(DATEHISTOGRAM);
        aggregationTypes.add(GEOHASH);
        aggregationTypes.add(HISTOGRAM);
        aggregationTypes.add(TERM);
        return aggregationTypes;
    }
}
