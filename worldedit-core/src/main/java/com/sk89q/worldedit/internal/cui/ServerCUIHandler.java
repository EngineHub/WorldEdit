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

package com.sk89q.worldedit.internal.cui;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Handles creation of server-side CUI systems.
 */
public class ServerCUIHandler {

    private ServerCUIHandler() {
    }

    /**
     * Creates a structure block that shows the region.
     *
     * <p>
     *     Null symbolises removal of the CUI.
     * </p>
     *
     * @param player The player to create the structure block for.
     * @return The structure block, or null
     */
    @Nullable
    public static BaseBlock createStructureBlock(Player player) {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
        RegionSelector regionSelector = session.getRegionSelector(player.getWorld());

        int posX, posY, posZ;
        int width, height, length;

        if (regionSelector instanceof CuboidRegionSelector) {
            if (regionSelector.isDefined()) {
                try {
                    CuboidRegion region = ((CuboidRegionSelector) regionSelector).getRegion();

                    posX = region.getMinimumPoint().getBlockX();
                    posY = region.getMinimumPoint().getBlockY();
                    posZ = region.getMinimumPoint().getBlockZ();

                    width = region.getWidth();
                    height = region.getHeight();
                    length = region.getLength();
                } catch (IncompleteRegionException e) {
                    // This will never happen.
                    e.printStackTrace();
                    return null;
                }
            } else {
                CuboidRegion region = ((CuboidRegionSelector) regionSelector).getIncompleteRegion();
                BlockVector3 point;
                if (region.getPos1() != null) {
                    point = region.getPos1();
                } else if (region.getPos2() != null) {
                    point = region.getPos2();
                } else {
                    // No more selection
                    return null;
                }

                // Just select the point.
                posX = point.getBlockX();
                posY = point.getBlockY();
                posZ = point.getBlockZ();
                width = 1;
                height = 1;
                length = 1;
            }
        } else {
            // We only support cuboid regions right now.
            return null;
        }

        if (width > 32 || length > 32 || height > 32) {
            // Structure blocks have a limit of 32x32x32
            return null;
        }

        // Borrowed this math from FAWE
        double rotX = player.getLocation().getYaw();
        double rotY = player.getLocation().getPitch();
        double xz = Math.cos(Math.toRadians(rotY));
        int x = (int) (player.getLocation().getX() - (-xz * Math.sin(Math.toRadians(rotX))) * 12);
        int z = (int) (player.getLocation().getZ() - (xz * Math.cos(Math.toRadians(rotX))) * 12);
        int y = Math.max(0, Math.min(Math.min(255, posY + 32), posY + 3));

        Map<String, Tag> structureTag = new HashMap<>();

        posX -= x;
        posY -= y;
        posZ -= z;

        if (Math.abs(posX) > 32 || Math.abs(posY) > 32 || Math.abs(posZ) > 32) {
            // Structure blocks have a limit
            return null;
        }

        structureTag.put("name", new StringTag("worldedit:" + player.getName()));
        structureTag.put("author", new StringTag(player.getName()));
        structureTag.put("metadata", new StringTag(""));
        structureTag.put("x", new IntTag(x));
        structureTag.put("y", new IntTag(y));
        structureTag.put("z", new IntTag(z));
        structureTag.put("posX", new IntTag(posX));
        structureTag.put("posY", new IntTag(posY));
        structureTag.put("posZ", new IntTag(posZ));
        structureTag.put("sizeX", new IntTag(width));
        structureTag.put("sizeY", new IntTag(height));
        structureTag.put("sizeZ", new IntTag(length));
        structureTag.put("rotation", new StringTag("NONE"));
        structureTag.put("mirror", new StringTag("NONE"));
        structureTag.put("mode", new StringTag("SAVE"));
        structureTag.put("ignoreEntities", new ByteTag((byte) 1));
        structureTag.put("showboundingbox", new ByteTag((byte) 1));
        structureTag.put("id", new StringTag(BlockTypes.STRUCTURE_BLOCK.getId()));

        return BlockTypes.STRUCTURE_BLOCK.getDefaultState().toBaseBlock(new CompoundTag(structureTag));
    }
}
