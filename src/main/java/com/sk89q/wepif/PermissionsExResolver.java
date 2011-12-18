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

package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

public class PermissionsExResolver implements PermissionsResolver {
    private final PermissionManager manager;
    private final Server server;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        try {
            PermissionManager manager = server.getServicesManager().load(PermissionManager.class);

            if (manager == null) {
                return null;
            }

            return new PermissionsExResolver(server, manager);
        } catch (Throwable t) {
            return null;
        }
    }

    public PermissionsExResolver(Server server, PermissionManager manager) {
        this.server = server;
        this.manager = manager;
    }

    public void load() {

    }

    public boolean hasPermission(String name, String permission) {
        Player player = server.getPlayerExact(name);
        return manager.has(name, permission, player == null ? null : player.getWorld().getName());
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return manager.has(name, permission, worldName);
    }

    public boolean inGroup(String player, String group) {
        PermissionUser user = manager.getUser(player);
        if (user == null) {
            return false;
        }
        return user.inGroup(group);
    }

    public String[] getGroups(String player) {
        PermissionUser user = manager.getUser(player);
        if (user == null) {
            return new String[0];
        }
        return user.getGroupsNames();
    }

    public boolean hasPermission(OfflinePlayer player, String permission) {
        Player onlinePlayer = player.getPlayer();
        return manager.has(player.getName(), permission, onlinePlayer == null ? null : onlinePlayer.getWorld().getName());
    }

    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(worldName, player.getName(), permission);
    }

    public boolean inGroup(OfflinePlayer player, String group) {
        return inGroup(player.getName(), group);
    }

    public String[] getGroups(OfflinePlayer player) {
        return getGroups(player.getName());
    }

    public String getDetectionMessage() {
        return "PermissionsEx detected! Using PermissionsEx for permissions.";
    }
}
