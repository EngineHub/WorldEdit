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

package com.sk89q.worldedit.forge.internal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Utility methods for setting tile entities in the world.
 */
public final class TileEntityUtils {

    private TileEntityUtils() {
    }

    /**
     * Set a tile entity at the given location using the tile entity ID from
     * the tag.
     *
     * @param world the world
     * @param position the position
     * @param tag the tag for the tile entity
     */
    static boolean setTileEntity(Level world, BlockPos position, CompoundTag tag) {
        BlockEntity tileEntity = BlockEntity.loadStatic(position, world.getBlockState(position), tag);
        if (tileEntity == null) {
            return false;
        }
        world.setBlockEntity(tileEntity);
        return true;
    }

    public static CompoundTag copyNbtData(BlockEntity tile) {
        return tile.save(new CompoundTag());
    }
}
