package com.sk89q.worldedit.expression.runtime;

public class For extends Node {
    RValue init;
    RValue condition;
    RValue increment;
    RValue body;

    public For(int position, RValue init, RValue condition, RValue increment, RValue body) {
        super(position);

        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public double getValue() throws EvaluationException {
        int iterations = 0;
        double ret = 0.0;

        for (init.getValue(); condition.getValue() > 0; increment.getValue()) {
            if (iterations > 256) {
                throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
            }
            ++iterations;

            try {
                ret = body.getValue();
            } catch (BreakException e) {
                if (e.doContinue) {
                    continue;
                } else {
                    break;
                }
            }
        }

        return ret;
    }

    @Override
    public char id() {
        return 'F';
    }

    @Override
    public String toString() {
        return "for (" + init + "; " + condition + "; " + increment + ") { " + body + " }";
    }

    //TODO: optimizer
}
