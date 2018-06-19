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
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.util.*;

/**
 * Plants a tree.
 */
public class TreePlanter implements BlockTool {

    private TreeGenerator.TreeType treeType;

    public TreePlanter(TreeGenerator.TreeType treeType) {
        this.treeType = treeType;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.tree");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {

        EditSession editSession = session.createEditSession(player);

        try {
            boolean successful = false;
            
            for (int i = 0; i < 10; i++) {
                if (treeType.generate(editSession, clicked.toVector().add(0, 1, 0))) {
                    successful = true;
                    break;
                }
            }
            
            if (!successful) {
                player.printError("A tree can't go there.");
            }
        } catch (MaxChangedBlocksException e) {
            player.printError("Max. blocks changed reached.");
        } finally {
            session.remember(editSession);
        }

        return true;
    }

}
