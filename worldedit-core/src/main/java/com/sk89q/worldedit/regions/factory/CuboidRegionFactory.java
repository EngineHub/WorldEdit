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

public class CuboidRegionFactory implements RegionFactory {

    private final double height;

    public CuboidRegionFactory() {
        this(-1);
    }

    public CuboidRegionFactory(double height) {
        this.height = height;
    }

    @Override
    public Region createCenteredAt(BlockVector3 position, double size) {
        CuboidRegion region = CuboidRegion.fromCenter(position, (int) size);
        if (height > 0) {
            region.setPos1(region.getPos1().withY(position.getBlockY() - (int) (height / 2)));
            region.setPos2(region.getPos2().withY(position.getBlockY() + (int) (height / 2)));
        }
        return region;
    }

}
