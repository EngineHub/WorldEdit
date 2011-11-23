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
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditCriticalPlayerListener extends PlayerListener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;

    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldEditCriticalPlayerListener(WorldEditPlugin plugin) {
        this.plugin = plugin;

        plugin.registerEvent("PLAYER_JOIN", this, Priority.Lowest);
    }

    /**
     * Called when a player joins a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        wrapPlayer(event.getPlayer()).dispatchCUIHandshake();
    }

    private BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(plugin, plugin.getServerInterface(), player);
    }
}
