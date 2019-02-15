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

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.internal.cui.multi.CUIMultiRegion;
import com.sk89q.worldedit.internal.cui.multi.ConvexPolyhedralCUIMultiRegion;
import com.sk89q.worldedit.internal.cui.multi.CuboidCUIMultiRegion;
import com.sk89q.worldedit.internal.cui.multi.CylinderCUIMultiRegion;
import com.sk89q.worldedit.internal.cui.multi.EllipsoidCUIMultiRegion;
import com.sk89q.worldedit.internal.cui.multi.Polygonal2DCUIMultiRegion;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

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
        WorldEdit.getInstance().getSession(plugin.wrapPlayer(event.getPlayer()));
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

    private CUIMultiRegion lastRegion;
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final LocalSession session = plugin.getSession(event.getPlayer());
        if (!session.hasCUISupport() || session.getCUIVersion() < 4) return;
        final LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        Tool tool = session.getTool(event.getPlayer().getInventory().getItemInMainHand().getTypeId());
        if (!(tool instanceof BrushTool)) {
            if (lastRegion != null)
                lastRegion.clearRegion(session, player);
            return;
        }
        BrushTool brush = (BrushTool) tool;
        int range = brush.getRange();
        double size = brush.getSize();
        final WorldVector trace = player.getBlockTrace(range, true);
        Region bounds = brush.getBrush().getBounds(session.createEditSession(player), trace, size);
        CUIMultiRegion region = null;
        if (bounds instanceof CuboidRegion) {
            region = new CuboidCUIMultiRegion((CuboidRegion) bounds, null, 2, true);
        } else if (bounds instanceof CylinderRegion) {
            region = new CylinderCUIMultiRegion((CylinderRegion) bounds, null, 2, true);
        } else if (bounds instanceof EllipsoidRegion) {
            region = new EllipsoidCUIMultiRegion((EllipsoidRegion) bounds, null, 2, true);
        } else if (bounds instanceof Polygonal2DRegion) {
            region = new Polygonal2DCUIMultiRegion((Polygonal2DRegion) bounds, null, 2, true);
        } else if (bounds instanceof ConvexPolyhedralRegion) {
            region = new ConvexPolyhedralCUIMultiRegion((ConvexPolyhedralRegion) bounds, null, 2, true);
        }
        if (region == null) {
            region = new CuboidCUIMultiRegion(CuboidRegion.makeCuboid(bounds), null, 2, true);
        }

        if (lastRegion != null) {
            lastRegion.clearRegion(session, player);
        }
        region.describeCUI(session, player);
        lastRegion = region;
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
        } catch (NoSuchMethodError ignored) {
        } catch (NoSuchFieldError ignored) {
        }

        final LocalPlayer player = plugin.wrapPlayer(event.getPlayer());
        final World world = player.getWorld();
        final WorldEdit we = plugin.getWorldEdit();

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            final Block clickedBlock = event.getClickedBlock();
            final WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());

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
            final WorldVector pos = new WorldVector(LocalWorldAdapter.adapt(world), clickedBlock.getX(),
                    clickedBlock.getY(), clickedBlock.getZ());

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
