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

package com.sk89q.worldedit.internal.expression;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Doubles;
import com.sk89q.worldedit.internal.expression.LocalSlot.Variable;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.noise.PerlinNoise;
import com.sk89q.worldedit.math.noise.RidgedMultiFractalNoise;
import com.sk89q.worldedit.math.noise.VoronoiNoise;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodType.methodType;

/**
 * Contains all functions that can be used in expressions.
 */
final class Functions {

    static SetMultimap<String, MethodHandle> getFunctionMap() {
        SetMultimap<String, MethodHandle> map = HashMultimap.create();
        Functions functions = new Functions();
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        try {
            addMathHandles(map, lookup);
            addStaticFunctionHandles(map, lookup);
            functions.addInstanceFunctionHandles(map, lookup);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        // clean up all the functions
        return ImmutableSetMultimap.copyOf(
            Multimaps.transformValues(map, Functions::clean)
        );
    }

    private static final MethodHandle DOUBLE_VALUE;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            DOUBLE_VALUE = lookup.findVirtual(Number.class, "doubleValue",
                methodType(double.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static MethodHandle clean(MethodHandle handle) {
        // box it all first
        handle = handle.asType(handle.type().wrap());
        if (handle.type().returnType() != Double.class) {
            // Ensure that the handle returns a Double, even if originally a Number
            checkState(Number.class.isAssignableFrom(handle.type().returnType()),
                "Function does not return a number");
            handle = handle.asType(handle.type().changeReturnType(Number.class));
            handle = filterReturnValue(handle, DOUBLE_VALUE);
        }
        return handle;
    }

    private static void addMathHandles(
        SetMultimap<String, MethodHandle> map,
        MethodHandles.Lookup lookup
    ) throws NoSuchMethodException, IllegalAccessException {
        // double <name>(double) functions
        for (String name : ImmutableList.of(
            "sin", "cos", "tan", "asin", "acos", "atan",
            "sinh", "cosh", "tanh", "sqrt", "cbrt", "abs",
            "ceil", "floor", "rint", "exp", "log", "log10"
        )) {
            map.put(name, lookup.findStatic(Math.class, name,
                methodType(double.class, double.class)));
        }
        // Alias ln -> log
        map.put("ln", lookup.findStatic(Math.class, "log",
            methodType(double.class, double.class)));
        map.put("round", lookup.findStatic(Math.class, "round",
            methodType(long.class, double.class)));

        map.put("atan2", lookup.findStatic(Math.class, "atan2",
            methodType(double.class, double.class, double.class)));

        // Special cases: we accept varargs for these
        map.put("min", lookup.findStatic(Doubles.class, "min",
            methodType(double.class, double[].class))
            .asVarargsCollector(double[].class));
        map.put("max", lookup.findStatic(Doubles.class, "max",
            methodType(double.class, double[].class))
            .asVarargsCollector(double[].class));
    }

    private static void addStaticFunctionHandles(
        SetMultimap<String, MethodHandle> map,
        MethodHandles.Lookup lookup
    ) throws NoSuchMethodException, IllegalAccessException {
        map.put("rotate", lookup.findStatic(Functions.class, "rotate",
            methodType(double.class, Variable.class, Variable.class, double.class)));
        map.put("swap", lookup.findStatic(Functions.class, "swap",
            methodType(double.class, Variable.class, Variable.class)));
        map.put("gmegabuf", lookup.findStatic(Functions.class, "gmegabuf",
            methodType(double.class, double.class)));
        map.put("gmegabuf", lookup.findStatic(Functions.class, "gmegabuf",
            methodType(double.class, double.class, double.class)));
        map.put("gclosest", lookup.findStatic(Functions.class, "gclosest",
            methodType(double.class, double.class, double.class, double.class, double.class,
                double.class, double.class)));
        map.put("random", lookup.findStatic(Functions.class, "random",
            methodType(double.class)));
        map.put("randint", lookup.findStatic(Functions.class, "randint",
            methodType(double.class, double.class)));
        map.put("perlin", lookup.findStatic(Functions.class, "perlin",
            methodType(double.class, double.class, double.class, double.class, double.class,
                double.class, double.class, double.class)));
        map.put("voronoi", lookup.findStatic(Functions.class, "voronoi",
            methodType(double.class, double.class, double.class, double.class, double.class,
                double.class)));
        map.put("ridgedmulti", lookup.findStatic(Functions.class, "ridgedmulti",
            methodType(double.class, double.class, double.class, double.class, double.class,
                double.class, double.class)));
        map.put("query", lookup.findStatic(Functions.class, "query",
            methodType(double.class, double.class, double.class, double.class, LocalSlot.class,
                LocalSlot.class)));
        map.put("queryAbs", lookup.findStatic(Functions.class, "queryAbs",
            methodType(double.class, double.class, double.class, double.class, LocalSlot.class,
                LocalSlot.class)));
        map.put("queryRel", lookup.findStatic(Functions.class, "queryRel",
            methodType(double.class, double.class, double.class, double.class, LocalSlot.class,
                LocalSlot.class)));
    }

    private void addInstanceFunctionHandles(
        SetMultimap<String, MethodHandle> map,
        MethodHandles.Lookup lookup
    ) throws NoSuchMethodException, IllegalAccessException {
        map.put("megabuf", lookup.findSpecial(Functions.class, "megabuf",
            methodType(double.class, double.class), Functions.class)
            .bindTo(this));
        map.put("megabuf", lookup.findSpecial(Functions.class, "megabuf",
            methodType(double.class, double.class, double.class), Functions.class)
            .bindTo(this));
        map.put("closest", lookup.findSpecial(Functions.class, "closest",
            methodType(double.class, double.class, double.class, double.class, double.class,
                double.class, double.class), Functions.class)
            .bindTo(this));
    }

    private static double rotate(Variable x, Variable y, double angle) {
        final double cosF = Math.cos(angle);
        final double sinF = Math.sin(angle);

        final double xOld = x.getValue();
        final double yOld = y.getValue();

        x.setValue(xOld * cosF - yOld * sinF);
        y.setValue(xOld * sinF + yOld * cosF);

        return 0.0;
    }

    private static double swap(Variable x, Variable y) {
        final double tmp = x.getValue();

        x.setValue(y.getValue());
        y.setValue(tmp);

        return 0.0;
    }


    private static final Int2ObjectMap<double[]> globalMegaBuffer = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<double[]> megaBuffer = new Int2ObjectOpenHashMap<>();

    private static double[] getSubBuffer(Int2ObjectMap<double[]> megabuf, int key) {
        return megabuf.computeIfAbsent(key, k -> new double[1024]);
    }

    private static double getBufferItem(final Int2ObjectMap<double[]> megabuf, final int index) {
        return getSubBuffer(megabuf, index & ~1023)[index & 1023];
    }

    private static double setBufferItem(final Int2ObjectMap<double[]> megabuf, final int index, double value) {
        return getSubBuffer(megabuf, index & ~1023)[index & 1023] = value;
    }

    private static double gmegabuf(double index) {
        return getBufferItem(globalMegaBuffer, (int) index);
    }

    private static double gmegabuf(double index, double value) {
        return setBufferItem(globalMegaBuffer, (int) index, value);
    }

    private double megabuf(double index) {
        return getBufferItem(megaBuffer, (int) index);
    }

    private double megabuf(double index, double value) {
        return setBufferItem(megaBuffer, (int) index, value);
    }

    private double closest(double x, double y, double z, double index, double count, double stride) {
        return findClosest(
            megaBuffer, x, y, z, (int) index, (int) count, (int) stride
        );
    }

    private static double gclosest(double x, double y, double z, double index, double count, double stride) {
        return findClosest(
            globalMegaBuffer, x, y, z, (int) index, (int) count, (int) stride
        );
    }

    private static double findClosest(Int2ObjectMap<double[]> megabuf, double x, double y, double z, int index, int count, int stride) {
        int closestIndex = -1;
        double minDistanceSquared = Double.MAX_VALUE;

        for (int i = 0; i < count; ++i) {
            double currentX = getBufferItem(megabuf, index) - x;
            double currentY = getBufferItem(megabuf, index+1) - y;
            double currentZ = getBufferItem(megabuf, index+2) - z;

            double currentDistanceSquared = currentX*currentX + currentY*currentY + currentZ*currentZ;

            if (currentDistanceSquared < minDistanceSquared) {
                minDistanceSquared = currentDistanceSquared;
                closestIndex = index;
            }

            index += stride;
        }

        return closestIndex;
    }

    private static double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    private static double randint(double max) {
        return ThreadLocalRandom.current().nextInt((int) Math.floor(max));
    }

    private static final ThreadLocal<PerlinNoise> localPerlin = ThreadLocal.withInitial(PerlinNoise::new);

    private static double perlin(double seed, double x, double y, double z,
                                 double frequency, double octaves, double persistence) {
        PerlinNoise perlin = localPerlin.get();
        try {
            perlin.setSeed((int) seed);
            perlin.setFrequency(frequency);
            perlin.setOctaveCount((int) octaves);
            perlin.setPersistence(persistence);
        } catch (IllegalArgumentException e) {
            throw new EvaluationException(0, "Perlin noise error: " + e.getMessage());
        }
        return perlin.noise(Vector3.at(x, y, z));
    }

    private static final ThreadLocal<VoronoiNoise> localVoronoi = ThreadLocal.withInitial(VoronoiNoise::new);

    private static double voronoi(double seed, double x, double y, double z, double frequency) {
        VoronoiNoise voronoi = localVoronoi.get();
        try {
            voronoi.setSeed((int) seed);
            voronoi.setFrequency(frequency);
        } catch (IllegalArgumentException e) {
            throw new EvaluationException(0, "Voronoi error: " + e.getMessage());
        }
        return voronoi.noise(Vector3.at(x, y, z));
    }

    private static final ThreadLocal<RidgedMultiFractalNoise> localRidgedMulti = ThreadLocal.withInitial(RidgedMultiFractalNoise::new);

    private static double ridgedmulti(double seed, double x, double y, double z,
                                      double frequency, double octaves) {
        RidgedMultiFractalNoise ridgedMulti = localRidgedMulti.get();
        try {
            ridgedMulti.setSeed((int) seed);
            ridgedMulti.setFrequency(frequency);
            ridgedMulti.setOctaveCount((int) octaves);
        } catch (IllegalArgumentException e) {
            throw new EvaluationException(0, "Ridged multi error: " + e.getMessage());
        }
        return ridgedMulti.noise(Vector3.at(x, y, z));
    }

    private static double queryInternal(LocalSlot type, LocalSlot data, double typeId, double dataValue) {
        // Compare to input values and determine return value
        // -1 is a wildcard, always true
        double ret = ((type.getValue() == -1 || typeId == type.getValue())
            && (data.getValue() == -1 || dataValue == data.getValue())) ? 1.0 : 0.0;

        if (type instanceof Variable) {
            ((Variable) type).setValue(typeId);
        }
        if (data instanceof Variable) {
            ((Variable) data).setValue(dataValue);
        }

        return ret;
    }

    private static double query(double x, double y, double z, LocalSlot type, LocalSlot data) {
        final ExpressionEnvironment environment = Expression.getInstance().getEnvironment();

        // Read values from world
        final double typeId = environment.getBlockType(x, y, z);
        final double dataValue = environment.getBlockData(x, y, z);

        return queryInternal(type, data, typeId, dataValue);
    }

    private static double queryAbs(double x, double y, double z, LocalSlot type, LocalSlot data) {
        final ExpressionEnvironment environment = Expression.getInstance().getEnvironment();

        // Read values from world
        final double typeId = environment.getBlockTypeAbs(x, y, z);
        final double dataValue = environment.getBlockDataAbs(x, y, z);

        return queryInternal(type, data, typeId, dataValue);
    }

    private static double queryRel(double x, double y, double z, LocalSlot type, LocalSlot data) {
        final ExpressionEnvironment environment = Expression.getInstance().getEnvironment();

        // Read values from world
        final double typeId = environment.getBlockTypeRel(x, y, z);
        final double dataValue = environment.getBlockDataRel(x, y, z);

        return queryInternal(type, data, typeId, dataValue);
    }

    private Functions() {
    }

}
