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

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;

public class BukkitPlayer extends LocalPlayer {
    private Player player;
    private WorldEditPlugin plugin;

    public BukkitPlayer(WorldEditPlugin plugin, ServerInterface server, Player player) {
        super(server);
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public int getItemInHand() {
        ItemStack itemStack = player.getItemInHand();
        return itemStack != null ? itemStack.getTypeId() : 0;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Location loc = player.getLocation();
        return new WorldVector(new BukkitWorld(loc.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getLocation().getPitch();
    }

    @Override
    public double getYaw() {
        return player.getLocation().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        player.getInventory().addItem(new ItemStack(type, amt));
    }

    @Override
    public void printRaw(String msg) {
        player.sendMessage(msg);
    }

    @Override
    public void print(String msg) {
        player.sendMessage("\u00A7d" + msg);
    }

    @Override
    public void printDebug(String msg) {
        player.sendMessage("\u00A77" + msg);
    }

    @Override
    public void printError(String msg) {
        player.sendMessage("\u00A7c" + msg);
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.teleport(new Location(player.getWorld(), pos.getX(), pos.getY(),
                pos.getZ(), yaw, pitch));
    }

    @Override
    public String[] getGroups() {
        return plugin.getPermissionsResolver().getGroups(player.getName());
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new BukkitPlayerBlockBag(player);
    }

    @Override
    public boolean hasPermission(String perm) {
        return (!plugin.getLocalConfiguration().noOpPermissions && player.isOp())
                || plugin.getPermissionsResolver().hasPermission(
                player.getWorld().getName(), player.getName(), perm);
    }

    @Override
    public LocalWorld getWorld() {
        return new BukkitWorld(player.getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();

        if (params.length > 0) {
            player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75" + event.getTypeId()
                    + "|" + StringUtil.joinString(params, "|"));
        } else {
            player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75" + event.getTypeId());
        }
    }

    @Override
    public void dispatchCUIHandshake() {
        player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75");
    }

    @Override
    public void fireBlockEvent(Vector loc) {
        if (plugin.getLocalConfiguration().useBlockEvents) {
            Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            BlockBreakEvent event = new BlockBreakEvent(block, player);
            plugin.getServer().getPluginManager().callEvent(event);
        }
    }
}
