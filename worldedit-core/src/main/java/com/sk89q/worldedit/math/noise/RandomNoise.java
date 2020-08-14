/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.math.noise;

import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;

import java.util.Random;

/**
 * Generates noise using {@link java.util.Random}. Every time a noise
 * generating function is called, a new value will be returned.
 */
public class RandomNoise implements NoiseGenerator {

    private final Random random;

    /**
     * Create a new noise generator using the given {@code Random}.
     *
     * @param random the random instance
     */
    public RandomNoise(Random random) {
        this.random = random;
    }

    /**
     * Create a new noise generator with a newly constructed {@code Random}
     * instance.
     */
    public RandomNoise() {
        this(new Random());
    }

    @Override
    public float noise(Vector2 position) {
        return random.nextFloat();
    }

    @Override
    public float noise(Vector3 position) {
        return random.nextFloat();
    }

}
