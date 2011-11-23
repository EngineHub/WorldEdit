package com.sk89q.worldedit.expression.runtime;

public class Return extends Node {
    RValue value;

    public Return(int position, RValue value) {
        super(position);

        this.value = value;
    }

    @Override
    public double getValue() throws EvaluationException {
        throw new ReturnException(value.getValue());
    }

    @Override
    public char id() {
        return 'r';
    }

    @Override
    public String toString() {
        return "return " + value;
    }

    //TODO: optimizer
}
