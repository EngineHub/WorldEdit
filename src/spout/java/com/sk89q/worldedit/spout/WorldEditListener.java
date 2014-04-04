/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import org.spout.api.Spout;
import org.spout.api.chat.ChatArguments;
import org.spout.api.chat.ChatSection;
import org.spout.api.event.EventHandler;
import org.spout.api.event.Listener;
import org.spout.api.event.Order;
import org.spout.api.event.player.PlayerInteractEvent;
import org.spout.api.event.player.PlayerInteractEvent.Action;
import org.spout.api.event.player.PlayerLeaveEvent;
import org.spout.api.event.server.PreCommandEvent;
import org.spout.api.event.world.WorldLoadEvent;
import org.spout.api.generator.biome.BiomeGenerator;
import org.spout.api.geo.discrete.Point;
import org.spout.api.scheduler.TaskPriority;

import java.util.Arrays;
import java.util.List;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditListener implements Listener {
    /**
     * Plugin.
     */
    private WorldEditPlugin plugin;

    private boolean ignoreLeftClickAir = false;

    /**
     * Construct the object;
     *
     * @param plugin
     */
    public WorldEditListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
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
    @EventHandler(order = Order.EARLY)
    public void onPlayerCommandPreprocess(PreCommandEvent event) {

        if (event.getCommand().startsWith("nowe:")) {
            event.setCommand(event.getCommand().substring(5));
            return;
        }

        List<ChatSection> args = event.getArguments().toSections(ChatSection.SplitType.WORD);
        if (args.size() > 0) {
            String[] split = new String[args.size() + 1];
            split[0] = "/" + event.getCommand();
            for (int i = 0; i < args.size(); ++i) {
                split[i + 1] = args.get(i).getPlainString();
            }

            String[] newSplit = plugin.getWorldEdit().commandDetection(split);
            if (!Arrays.equals(split, newSplit)) {
                event.setCommand(newSplit[0]);
                ChatArguments newArgs = new ChatArguments();
                for (int i = 1; i < newSplit.length; ++i) {
                    newArgs.append(newSplit[i]);
                }
                event.setArguments(newArgs);
            }
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
                final WorldVector pos = new WorldVector(world, clickedBlock.getBlockX(),
                        clickedBlock.getBlockY(), clickedBlock.getBlockZ());


                if (we.handleBlockLeftClick(player, pos)) {
                    event.setCancelled(true);
                }
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

            if (!event.isAir() && !ignoreLeftClickAir) {
                final int taskId = Spout.getEngine().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        ignoreLeftClickAir = false;
                    }
                }, 100, TaskPriority.NORMAL).getTaskId();

                if (taskId != -1) {
                    ignoreLeftClickAir = true;
                }
            }
        } else if (action == Action.RIGHT_CLICK) {
            if (!event.isAir()) {
                final Point clickedBlock = event.getInteractedPoint();
                final WorldVector pos = new WorldVector(world, clickedBlock.getBlockX(),
                        clickedBlock.getBlockY(), clickedBlock.getBlockZ());

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
    public void onWorldLoad(WorldLoadEvent event) {
        if (event.getWorld().getGenerator() instanceof BiomeGenerator) {
            plugin.getServerInterface().getBiomes().registerBiomeTypes((BiomeGenerator) event.getWorld().getGenerator());
        }
    }
}
