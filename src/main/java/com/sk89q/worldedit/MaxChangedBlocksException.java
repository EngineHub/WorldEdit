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

package com.sk89q.worldedit;

/**
 * Thrown when too many blocks are changed (which may be limited
 * due to the configuration).
 */
public class MaxChangedBlocksException extends WorldEditException {

    int maxBlocks;

    /**
     * Create a new instance.
     *
     * @param maxBlocks the maximum number of blocks that can be changed
     */
    public MaxChangedBlocksException(int maxBlocks) {
        this.maxBlocks = maxBlocks;
    }

    /**
     * Get the limit.
     *
     * @return the maximum number of blocks that can be changed
     */
    public int getBlockLimit() {
        return maxBlocks;
    }

}
