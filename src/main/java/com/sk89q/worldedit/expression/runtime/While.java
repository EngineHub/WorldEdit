package com.sk89q.worldedit.expression.runtime;

public class While extends Node {
    RValue condition;
    RValue body;
    boolean footChecked;

    public While(int position, RValue condition, RValue body, boolean footChecked) {
        super(position);

        this.condition = condition;
        this.body = body;
        this.footChecked = footChecked;
    }

    @Override
    public double getValue() throws EvaluationException {
        int iterations = 0;
        double ret = 0.0;

        if (footChecked) {
            do {
                ret = body.getValue();
                ++iterations;
                if (iterations > 256) {
                    throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
                }
            } while (condition.getValue() > 0.0);
        } else {
            while (condition.getValue() > 0.0) {
                ret = body.getValue();
                ++iterations;
                if (iterations > 256) {
                    throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
                }
            }
        }

        return ret;
    }

    @Override
    public char id() {
        return 'w';
    }

    @Override
    public String toString() {
        if (footChecked) {
            return "do { "+body+" } while ("+condition+")";
        } else {
            return "while ("+condition+") { "+body+" }";
        }
    }

    //TODO: optimizer
}
