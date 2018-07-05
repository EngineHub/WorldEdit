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
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;

/**
 * A mode that cycles the data values of supported blocks.
 */
public class BlockDataCyler implements DoubleActionBlockTool {

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.data-cycler");
    }

    private boolean handleCycle(Platform server, LocalConfiguration config,
            Player player, LocalSession session, Location clicked, boolean forward) {

        World world = (World) clicked.getExtent();

        BlockState block = world.getBlock(clicked.toVector());

        if (!config.allowedDataCycleBlocks.isEmpty()
                && !player.hasPermission("worldedit.override.data-cycler")
                && !config.allowedDataCycleBlocks.contains(block.getBlockType().getId())) {
            player.printError("You are not permitted to cycle the data value of that block.");
            return true;
        }

        if (block.getStates().keySet().isEmpty()) {
            player.printError("That block's data cannot be cycled!");
        } else {
            BlockState newBlock = block;

            // TODO Forward = cycle value, Backward = Next property
            //        int increment = forward ? 1 : -1;
            //        BaseBlock newBlock = new BaseBlock(type, BlockData.cycle(type, data, increment));
            EditSession editSession = session.createEditSession(player);

            try {
                editSession.setBlock(clicked.toVector(), newBlock);
            } catch (MaxChangedBlocksException e) {
                player.printError("Max blocks change limit reached.");
            } finally {
                session.remember(editSession);
            }
        }

        return true;
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {
        return handleCycle(server, config, player, session, clicked, true);
    }

    @Override
    public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {
        return handleCycle(server, config, player, session, clicked, false);
    }

}
