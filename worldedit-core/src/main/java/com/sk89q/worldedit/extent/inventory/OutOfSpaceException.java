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

package com.sk89q.worldedit.extent.inventory;

import com.sk89q.worldedit.world.block.BlockType;

/**
 * Thrown when the target inventory of a block bag is full.
 */
public class OutOfSpaceException extends BlockBagException {

    private final BlockType type;

    /**
     * Construct the object.
     *
     * @param type the type of the block
     */
    public OutOfSpaceException(BlockType type) {
        this.type = type;
    }

    /**
     * Get the type of the block.
     *
     * @return the type
     */
    public BlockType getType() {
        return this.type;
    }
}
