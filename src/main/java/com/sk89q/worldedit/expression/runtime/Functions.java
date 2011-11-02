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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains all functions that can be used in expressions.
 *
 * @author TomyLobo
 */
public final class Functions {
    private static class Overload {
        private final Method method; 
        private final int mask;

        public Overload(Method method) throws IllegalArgumentException {
            this.method = method;

            int accum = 0;
            Class<?>[] parameters = method.getParameterTypes();
            for (Class<?> parameter : parameters) {
                if (!RValue.class.isAssignableFrom(parameter)) {
                    throw new IllegalArgumentException("Method takes arguments that can't be cast to RValue.");
                }

                accum <<= 2;

                if (LValue.class.isAssignableFrom(parameter)) {
                    accum |= 3;
                } else {
                    accum |= 1;
                }
            }
            mask = accum;
        }

        public boolean matches(RValue... args) {
            int accum = 0;
            for (RValue argument : args) {
                accum <<= 2;

                if (argument instanceof LValue) {
                    accum |= 3;
                } else {
                    accum |= 1;
                }
            }

            return (accum & mask) == mask;
        }
    }


    public static final Function getFunction(int position, String name, RValue... args) throws NoSuchMethodException {
        return new Function(position, getMethod(name, args), args);
    }

    private static Method getMethod(String name, RValue... args) throws NoSuchMethodException {
        final List<Overload> overloads = functions.get(name);
        if (overloads != null) {
            for (Overload overload : overloads) {
                if (overload.matches(args)) {
                    return overload.method;
                }
            }
        }

        throw new NoSuchMethodException();
    }

    private static final Map<String, List<Overload>> functions = new HashMap<String, List<Overload>>();
    static {
        for (Method method : Functions.class.getMethods()) {
            final String methodName = method.getName();

            try {
                Overload overload = new Overload(method);

                List<Overload> overloads = functions.get(methodName);
                if (overloads == null) {
                    functions.put(methodName, overloads = new ArrayList<Overload>());
                }

                overloads.add(overload);
            } catch(IllegalArgumentException e) {}
        }
    }


    public static final double sin(RValue x) throws Exception {
        return Math.sin(x.getValue());
    }

    public static final double cos(RValue x) throws Exception {
        return Math.cos(x.getValue());
    }

    public static final double tan(RValue x) throws Exception {
        return Math.tan(x.getValue());
    }


    public static final double asin(RValue x) throws Exception {
        return Math.asin(x.getValue());
    }

    public static final double acos(RValue x) throws Exception {
        return Math.acos(x.getValue());
    }

    public static final double atan(RValue x) throws Exception {
        return Math.atan(x.getValue());
    }

    public static final double atan2(RValue y, RValue x) throws Exception {
        return Math.atan2(y.getValue(), x.getValue());
    }


    public static final double sinh(RValue x) throws Exception {
        return Math.sinh(x.getValue());
    }

    public static final double cosh(RValue x) throws Exception {
        return Math.cosh(x.getValue());
    }

    public static final double tanh(RValue x) throws Exception {
        return Math.tanh(x.getValue());
    }


    public static final double sqrt(RValue x) throws Exception {
        return Math.sqrt(x.getValue());
    }

    public static final double cbrt(RValue x) throws Exception {
        return Math.cbrt(x.getValue());
    }


    public static final double abs(RValue x) throws Exception {
        return Math.abs(x.getValue());
    }

    public static final double min(RValue a, RValue b) throws Exception {
        return Math.min(a.getValue(), b.getValue());
    }

    public static final double min(RValue a, RValue b, RValue c) throws Exception {
        return Math.min(a.getValue(), Math.min(b.getValue(), c.getValue()));
    }

    public static final double max(RValue a, RValue b) throws Exception {
        return Math.max(a.getValue(), b.getValue());
    }

    public static final double max(RValue a, RValue b, RValue c) throws Exception {
        return Math.max(a.getValue(), Math.max(b.getValue(), c.getValue()));
    }


    public static final double ceil(RValue x) throws Exception {
        return Math.ceil(x.getValue());
    }

    public static final double floor(RValue x) throws Exception {
        return Math.floor(x.getValue());
    }

    public static final double rint(RValue x) throws Exception {
        return Math.rint(x.getValue());
    }

    public static final double round(RValue x) throws Exception {
        return Math.round(x.getValue());
    }


    public static final double exp(RValue x) throws Exception {
        return Math.exp(x.getValue());
    }

    public static final double ln(RValue x) throws Exception {
        return Math.log(x.getValue());
    }

    public static final double log(RValue x) throws Exception {
        return Math.log(x.getValue());
    }

    public static final double log10(RValue x) throws Exception {
        return Math.log10(x.getValue());
    }


    public static final double rotate(LValue x, LValue y, RValue angle) throws EvaluationException {
        final double f = angle.getValue();

        final double cosF = Math.cos(f);
        final double sinF = Math.sin(f);

        final double xOld = x.getValue();
        final double yOld = y.getValue();

        x.assign(xOld * cosF - yOld * sinF);
        y.assign(xOld * sinF + yOld * cosF);

        return 0;
    }
}
