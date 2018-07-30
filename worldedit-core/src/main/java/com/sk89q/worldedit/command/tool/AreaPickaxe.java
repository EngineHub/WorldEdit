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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

/**
 * A super pickaxe mode that will remove blocks in an area.
 */
public class AreaPickaxe implements BlockTool {

    private int range;

    public AreaPickaxe(int range) {
        this.range = range;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.superpickaxe.area");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {
        int ox = clicked.getBlockX();
        int oy = clicked.getBlockY();
        int oz = clicked.getBlockZ();
        BlockType initialType = clicked.getExtent().getBlock(clicked.toVector()).getBlockType();

        if (initialType == BlockTypes.AIR) {
            return true;
        }

        if (initialType == BlockTypes.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        EditSession editSession = session.createEditSession(player);
        editSession.getSurvivalExtent().setToolUse(config.superPickaxeManyDrop);

        try {
            for (int x = ox - range; x <= ox + range; ++x) {
                for (int y = oy - range; y <= oy + range; ++y) {
                    for (int z = oz - range; z <= oz + range; ++z) {
                        Vector pos = new Vector(x, y, z);
                        if (editSession.getBlock(pos).getBlockType() != initialType) {
                            continue;
                        }

                        ((World) clicked.getExtent()).queueBlockBreakEffect(server, pos, initialType, clicked.toVector().distanceSq(pos));

                        editSession.setBlock(pos, BlockTypes.AIR.getDefaultState());
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
