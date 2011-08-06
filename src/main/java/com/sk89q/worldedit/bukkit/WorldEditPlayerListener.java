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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditPlayerListener extends PlayerListener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;
    
    /**
     * Called when a player plays an animation, such as an arm swing
     * 
     * @param event Relevant event details
     */
    
    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldEditPlayerListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getWorldEdit().markExpire(wrapPlayer(event.getPlayer()));
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] split = event.getMessage().split(" ");
        
        if (plugin.getWorldEdit().handleCommand(wrapPlayer(event.getPlayer()), split)) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a player interacts
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            LocalWorld world = new BukkitWorld(event.getClickedBlock().getWorld());
            WorldVector pos = new WorldVector(world, event.getClickedBlock().getX(),
                    event.getClickedBlock().getY(), event.getClickedBlock().getZ());
            LocalPlayer player = wrapPlayer(event.getPlayer());

            if (plugin.getWorldEdit().handleBlockLeftClick(player, pos)) {
                event.setCancelled(true);
            }

            if (plugin.getWorldEdit().handleArmSwing(wrapPlayer(event.getPlayer()))) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (plugin.getWorldEdit().handleArmSwing(wrapPlayer(event.getPlayer()))) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            LocalWorld world = new BukkitWorld(event.getClickedBlock().getWorld());
            WorldVector pos = new WorldVector(world, event.getClickedBlock().getX(),
                    event.getClickedBlock().getY(), event.getClickedBlock().getZ());
            LocalPlayer player = wrapPlayer(event.getPlayer());
            
            if (plugin.getWorldEdit().handleBlockRightClick(player, pos)) {
                event.setCancelled(true);
            }

            if (plugin.getWorldEdit().handleRightClick(wrapPlayer(event.getPlayer()))) {
                event.setCancelled(true);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (plugin.getWorldEdit().handleRightClick(wrapPlayer(event.getPlayer()))) {
                event.setCancelled(true);
            }
        }
    }
    
    private BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(plugin, plugin.getServerInterface(), player);
    }
}
