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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.operation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.noise.NoiseGenerator;
import com.sk89q.worldedit.noise.RandomNoise;

/**
 * Randomly applies the given {@link RegionFunction} onto random ground blocks.
 * <p>
 * This class can be used to generate a structure randomly over an area.
 */
public class GroundScatterFunction extends GroundFindingFunction {

    private NoiseGenerator noiseGenerator;
    private RegionFunction function;
    private double density;

    /**
     * Create a new instance using a {@link RandomNoise} as the noise generator.
     *
     * @param editSession the edit session
     * @param function the function
     */
    public GroundScatterFunction(EditSession editSession, RegionFunction function) {
        this(editSession, function, new RandomNoise());
    }

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     * @param function the function
     */
    public GroundScatterFunction(EditSession editSession, RegionFunction function, NoiseGenerator noiseGenerator) {
        super(editSession);
        this.function = function;
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Get the density (0 <= density <= 1) which indicates the threshold that
     * must be met for the function to be applied to a column.
     *
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * Set the density (0 <= density <= 1) which indicates the threshold that
     * must be met for the function to be applied to a column.
     *
     * @param density the density
     */
    public void setDensity(double density) {
        this.density = density;
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
     * @param noiseGenerator the noise generator
     */
    public void setNoiseGenerator(NoiseGenerator noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
    }

    /**
     * Get the function to apply.
     *
     * @return the region function
     */
    public RegionFunction getFunction() {
        return function;
    }

    /**
     * Set the function to apply.
     *
     * @param function the region function
     */
    public void setFunction(RegionFunction function) {
        this.function = function;
    }

    @Override
    protected boolean shouldContinue(Vector2D pt) {
        return noiseGenerator.noise(pt) <= density;
    }

    @Override
    protected boolean apply(Vector position, BaseBlock block) throws WorldEditException {
        return function.apply(position);
    }
}
