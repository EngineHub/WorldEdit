package com.sk89q.worldedit.expression.runtime;

public class Conditional extends Node {
    RValue condition;
    RValue truePart;
    RValue falsePart;

    public Conditional(int position, RValue condition, RValue truePart, RValue falsePart) {
        super(position);

        this.condition = condition;
        this.truePart = truePart;
        this.falsePart = falsePart;
    }

    @Override
    public double getValue() throws EvaluationException {
        if (condition.getValue() > 0.0) {
            return truePart.getValue();
        } else {
            return falsePart == null ? 0 : falsePart.getValue();
        }
    }

    @Override
    public char id() {
        return 'I';
    }

    @Override
    public String toString() {
        if (falsePart == null) {
            return "if (" + condition + ") { " + truePart + " }";
        } else {
            return "if (" + condition + ") { " + truePart + " } else { " + falsePart + " }";
        }
    }

    //TODO: optimizer
}
