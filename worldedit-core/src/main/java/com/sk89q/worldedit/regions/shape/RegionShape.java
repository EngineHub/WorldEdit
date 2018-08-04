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

package com.sk89q.worldedit.regions.shape;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getMaterial} method.
 */
public class RegionShape extends ArbitraryShape {

    public RegionShape(Region extent) {
        super(extent);
    }

    @Override
    protected BlockStateHolder getMaterial(int x, int y, int z, BlockStateHolder defaultMaterial) {
        if (!this.extent.contains(new Vector(x, y, z))) {
            return null;
        }

        return defaultMaterial;
    }

}
