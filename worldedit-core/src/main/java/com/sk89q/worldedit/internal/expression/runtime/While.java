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
 * A while loop.
 */
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
                if (iterations > 256) {
                    throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
                }
                if (Thread.interrupted()) {
                    throw new EvaluationException(getPosition(), "Calculations exceeded time limit.");
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
            } while (condition.getValue() > 0.0);
        } else {
            while (condition.getValue() > 0.0) {
                if (iterations > 256) {
                    throw new EvaluationException(getPosition(), "Loop exceeded 256 iterations.");
                }
                if (Thread.interrupted()) {
                    throw new EvaluationException(getPosition(), "Calculations exceeded time limit.");
                }
                ++iterations;

                try {
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
            return "do { " + body + " } while (" + condition + ")";
        } else {
            return "while (" + condition + ") { " + body + " }";
        }
    }

    @Override
    public RValue optimize() throws EvaluationException {
        final RValue newCondition = condition.optimize();

        if (newCondition instanceof Constant && newCondition.getValue() <= 0) {
            // If the condition is always false, the loop can be flattened.
            if (footChecked) {
                // Foot-checked loops run at least once.
                return body.optimize();
            } else {
                // Loops that never run always return 0.0.
                return new Constant(getPosition(), 0.0);
            }
        }

        return new While(getPosition(), newCondition, body.optimize(), footChecked);
    }

    @Override
    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        condition = condition.bindVariables(expression, false);
        body = body.bindVariables(expression, false);

        return this;
    }

}
