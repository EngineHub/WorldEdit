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

package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class bPermissionsResolver implements PermissionsResolver {

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        try {
            Class.forName("de.bananaco.bpermissions.api.ApiLayer");
        } catch (ClassNotFoundException e) {
            return null;
        }

        return new bPermissionsResolver(server);
    }
    
    private final Server server;

    public bPermissionsResolver(Server server) {
        this.server = server;
    }

    @Override
    public void load() {
    }

    @Override
    public String getDetectionMessage() {
        return "bPermissions detected! Using bPermissions for permissions";
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return hasPermission(server.getOfflinePlayer(name), permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return ApiLayer.hasPermission(worldName, CalculableType.USER, name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return inGroup(server.getOfflinePlayer(player), group);
    }

    @Override
    public String[] getGroups(String player) {
        return getGroups(server.getOfflinePlayer(player));
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return ApiLayer.hasPermission(null, CalculableType.USER, player.getName(), permission);
        } else {
            return ApiLayer.hasPermission(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName(), permission);
        }
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return ApiLayer.hasGroupRecursive(null, CalculableType.USER, player.getName(), group);
        } else {
            return ApiLayer.hasGroupRecursive(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName(), group);
        }
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return ApiLayer.getGroups(null, CalculableType.USER, player.getName());
        } else {
            return ApiLayer.getGroups(onlinePlayer.getWorld().getName(), CalculableType.USER, player.getName());
        }
    }

}
