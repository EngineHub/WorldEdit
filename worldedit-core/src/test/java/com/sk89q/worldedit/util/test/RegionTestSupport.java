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

package com.sk89q.worldedit.util.test;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Small helpers shared by the region tests.
 */
public final class RegionTestSupport {

    private RegionTestSupport() {
    }

    public static BlockVector3 at(int x, int y, int z) {
        return BlockVector3.at(x, y, z);
    }

    /**
     * Builds a convex hull without a world from the given vertices, in order.
     */
    public static ConvexPolyhedralRegion hull(BlockVector3... vertices) {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((World) null);
        for (BlockVector3 vertex : vertices) {
            region.addVertex(vertex);
        }
        return region;
    }

    /**
     * Gets every block the region's iterator returns, in iteration order.
     */
    public static List<BlockVector3> iterate(Region region) {
        List<BlockVector3> points = new ArrayList<>();
        for (BlockVector3 point : region) {
            points.add(point);
        }
        return points;
    }

    /**
     * Gets the distinct blocks the region's iterator returns.
     */
    public static Set<BlockVector3> collect(Region region) {
        return new HashSet<>(iterate(region));
    }
}
