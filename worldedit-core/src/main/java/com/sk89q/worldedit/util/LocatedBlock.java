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

package com.sk89q.worldedit.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.Objects;

/**
 * Represents a block located at some position.
 */
public final class LocatedBlock {

    private final BlockVector3 location;
    private final BaseBlock block;

    public LocatedBlock(BlockVector3 location, BaseBlock block) {
        this.location = checkNotNull(location);
        this.block = checkNotNull(block);
    }

    public BlockVector3 getLocation() {
        return location;
    }

    public BaseBlock getBlock() {
        return block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, block);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        LocatedBlock lb = (LocatedBlock) obj;
        return Objects.equals(location, lb.location) && Objects.equals(block, lb.block);
    }

}
