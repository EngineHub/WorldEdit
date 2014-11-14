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

package com.sk89q.worldedit.internal.expression.parser;

import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.runtime.EvaluationException;
import com.sk89q.worldedit.internal.expression.runtime.LValue;
import com.sk89q.worldedit.internal.expression.runtime.RValue;

public class UnboundVariable extends PseudoToken implements LValue {

    public final String name;

    public UnboundVariable(int position, String name) {
        super(position);
        this.name = name;
    }

    @Override
    public char id() {
        return 'V';
    }

    @Override
    public String toString() {
        return "UnboundVariable(" + name + ")";
    }

    @Override
    public double getValue() throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to evaluate unbound variable!");
    }

    @Override
    public LValue optimize() throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to optimize unbound variable!");
    }

    @Override
    public double assign(double value) throws EvaluationException {
        throw new EvaluationException(getPosition(), "Tried to assign unbound variable!");
    }

    public RValue bind(Expression expression, boolean isLValue) throws ParserException {
        final RValue variable = expression.getVariable(name, isLValue);
        if (variable == null) {
            throw new ParserException(getPosition(), "Variable '" + name + "' not found");
        }

        return variable;
    }

    @Override
    public LValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        final RValue variable = expression.getVariable(name, preferLValue);
        if (variable == null) {
            throw new ParserException(getPosition(), "Variable '" + name + "' not found");
        }

        return (LValue) variable;
    }

}
