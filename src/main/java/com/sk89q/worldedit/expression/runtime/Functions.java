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

import java.util.Arrays;

/**
 * Contains all functions that can be used in expressions.
 *
 * @author TomyLobo
 */
public final class Functions {
    public static final Function getFunction(int position, String name, RValue... args) throws NoSuchMethodException {
        final Class<?>[] parameterTypes = (Class<?>[]) new Class[args.length];
        Arrays.fill(parameterTypes, RValue.class);
        return new Function(position, Functions.class.getMethod(name, parameterTypes), args);
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
}
