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

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;

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

        plugin.registerEvent("PLAYER_QUIT", this);
        plugin.registerEvent("PLAYER_INTERACT", this);
        plugin.registerEvent("PLAYER_COMMAND_PREPROCESS", this);
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getWorldEdit().markExpire(plugin.wrapPlayer(event.getPlayer()));
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        String[] split = event.getMessage().split(" ");

        if (plugin.getWorldEdit().handleCommand(plugin.wrapPlayer(event.getPlayer()), split)) {
            event.setCancelled(true);
        }
    }

    private boolean ignoreLeftClickAir = false;

    /**
     * Called when a player interacts
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        
        final LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        final LocalWorld world = player.getWorld();
        final WorldEdit we = plugin.getWorldEdit();

        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK: {
            final Block clickedBlock = event.getClickedBlock();
            final WorldVector pos = new WorldVector(world, clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());

            if (we.handleBlockLeftClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

            if (!ignoreLeftClickAir) {
                final int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        ignoreLeftClickAir = false;
                    }
                }, 2);

                if (taskId != -1) {
                    ignoreLeftClickAir = true;
                }
            }

            break;
        }

        case LEFT_CLICK_AIR:
            if (ignoreLeftClickAir) {
                break;
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

            break;

        case RIGHT_CLICK_BLOCK: {
            final Block clickedBlock = event.getClickedBlock();
            final WorldVector pos = new WorldVector(world, clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());

            if (we.handleBlockRightClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
            break;
        }

        case RIGHT_CLICK_AIR:
            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
            break;
        }
    }
}
