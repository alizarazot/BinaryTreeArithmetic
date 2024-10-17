package com.ufpso.binarytreearithmetic;

public class Token {
    enum Type {
        OPERATOR,
        NUMBER,
    }
    Type type;

    enum Operator {
        ADD,
        SUB,
        MUL,
        DIV,
    }
    Operator valueOperator;
    
    double valueNumber;

    public Token(Type type, Operator valueOperator) {
        this.type = type;
        this.valueOperator = valueOperator;
    }
    
    public Token(Type type, double valueNumber) {
        this.type = type;
        this.valueNumber = valueNumber;
    }
}
