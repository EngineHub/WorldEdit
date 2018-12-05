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

// $Id$

package com.sk89q.worldedit.bukkit;

import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all events thrown in relation to a Player
 */
public class WorldEditListener implements Listener {

    private WorldEditPlugin plugin;

    /**
     * Called when a player plays an animation, such as an arm swing
     *
     * @param event Relevant event details
     */

    /**
     * Construct the object;
     *
     * @param plugin the plugin
     */
    public WorldEditListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamemode(PlayerGameModeChangeEvent event) {
        if (!plugin.getInternalPlatform().isHookingEvents()) {
            return;
        }

        // this will automatically refresh their session, we don't have to do anything
        WorldEdit.getInstance().getSessionManager().get(plugin.wrapPlayer(event.getPlayer()));
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] split = event.getMessage().split(" ");

        if (split.length > 0) {
            split[0] = split[0].substring(1);
            split = plugin.getWorldEdit().getPlatformManager().getCommandManager().commandDetection(split);
        }

        final String newMessage = "/" + StringUtil.joinString(split, " ");

        if (!newMessage.equals(event.getMessage())) {
            event.setMessage(newMessage);
            plugin.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (!event.getMessage().isEmpty()) {
                    plugin.getServer().dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandSendEvent event) {
        CommandLocals locals = new CommandLocals();
        locals.put(Actor.class, plugin.wrapCommandSender(event.getPlayer()));
        Set<String> toRemove = plugin.getWorldEdit().getPlatformManager().getCommandManager().getDispatcher().getCommands().stream()
                .filter(commandMapping -> !commandMapping.getCallable().testPermission(locals))
                .map(CommandMapping::getPrimaryAlias)
                .collect(Collectors.toSet());
        event.getCommands().removeIf(toRemove::contains);
    }

    /**
     * Called when a player interacts
     *
     * @param event Relevant event details
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getInternalPlatform().isHookingEvents()) {
            return;
        }

        if (event.useItemInHand() == Result.DENY) {
            return;
        }

        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return; // TODO api needs to be able to get either hand depending on event
                // for now just ignore all off hand interacts
            }
        } catch (NoSuchMethodError | NoSuchFieldError ignored) {
        }

        final Player player = plugin.wrapPlayer(event.getPlayer());
        final World world = player.getWorld();
        final WorldEdit we = plugin.getWorldEdit();

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            final Block clickedBlock = event.getClickedBlock();
            final Location pos = new Location(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());

            if (we.handleBlockLeftClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }

        } else if (action == Action.LEFT_CLICK_AIR) {

            if (we.handleArmSwing(player)) {
                event.setCancelled(true);
            }


        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            final Block clickedBlock = event.getClickedBlock();
            final Location pos = new Location(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());

            if (we.handleBlockRightClick(player, pos)) {
                event.setCancelled(true);
            }

            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        } else if (action == Action.RIGHT_CLICK_AIR) {
            if (we.handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }
}
