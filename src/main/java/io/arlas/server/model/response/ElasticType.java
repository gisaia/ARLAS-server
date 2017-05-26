package io.arlas.server.model.response;

public enum ElasticType {
    TEXT("text"), KEYWORD("keyword"),
    LONG("long"), INTEGER("integer"), SHORT("short"), BYTE("byte"), DOUBLE("double"), FLOAT("double"),
    DATE("date"),
    BOOLEAN("boolean"),
    BINARY("binary"),
    INT_RANGE("integer_range"), FLOAT_RANGE("float_range"), LONG_RANGE("long_range"), DOUBLE_RANGE("double_range"), DATE_RANGE("date_range"),
    OBJECT("object"), NESTED("nested"),
    GEO_POINT("geo_point"), GEO_SHAPE("geo_shape"),
    IP("ip"),
    COMPLETION("completion"), TOKEN_COUNT("token_count"), MAPPER_MURMUR3("murmur3"),
    UNKNOWN("unknown");

    public final String elasticType;
    
    ElasticType(String elasticType) {
        this.elasticType = elasticType;
    }
    
    public static ElasticType getType(String type) {
        ElasticType ret = UNKNOWN;
        for(ElasticType t : ElasticType.values()) {
            if(t.elasticType.equals(type)) {
                ret = t;
                break;
            }
        }
        return ret;
    }
    
    @Override
    public String toString() {
        return elasticType.toString();
    }
}