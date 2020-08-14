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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;

import javax.annotation.Nullable;

public class SmoothBrush implements Brush {

    private final Mask mask;
    private final int iterations;

    public SmoothBrush(int iterations) {
        this(iterations, null);
    }

    public SmoothBrush(int iterations, @Nullable Mask mask) {
        this.iterations = iterations;
        this.mask = mask;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        Vector3 posDouble = position.toVector3();
        Location min = new Location(editSession.getWorld(), posDouble.subtract(size, size, size));
        BlockVector3 max = posDouble.add(size, size + 10, size).toBlockPoint();
        Region region = new CuboidRegion(editSession.getWorld(), min.toVector().toBlockPoint(), max);
        HeightMap heightMap = new HeightMap(editSession, region, mask);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        heightMap.applyFilter(filter, iterations);
    }

}
