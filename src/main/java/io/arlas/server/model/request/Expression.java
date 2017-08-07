package io.arlas.server.model.request;

public class Expression {
    public String field;
    public OperatorEnum op;
    public String value;

    public Expression(){}
    public Expression(String field, OperatorEnum op, String value){
        this.field=field;
        this.op=op;
        this.value=value;
    }

    @Override
    public String toString() {
        return field+":"+op.name()+":"+value;
    }
}
