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
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;

/**
 * A mode that replaces one block.
 */
public class BlockReplacer implements DoubleActionBlockTool {

    private BlockStateHolder targetBlock;

    public BlockReplacer(BlockStateHolder targetBlock) {
        this.targetBlock = targetBlock;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.replacer");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {
        BlockBag bag = session.getBlockBag(player);

        EditSession editSession = session.createEditSession(player);

        try {
            editSession.setBlock(clicked.toVector(), targetBlock);
        } catch (MaxChangedBlocksException ignored) {
        } finally {
            if (bag != null) {
                bag.flushChanges();
            }
            session.remember(editSession);
        }

        return true;
    }


    @Override
    public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session, com.sk89q.worldedit.util.Location clicked) {
        EditSession editSession = session.createEditSession(player);
        targetBlock = (editSession).getBlock(clicked.toVector());
        BlockType type = targetBlock.getBlockType();

        if (type != null) {
            player.print("Replacer tool switched to: " + type.getName());
        }

        return true;
    }

}
