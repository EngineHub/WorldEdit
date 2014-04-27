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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.util.Vectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;

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

    public BaseBlock getBlockInHand() throws WorldEditException {
        ItemStack itemStack = player.getItemInHand();
        return BukkitUtil.toBlock(getWorld(), itemStack);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Location loc = player.getLocation();
        return new WorldVector(BukkitUtil.getLocalWorld(loc.getWorld()),
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
        for (String part : msg.split("\n")) {
            player.sendMessage(part);
        }
    }

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A7d" + part);
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A77" + part);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A7c" + part);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.teleport(new Location(player.getWorld(), pos.getX(), pos.getY(),
                pos.getZ(), yaw, pitch));
    }

    @Override
    public String[] getGroups() {
        return plugin.getPermissionsResolver().getGroups(player);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new BukkitPlayerBlockBag(player);
    }

    @Override
    public boolean hasPermission(String perm) {
        return (!plugin.getLocalConfiguration().noOpPermissions && player.isOp())
                || plugin.getPermissionsResolver().hasPermission(
                        player.getWorld().getName(), player, perm);
    }

    @Override
    public LocalWorld getWorld() {
        return BukkitUtil.getLocalWorld(player.getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        player.sendPluginMessage(plugin, WorldEditPlugin.CUI_PLUGIN_CHANNEL, send.getBytes(CUIChannelListener.UTF_8_CHARSET));
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean hasCreativeMode() {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public void floatAt(int x, int y, int z, boolean alwaysGlass) {
        if (alwaysGlass || !player.getAllowFlight()) {
            super.floatAt(x, y, z, alwaysGlass);
            return;
        }

        setPosition(new Vector(x + 0.5, y, z + 0.5));
        player.setFlying(true);
    }

    @Override
    public com.sk89q.worldedit.util.Location getLocation() {
        Location nativeLocation = player.getLocation();
        Vector position = BukkitUtil.toVector(nativeLocation);
        Vector direction = Vectors.fromEulerDeg(nativeLocation.getYaw(), nativeLocation.getPitch());
        return new com.sk89q.worldedit.util.Location(getWorld(), position, direction);
    }
}
