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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Function extends Invokable {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dynamic { }

    final Method method;
    final Invokable[] args;

    Function(int position, Method method, Invokable... args) {
        super(position);
        this.method = method;
        this.args = args;
    }

    @Override
    public final double invoke() throws EvaluationException {
        try {
            return (Double) method.invoke(null, (Object[]) args);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof EvaluationException) {
                throw (EvaluationException) e.getTargetException();
            }
            throw new EvaluationException(-1, "Exception caught while evaluating expression", e.getTargetException());
        }
        catch (IllegalAccessException e) {
            throw new EvaluationException(-1, "Internal error while evaluating expression", e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(method.getName()).append('(');
        boolean first = true;
        for (Object obj : args) {
            if (!first) {
                ret.append(", ");
            }
            first = false;
            ret.append(obj);
        }
        return ret.append(')').toString();
    }

    @Override
    public char id() {
        return 'f';
    }

    @Override
    public Invokable optimize() throws EvaluationException {
        final Invokable[] optimizedArgs = new Invokable[args.length];
        boolean optimizable = !method.isAnnotationPresent(Dynamic.class);
        for (int i = 0; i < args.length; ++i) {
            final Invokable optimized = optimizedArgs[i] = args[i].optimize();

            if (!(optimized instanceof Constant)) {
                optimizable = false;
            }
        }

        if (optimizable) {
            return new Constant(getPosition(), invoke());
        }
        else {
            return new Function(getPosition(), method, optimizedArgs);
        }
    }
}
