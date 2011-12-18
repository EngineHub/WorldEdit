// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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
import com.sk89q.worldedit.util.TreeGenerator;

/**
 * Plants a tree.
 * 
 * @author sk89q
 */
public class TreePlanter implements BlockTool {
    private TreeGenerator gen;

    public TreePlanter(TreeGenerator gen) {
        this.gen = gen;
    }

    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.tool.tree");
    }

    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {

        EditSession editSession = session.createEditSession(player);

        try {
            if (!gen.generate(editSession, clicked.add(0, 1, 0))) {
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
