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

package com.sk89q.worldedit.spout;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import org.spout.api.Spout;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerChatEvent;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.event.player.PlayerJoinEvent;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.api.event.server.PreCommandEvent;
import org.spout.api.geo.discrete.Point;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditPlayerListener implements Listener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;

    private boolean ignoreLeftClickAir = false;

    private final static Pattern cuipattern = Pattern.compile("u00a74u00a75u00a73u00a74([^|]*)\\|?(.*)");

    /**
     * Construct the object;
     * 
     * @param plugin
     */
    public WorldEditPlayerListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(order = Order.EARLIEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.wrapPlayer(event.getPlayer()).dispatchCUIHandshake();
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @EventHandler
    public void onPlayerQuit(PlayerLeaveEvent event) {
        plugin.getWorldEdit().markExpire(plugin.wrapPlayer(event.getPlayer()));
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    //@EventHandler(order = Order.EARLY)
    public void onPlayerCommandPreprocess(PreCommandEvent event) {

        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split[0] = "/" + split[0];
            split = plugin.getWorldEdit().commandDetection(split);
            event.setMessage(StringUtil.joinString(split, " "));
        }
    }

    /**
     * Called when a player interacts
     *
     * @param event Relevant event details
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        final LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        final LocalWorld world = player.getWorld();
        final WorldEdit we = plugin.getWorldEdit();

        PlayerInteractEvent.Action action = event.getAction();
        if (action == Action.LEFT_CLICK) {
            if (event.isAir() && ignoreLeftClickAir) {
                return;
            }

            if (!event.isAir()) {
                final Point clickedBlock = event.getInteractedPoint();
                final WorldVector pos = new WorldVector(world, clickedBlock.getX(),
                        clickedBlock.getY(), clickedBlock.getZ());


                if (we.handleBlockLeftClick(player, pos)) {
                    event.setCancelled(true);
                }
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

            if (!event.isAir() && !ignoreLeftClickAir) {
                final int taskId = Spout.getGame().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        ignoreLeftClickAir = false;
                    }
                }, 2);

                if (taskId != -1) {
                    ignoreLeftClickAir = true;
                }
            }
        } else if (action == Action.RIGHT_CLICK) {
            if (!event.isAir()) {
                final Point clickedBlock = event.getInteractedPoint();
                final WorldVector pos = new WorldVector(world, clickedBlock.getX(),
                        clickedBlock.getY(), clickedBlock.getZ());

                if (we.handleBlockRightClick(player, pos)) {
                    event.setCancelled(true);
                }
            }

            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Matcher matcher = cuipattern.matcher(event.getMessage());
        if (matcher.find()) {
            String type = matcher.group(1);
            String args = matcher.group(2);

            if( type.equals("v") ) {
                try {
                    plugin.getSession(event.getPlayer()).setCUIVersion(Integer.parseInt(args));
                    event.setCancelled(true);
                } catch( NumberFormatException e ) {
                }
            }

        }
    }
}
