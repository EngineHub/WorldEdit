package com.sk89q.worldedit.expression.runtime;

public class SimpleFor extends Node {
    LValue counter;
    RValue first;
    RValue last;
    RValue body;

    public SimpleFor(int position, LValue counter, RValue first, RValue last, RValue body) {
        super(position);

        this.counter = counter;
        this.first = first;
        this.last = last;
        this.body = body;
    }

    @Override
    public double getValue() throws EvaluationException {
        int iterations = 0;
        double ret = 0.0;

        double firstValue = first.getValue();
        double lastValue = last.getValue();

        for (double i = firstValue; i <= lastValue; ++i) {
            if (iterations > 256) {
                throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
            }
            ++iterations;

            try {
                counter.assign(i);
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
        return 'S';
    }

    @Override
    public String toString() {
        return "for (" + counter + " = " + first + ", " + last + ") { " + body + " }";
    }

    //TODO: optimizer
}
