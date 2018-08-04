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

import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A mode that cycles the data values of supported blocks.
 */
public class BlockDataCyler implements DoubleActionBlockTool {

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.data-cycler");
    }

    private Map<UUID, Property<?>> selectedProperties = new HashMap<>();

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
            Property currentProperty = selectedProperties.get(player.getUniqueId());

            if (currentProperty == null || (forward && block.getState(currentProperty) == null)) {
                currentProperty = block.getStates().keySet().stream().findFirst().get();
                selectedProperties.put(player.getUniqueId(), currentProperty);
            }

            if (forward) {
                block.getState(currentProperty);
                int index = currentProperty.getValues().indexOf(block.getState(currentProperty));
                index = (index + 1) % currentProperty.getValues().size();
                BlockState newBlock = block.with(currentProperty, currentProperty.getValues().get(index));

                EditSession editSession = session.createEditSession(player);

                try {
                    editSession.setBlock(clicked.toVector(), newBlock);
                    player.print("Value of " + currentProperty.getName() + " is now " + currentProperty.getValues().get(index).toString());
                } catch (MaxChangedBlocksException e) {
                    player.printError("Max blocks change limit reached.");
                } finally {
                    session.remember(editSession);
                }
            } else {
                List<Property<?>> properties = Lists.newArrayList(block.getStates().keySet());
                int index = properties.indexOf(currentProperty);
                index = (index + 1) % properties.size();
                currentProperty = properties.get(index);
                selectedProperties.put(player.getUniqueId(), currentProperty);
                player.print("Now cycling " + currentProperty.getName());
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
