package com.sk89q.worldedit.expression.runtime;

public class ReturnException extends EvaluationException {
    private static final long serialVersionUID = 1L;

    final double value;

    public ReturnException(double value) {
        super(-1);

        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
