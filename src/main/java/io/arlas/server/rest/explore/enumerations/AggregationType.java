package io.arlas.server.rest.explore.enumerations;


public enum AggregationType {
    datehistogram,geohash,histogram;

    public static final String allowableAggregationTypes="datehistogram,geohash,histogram";
}
