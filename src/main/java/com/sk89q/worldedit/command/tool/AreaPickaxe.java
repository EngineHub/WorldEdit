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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A super pickaxe mode that will remove blocks in an area.
 * 
 * @author sk89q
 */
public class AreaPickaxe implements BlockTool {
    private static final BaseBlock air = new BaseBlock(0);
    private int range;

    public AreaPickaxe(int range) {
        this.range = range;
    }

    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.superpickaxe.area");
    }

    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        LocalWorld world = clicked.getWorld();
        int ox = clicked.getBlockX();
        int oy = clicked.getBlockY();
        int oz = clicked.getBlockZ();
        int initialType = world.getBlockType(clicked);

        if (initialType == 0) {
            return true;
        }

        if (initialType == BlockID.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        EditSession editSession = session.createEditSession(player);
        editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);

        try {
            for (int x = ox - range; x <= ox + range; ++x) {
                for (int y = oy - range; y <= oy + range; ++y) {
                    for (int z = oz - range; z <= oz + range; ++z) {
                        Vector pos = new Vector(x, y, z);
                        if (editSession.getBlockType(pos) != initialType) {
                            continue;
                        }

                        world.queueBlockBreakEffect(server, pos, initialType, clicked.distanceSq(pos));

                        editSession.setBlock(pos, air);
                    }
                }
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            editSession.flushQueue();
            session.remember(editSession);
        }

        return true;
    }
}
