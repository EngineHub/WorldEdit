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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    static boolean setTileEntity(World world, BlockPos position, CompoundNBT tag) {
        TileEntity tileEntity = TileEntity.func_235657_b_(world.getBlockState(position), tag);
        if (tileEntity == null) {
            return false;
        }
        world.setTileEntity(new BlockPos(position.getX(), position.getY(), position.getZ()), tileEntity);
        return true;
    }

    public static CompoundNBT copyNbtData(TileEntity tile) {
        CompoundNBT tag = new CompoundNBT();
        tile.write(tag);
        return tag;
    }
}
