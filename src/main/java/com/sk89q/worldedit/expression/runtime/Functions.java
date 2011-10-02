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

public final class Functions {
    public static final Function getFunction(int position, String name, Invokable... args) throws NoSuchMethodException {
        final Class<?>[] parameterTypes = (Class<?>[]) new Class[args.length];
        Arrays.fill(parameterTypes, Invokable.class);
        return new Function(position, Functions.class.getMethod(name, parameterTypes), args);
    }


    public static final double sin(Invokable x) throws Exception {
        return Math.sin(x.invoke());
    }

    public static final double cos(Invokable x) throws Exception {
        return Math.cos(x.invoke());
    }

    public static final double tan(Invokable x) throws Exception {
        return Math.tan(x.invoke());
    }


    public static final double asin(Invokable x) throws Exception {
        return Math.asin(x.invoke());
    }

    public static final double acos(Invokable x) throws Exception {
        return Math.acos(x.invoke());
    }

    public static final double atan(Invokable x) throws Exception {
        return Math.atan(x.invoke());
    }

    public static final double atan2(Invokable y, Invokable x) throws Exception {
        return Math.atan2(y.invoke(), x.invoke());
    }


    public static final double sinh(Invokable x) throws Exception {
        return Math.sinh(x.invoke());
    }

    public static final double cosh(Invokable x) throws Exception {
        return Math.cosh(x.invoke());
    }

    public static final double tanh(Invokable x) throws Exception {
        return Math.tanh(x.invoke());
    }


    public static final double sqrt(Invokable x) throws Exception {
        return Math.sqrt(x.invoke());
    }

    public static final double cbrt(Invokable x) throws Exception {
        return Math.cbrt(x.invoke());
    }


    public static final double abs(Invokable x) throws Exception {
        return Math.abs(x.invoke());
    }

    public static final double min(Invokable a, Invokable b) throws Exception {
        return Math.min(a.invoke(), b.invoke());
    }

    public static final double max(Invokable a, Invokable b) throws Exception {
        return Math.max(a.invoke(), b.invoke());
    }


    public static final double ceil(Invokable x) throws Exception {
        return Math.ceil(x.invoke());
    }

    public static final double floor(Invokable x) throws Exception {
        return Math.floor(x.invoke());
    }

    public static final double rint(Invokable x) throws Exception {
        return Math.rint(x.invoke());
    }

    public static final double round(Invokable x) throws Exception {
        return Math.round(x.invoke());
    }


    public static final double exp(Invokable x) throws Exception {
        return Math.exp(x.invoke());
    }

    public static final double ln(Invokable x) throws Exception {
        return Math.log(x.invoke());
    }

    public static final double log(Invokable x) throws Exception {
        return Math.log(x.invoke());
    }

    public static final double log10(Invokable x) throws Exception {
        return Math.log10(x.invoke());
    }
}
