// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;

/**
 * A mode that replaces one block.
 * 
 * @author sk89q
 */
public class BlockReplacer implements DoubleActionBlockTool {
    private Pattern targetPattern;

    public BlockReplacer(Pattern targetPattern) {
        this.targetPattern = targetPattern;
    }

    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.tool.replacer");
    }

    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {

        BlockBag bag = session.getBlockBag(player);

        LocalWorld world = clicked.getWorld();
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1, bag, player);

        try {
            editSession.setBlock(clicked, targetPattern);
        } catch (MaxChangedBlocksException e) {
        } finally {
            if (bag != null) {
                bag.flushChanges();
            }
            session.remember(editSession);
        }

        return true;
    }

    public boolean actSecondary(ServerInterface server,
            LocalConfiguration config, LocalPlayer player,
            LocalSession session, WorldVector clicked) {

        LocalWorld world = clicked.getWorld();
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1, player);
        final BaseBlock block = editSession.getBlock(clicked);
        targetPattern = new SingleBlockPattern(block);
        BlockType type = BlockType.fromID(block.getType());

        if (type != null) {
            player.print("Replacer tool switched to: " + type.getName());
        }

        return true;
    }

}
