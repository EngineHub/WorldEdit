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

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;

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
     * Called when a block is damaged (or broken)
     *
     * @param event Relevant event details
     */
    public void onBlockDamage(BlockDamageEvent event) {
        LocalWorld world = new BukkitWorld(event.getBlock().getWorld());
        WorldVector pos = new WorldVector(world, event.getBlock().getX(),
                event.getBlock().getY(), event.getBlock().getZ());
        LocalPlayer player = wrapPlayer(event.getPlayer());
        
        if (plugin.controller.handleBlockLeftClick(player, pos)) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a player right clicks a block
     *
     * @param event Relevant event details
     */
    public void onBlockRightClick(BlockRightClickEvent event) {
        LocalWorld world = new BukkitWorld(event.getBlock().getWorld());
        WorldVector pos = new WorldVector(world, event.getBlock().getX(),
                event.getBlock().getY(), event.getBlock().getZ());
        LocalPlayer player = wrapPlayer(event.getPlayer());
        
        plugin.controller.handleBlockRightClick(player, pos);
    }
    
    private BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(plugin.server, player);
    }
}
