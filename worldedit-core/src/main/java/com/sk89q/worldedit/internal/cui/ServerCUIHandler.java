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

package com.sk89q.worldedit.internal.cui;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.linbus.tree.LinCompoundTag;

import javax.annotation.Nullable;

/**
 * Handles creation of server-side CUI systems.
 */
public class ServerCUIHandler {

    private static final int MAX_DISTANCE = 32;

    private ServerCUIHandler() {
    }

    public static int getMaxServerCuiSize() {
        int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.GAME_HOOKS).getDataVersion();

        // 1.16 increased maxSize to 48.
        return dataVersion >= 2566 ? 48 : 32;
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

        int posX;
        int posY;
        int posZ;
        int width;
        int height;
        int length;

        if (regionSelector instanceof CuboidRegionSelector) {
            if (regionSelector.isDefined()) {
                try {
                    CuboidRegion region = ((CuboidRegionSelector) regionSelector).getRegion();

                    posX = region.getMinimumPoint().x();
                    posY = region.getMinimumPoint().y();
                    posZ = region.getMinimumPoint().z();

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
                posX = point.x();
                posY = point.y();
                posZ = point.z();
                width = 1;
                height = 1;
                length = 1;
            }
        } else {
            // We only support cuboid regions right now.
            return null;
        }

        int maxSize = getMaxServerCuiSize();

        if (width > maxSize || length > maxSize || height > maxSize) {
            // Structure blocks have a limit of maxSize^3
            return null;
        }

        // Borrowed this math from FAWE
        final Location location = player.getLocation();
        double rotX = location.getYaw();
        double rotY = location.getPitch();
        double xz = Math.cos(Math.toRadians(rotY));
        int x = (int) (location.getX() - (-xz * Math.sin(Math.toRadians(rotX))) * 12);
        int z = (int) (location.getZ() - (xz * Math.cos(Math.toRadians(rotX))) * 12);
        int y = Math.max(
            player.getWorld().getMinY(),
            Math.min(Math.min(player.getWorld().getMaxY(), posY + MAX_DISTANCE), posY + 3)
        );

        posX -= x;
        posY -= y;
        posZ -= z;

        if (Math.abs(posX) > MAX_DISTANCE || Math.abs(posY) > MAX_DISTANCE || Math.abs(posZ) > MAX_DISTANCE) {
            // Structure blocks have a limit
            return null;
        }

        LinCompoundTag.Builder structureTag = LinCompoundTag.builder();
        structureTag.putString("name", "worldedit:" + player.getName());
        structureTag.putString("author", player.getName());
        structureTag.putString("metadata", "");
        structureTag.putInt("x", x);
        structureTag.putInt("y", y);
        structureTag.putInt("z", z);
        structureTag.putInt("posX", posX);
        structureTag.putInt("posY", posY);
        structureTag.putInt("posZ", posZ);
        structureTag.putInt("sizeX", width);
        structureTag.putInt("sizeY", height);
        structureTag.putInt("sizeZ", length);
        structureTag.putString("rotation", "NONE");
        structureTag.putString("mirror", "NONE");
        structureTag.putString("mode", "SAVE");
        structureTag.putByte("ignoreEntities", (byte) 1);
        structureTag.putByte("showboundingbox", (byte) 1);
        structureTag.putString("id", BlockTypes.STRUCTURE_BLOCK.getId());

        return BlockTypes.STRUCTURE_BLOCK.getDefaultState().toBaseBlock(structureTag.build());
    }
}
