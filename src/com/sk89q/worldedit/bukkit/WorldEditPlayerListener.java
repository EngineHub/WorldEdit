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

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

import com.sk89q.worldedit.WorldEditPlayer;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditPlayerListener extends PlayerListener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;
    
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
    public void onPlayerQuit(PlayerEvent event) {
        WorldEditPlayer player = new BukkitPlayer(plugin.getServer().getOnlinePlayers()[0]);
        plugin.controller.handleDisconnect(player);
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    public void onPlayerCommand(PlayerChatEvent event) {
        WorldEditPlayer player = new BukkitPlayer(plugin.getServer().getOnlinePlayers()[0]);
        plugin.controller.handleCommand(player, event.getMessage().split(" "));
    }
}
