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

package com.sk89q.worldedit.math.noise;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;

import net.royawesome.jlibnoise.module.Module;

import java.util.Random;

abstract class JLibNoiseGenerator<V extends Module> implements NoiseGenerator {

    private static final Random RANDOM = new Random();
    private final V module;

    JLibNoiseGenerator() {
        module = createModule();
        setSeed(RANDOM.nextInt());
    }

    protected abstract V createModule();

    protected V getModule() {
        return module;
    }

    public abstract void setSeed(int seed);

    public abstract int getSeed();

    @Override
    public float noise(Vector2 position) {
        return forceRange(module.GetValue(position.getX(), 0, position.getZ()));
    }

    @Override
    public float noise(Vector3 position) {
        return forceRange(module.GetValue(position.getX(), position.getY(), position.getZ()));
    }

    private float forceRange(double value) {
        return (float) Math.max(0, Math.min(1, value / 2.0 + 0.5));
    }

}
