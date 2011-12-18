// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.expression.runtime;

import java.lang.reflect.Method;

/**
 * Wrapper for a pair of Java methods and their arguments (other Nodes), forming an LValue
 *
 * @author TomyLobo
 */
public class LValueFunction extends Function implements LValue {
    private final Object[] setterArgs;
    final Method setter;

    LValueFunction(int position, Method getter, Method setter, RValue... args) {
        super(position, getter, args);

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
}
