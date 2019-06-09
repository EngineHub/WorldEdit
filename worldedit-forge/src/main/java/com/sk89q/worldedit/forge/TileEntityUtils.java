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

package com.sk89q.worldedit.forge;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector3;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Utility methods for setting tile entities in the world.
 */
final class TileEntityUtils {

    private TileEntityUtils() {
    }

    /**
     * Update the given tag compound with position information.
     *
     * @param tag the tag
     * @param position the position
     */
    private static void updateForSet(CompoundNBT tag, BlockVector3 position) {
        checkNotNull(tag);
        checkNotNull(position);

        tag.put("x", new IntNBT(position.getBlockX()));
        tag.put("y", new IntNBT(position.getBlockY()));
        tag.put("z", new IntNBT(position.getBlockZ()));
    }

    /**
     * Set a tile entity at the given location using the tile entity ID from
     * the tag.
     *
     * @param world the world
     * @param position the position
     * @param tag the tag for the tile entity (may be null to do nothing)
     */
    static void setTileEntity(World world, BlockVector3 position, @Nullable CompoundNBT tag) {
        if (tag != null) {
            updateForSet(tag, position);
            TileEntity tileEntity = TileEntity.create(tag);
            if (tileEntity != null) {
                world.setTileEntity(new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ()), tileEntity);
            }
        }
    }

    public static CompoundNBT copyNbtData(TileEntity tile) {
        CompoundNBT tag = new CompoundNBT();
        tile.write(tag);
        return tag;
    }
}
