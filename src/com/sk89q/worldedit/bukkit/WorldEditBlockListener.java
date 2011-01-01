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

import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBrokenEvent;
import org.bukkit.event.block.BlockRightClickedEvent;
import com.sk89q.worldedit.*;

public class WorldEditBlockListener extends BlockListener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;
    
    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldEditBlockListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Called when a block is broken (or destroyed)
     *
     * @param event Relevant event details
     */
    public void onBlockBroken(BlockBrokenEvent event) {
        Vector pos = new Vector(event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ());
        WorldEditPlayer player = new BukkitPlayer(plugin.getServer().getOnlinePlayers()[0]);
        plugin.controller.handleBlockLeftClick(player, pos);
    }
    
    /**
     * Called when a player right clicks a block
     *
     * @param event Relevant event details
     */
    public void onBlockRightClicked(BlockRightClickedEvent event) {
        Vector pos = new Vector(event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ());
        WorldEditPlayer player = new BukkitPlayer(plugin.getServer().getOnlinePlayers()[0]);
        plugin.controller.handleBlockRightClick(player, pos);
    }
}
