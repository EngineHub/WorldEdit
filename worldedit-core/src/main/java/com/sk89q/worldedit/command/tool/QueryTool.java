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
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.World;

/**
 * Looks up information about a block.
 */
public class QueryTool implements BlockTool {

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.info");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {

        World world = (World) clicked.getExtent();
        EditSession editSession = session.createEditSession(player);
        BlockStateHolder block = editSession.getFullBlock(clicked.toVector());

        player.print("\u00A79@" + clicked.toVector() + ": " + "\u00A7e"
                + block.getBlockType().getName() + "\u00A77" + " ("
                + block.toString() + ") "
                + "\u00A7f"
                + " (" + world.getBlockLightLevel(clicked.toVector()) + "/" + world.getBlockLightLevel(clicked.toVector().add(0, 1, 0)) + ")");

        if (block instanceof MobSpawnerBlock) {
            player.printRaw("\u00A7e" + "Mob Type: "
                    + ((MobSpawnerBlock) block).getMobType());
        }

        return true;
    }

}
