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
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultResolver implements PermissionsResolver {

    private static Permission perms = null;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        if (server.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Permission> rsp = server.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return null;
        }
        perms = rsp.getProvider();
        if (perms == null) {
            return null;
        }

        return new VaultResolver(server);
    }

    private final Server server;

    public VaultResolver(Server server) {
        this.server = server;
    }

    @Override
    public void load() {
    }

    @Override
    public String getDetectionMessage() {
        return "Vault detected! Using Vault for permissions";
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return hasPermission(server.getOfflinePlayer(name), permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(worldName, server.getOfflinePlayer(name), permission);
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
            return perms.playerHas(null, player, permission);
        } else {
            return perms.playerHas(onlinePlayer.getWorld().getName(), player, permission);
        }
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return perms.playerHas(worldName, player, permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return perms.playerInGroup(null, player, group);
        } else {
            return perms.playerInGroup(onlinePlayer, group);
        }
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return perms.getPlayerGroups(null, player);
        } else {
            return perms.getPlayerGroups(onlinePlayer);
        }
    }

}
