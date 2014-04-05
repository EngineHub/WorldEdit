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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class SmoothBrush implements Brush {
    private int iterations;
    private boolean naturalOnly;

    public SmoothBrush(int iterations) {
        this(iterations, false);
    }

    public SmoothBrush(int iterations, boolean naturalOnly) {
        this.iterations = iterations;
        this.naturalOnly = naturalOnly;
    }

    public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
        double rad = size;
        WorldVector min = new WorldVector(LocalWorldAdapter.wrap(editSession.getWorld()), pos.subtract(rad, rad, rad));
        Vector max = pos.add(rad, rad + 10, rad);
        Region region = new CuboidRegion(editSession.getWorld(), min, max);
        HeightMap heightMap = new HeightMap(editSession, region, naturalOnly);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        heightMap.applyFilter(filter, iterations);
    }
}
