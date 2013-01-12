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
import com.sk89q.worldedit.blocks.*;

/**
 * Plants a tree.
 * 
 * @author sk89q
 */
public class QueryTool implements BlockTool {

    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.tool.info");
    }

    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {

        LocalWorld world = clicked.getWorld();
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, 0, player);
        BaseBlock block = (editSession).rawGetBlock(clicked);
        BlockType type = BlockType.fromID(block.getType());

        player.print("\u00A79@" + clicked + ": " + "\u00A7e"
                + "#" + block.getType() + "\u00A77" + " ("
                + (type == null ? "Unknown" : type.getName()) + ") "
                + "\u00A7f"
                + "[" + block.getData() + "]" + " (" + world.getBlockLightLevel(clicked) + "/" + world.getBlockLightLevel(clicked.add(0, 1, 0)) + ")");

        if (block instanceof MobSpawnerBlock) {
            player.printRaw("\u00A7e" + "Mob Type: "
                    + ((MobSpawnerBlock) block).getMobType());
        } else if (block instanceof NoteBlock) {
            player.printRaw("\u00A7e" + "Note block: "
                    + ((NoteBlock) block).getNote());
        } else if (block.getType() == BlockID.CLOTH) {
            // Should never be null
            player.printRaw("\u00A7e" + "Color: "
                    + ClothColor.fromID(block.getData()).getName());
        }

        return true;
    }

}
