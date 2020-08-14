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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.noise.NoiseGenerator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A mask that uses a noise generator and returns true whenever the noise
 * generator returns a value above the given density.
 */
public class NoiseFilter2D extends AbstractMask2D {

    private NoiseGenerator noiseGenerator;
    private double density;

    /**
     * Create a new noise filter.
     *
     * @param noiseGenerator the noise generator
     * @param density the density
     */
    public NoiseFilter2D(NoiseGenerator noiseGenerator, double density) {
        setNoiseGenerator(noiseGenerator);
        setDensity(density);
    }

    /**
     * Get the noise generator.
     *
     * @return the noise generator
     */
    public NoiseGenerator getNoiseGenerator() {
        return noiseGenerator;
    }

    /**
     * Set the noise generator.
     *
     * @param noiseGenerator a noise generator
     */
    public void setNoiseGenerator(NoiseGenerator noiseGenerator) {
        checkNotNull(noiseGenerator);
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Get the probability of passing as a number between 0 and 1 (inclusive).
     *
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * Set the probability of passing as a number between 0 and 1 (inclusive).
     */
    public void setDensity(double density) {
        checkArgument(density >= 0, "density must be >= 0");
        checkArgument(density <= 1, "density must be <= 1");
        this.density = density;
    }

    @Override
    public boolean test(BlockVector2 pos) {
        return noiseGenerator.noise(pos.toVector2()) <= density;
    }

}
