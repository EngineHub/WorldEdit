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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitCommandSender extends LocalPlayer {
    private CommandSender sender;
    private WorldEditPlugin plugin;

    public BukkitCommandSender(WorldEditPlugin plugin, ServerInterface server, CommandSender sender) {
        super(server);
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage(part);
        }
    }

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("\u00A7d" + part);
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("\u00A77" + part);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage("\u00A7c" + part);
        }
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        if (!plugin.getLocalConfiguration().noOpPermissions && sender.isOp()) {
            return true;
        }

        return plugin.getPermissionsResolver().hasPermission(null, sender.getName(), perm);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public int getItemInHand() {
        throw new PlayerNeededException();
    }

    @Override
    public Location getLocation() {
        throw new PlayerNeededException();
    }

    @Override
    public WorldVector getPosition() {
        throw new PlayerNeededException();
    }

    @Override
    public LocalWorld getWorld() {
        throw new PlayerNeededException();
    }

    @Override
    public double getPitch() {
        throw new PlayerNeededException();
    }

    @Override
    public double getYaw() {
        throw new PlayerNeededException();
    }

    @Override
    public void giveItem(int type, int amt) {
        throw new PlayerNeededException();
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        throw new PlayerNeededException();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        throw new PlayerNeededException();
    }
}
