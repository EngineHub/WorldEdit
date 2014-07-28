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

import net.royawesome.jlibnoise.module.source.Voronoi;

/**
 * Generates Voronoi noise.
 */
public class VoronoiNoise extends JLibNoiseGenerator<Voronoi> {

    @Override
    protected Voronoi createModule() {
        return new Voronoi();
    }

    public double getFrequency() {
        return getModule().getFrequency();
    }

    public void setFrequency(double frequency) {
        getModule().setFrequency(frequency);
    }

    @Override
    public void setSeed(int seed) {
        getModule().setSeed(seed);
    }

    @Override
    public int getSeed() {
        return getModule().getSeed();
    }

}
