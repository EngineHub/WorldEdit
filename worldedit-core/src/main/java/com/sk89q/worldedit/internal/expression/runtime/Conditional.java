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
 * An if/else statement or a ternary operator.
 */
public class Conditional extends Node {

    private RValue condition;
    private RValue truePart;
    private RValue falsePart;

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
            return falsePart == null ? 0.0 : falsePart.getValue();
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
        } else if (truePart instanceof Sequence || falsePart instanceof Sequence) {
            return "if (" + condition + ") { " + truePart + " } else { " + falsePart + " }";
        } else {
            return "(" + condition + ") ? (" + truePart + ") : (" + falsePart + ")";
        }
    }

    @Override
    public RValue optimize() throws EvaluationException {
        final RValue newCondition = condition.optimize();

        if (newCondition instanceof Constant) {
            if (newCondition.getValue() > 0) {
                return truePart.optimize();
            } else {
                return falsePart == null ? new Constant(getPosition(), 0.0) : falsePart.optimize();
            }
        }

        return new Conditional(getPosition(), newCondition, truePart.optimize(), falsePart == null ? null : falsePart.optimize());
    }

    @Override
    public RValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        condition = condition.bindVariables(expression, false);
        truePart = truePart.bindVariables(expression, false);
        if (falsePart != null) {
            falsePart = falsePart.bindVariables(expression, false);
        }

        return this;
    }

}
