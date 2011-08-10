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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class WorldEditBlockEvent extends BlockBreakEvent{

    public WorldEditBlockEvent(Block block, Player player) {
        super(block, player);
    }
    
    public WorldEditBlockEvent(Vector location, Player player) {
       this(player.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ()), player);
    }
    
}
