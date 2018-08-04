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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Generates solid and hollow shapes according to materials returned by the
 * {@link #getMaterial} method.
 */
public abstract class ArbitraryShape {

    protected final Region extent;

    public ArbitraryShape(Region extent) {
        this.extent = extent;
    }

    protected Region getExtent() {
        return extent;
    }

    /**
     * Override this function to specify the shape to generate.
     *
     * @param x X coordinate to be queried
     * @param y Y coordinate to be queried
     * @param z Z coordinate to be queried
     * @param defaultMaterial The material returned by the pattern for the current block.
     * @return material to place or null to not place anything.
     */
    protected abstract BlockStateHolder getMaterial(int x, int y, int z, BlockStateHolder defaultMaterial);

    /**
     * Generates the shape.
     *
     * @param editSession The EditSession to use.
     * @param pattern The pattern to generate default materials from.
     * @param hollow Specifies whether to generate a hollow shape.
     * @return number of affected blocks.
     * @throws MaxChangedBlocksException
     */
    public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
        int affected = 0;

        for (BlockVector position : getExtent()) {
            int x = position.getBlockX();
            int y = position.getBlockY();
            int z = position.getBlockZ();

            if (!hollow) {
                final BlockStateHolder material = getMaterial(x, y, z, pattern.apply(position));
                if (material != null && editSession.setBlock(position, material)) {
                    ++affected;
                }

                continue;
            }

            final BlockStateHolder material = getMaterial(x, y, z, pattern.apply(position));
            if (material == null) {
                continue;
            }

            if (editSession.setBlock(position, material)) {
                ++affected;
            }
        }

        return affected;
    }

}
