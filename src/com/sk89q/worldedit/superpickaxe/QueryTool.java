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

package com.sk89q.worldedit.superpickaxe;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;

/**
 * Plants a tree.
 * 
 * @author sk89q
 */
public class QueryTool implements SuperPickaxeMode {

    @Override
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        
        LocalWorld world = clicked.getWorld();
        BaseBlock block = (new EditSession(server, world, 0)).rawGetBlock(clicked);

        player.print("\u00A79@" + clicked + ": " + "\u00A7e"
                + "Type: " + block.getID() + "\u00A77" + " ("
                + BlockType.fromID(block.getID()).getName() + ") "
                + "\u00A7f"
                + "[" + block.getData() + "]");

        if (block instanceof MobSpawnerBlock) {
            player.printRaw("\u00A7e" + "Mob Type: "
                    + ((MobSpawnerBlock)block).getMobType());
        }
    
        return true;
    }

}
