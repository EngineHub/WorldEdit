/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.expression.runtime;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.parser.ParserException;

/**
 * A simple-style for loop.
 */
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
            if (Thread.interrupted()) {
                throw new EvaluationException(getPosition(), "Calculations exceeded time limit.");
            }
            ++iterations;

            try {
                counter.assign(i);
                ret = body.getValue();
            } catch (BreakException e) {
                if (e.doContinue) {
                    //noinspection UnnecessaryContinue
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

    @Override
    public RValue optimize() throws EvaluationException {
        // TODO: unroll small loops into Sequences

        return new SimpleFor(getPosition(), counter.optimize(), first.optimize(), last.optimize(), body.optimize());
    }

    @Override
    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        counter = counter.bindVariables(expression, true);
        first = first.bindVariables(expression, false);
        last = last.bindVariables(expression, false);
        body = body.bindVariables(expression, false);

        return this;
    }

}
