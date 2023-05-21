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

package com.sk89q.worldedit.regions.factory;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * A factory for a cuboid region, with a fixed minimum and maximum Y position.
 */
public class FixedHeightCuboidRegionFactory implements RegionFactory {
    private final int minY;
    private final int maxY;

    public FixedHeightCuboidRegionFactory(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Region createCenteredAt(BlockVector3 position, double size) {
        CuboidRegion region = CuboidRegion.fromCenter(position, (int) size);
        region.setPos1(region.getPos1().withY(minY));
        region.setPos2(region.getPos2().withY(maxY));
        return region;
    }
}
