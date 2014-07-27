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

import java.lang.reflect.Method;

/**
 * Wrapper for a pair of Java methods and their arguments (other Nodes),
 * forming an LValue.
 */
public class LValueFunction extends Function implements LValue {

    private final Object[] setterArgs;
    private final Method setter;

    LValueFunction(int position, Method getter, Method setter, RValue... args) {
        super(position, getter, args);
        assert (getter.isAnnotationPresent(Dynamic.class));

        setterArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, setterArgs, 0, args.length);
        this.setter = setter;
    }

    @Override
    public char id() {
        return 'l';
    }

    @Override
    public double assign(double value) throws EvaluationException {
        setterArgs[setterArgs.length - 1] = value;
        return invokeMethod(setter, setterArgs);
    }

    @Override
    public LValue optimize() throws EvaluationException {
        final RValue optimized = super.optimize();
        if (optimized == this) {
            return this;
        }

        if (optimized instanceof Function) {
            return new LValueFunction(optimized.getPosition(), method, setter, ((Function) optimized).args);
        }

        return (LValue) optimized;
    }

    @Override
    public LValue bindVariables(Expression expression, boolean preferLValue) throws ParserException {
        super.bindVariables(expression, preferLValue);

        return this;
    }

}
