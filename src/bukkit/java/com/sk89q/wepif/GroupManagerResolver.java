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
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GroupManagerResolver implements PermissionsResolver {
    private final WorldsHolder worldsHolder;
    private final Server server;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        try {
            WorldsHolder worldsHolder = server.getServicesManager().load(WorldsHolder.class);

            if (worldsHolder == null) {
                return null;
            }

            return new GroupManagerResolver(server, worldsHolder);
        } catch (Throwable t) {
            return null;
        }
    }

    public GroupManagerResolver(Server server, WorldsHolder worldsHolder) {
        this.server = server;
        this.worldsHolder = worldsHolder;
    }

    public void load() {

    }

    private AnjoPermissionsHandler getPermissionHandler(String name, String worldName) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (worldName == null || worldName.isEmpty()) {
            Player player = server.getPlayerExact(name);
            if (player == null) {
                return null;
            }
            World world = player.getWorld();
            if (world == null) {
                return worldsHolder.getDefaultWorld().getPermissionsHandler();
            }
            return worldsHolder.getWorldPermissions(world.getName());
        } else {
            return worldsHolder.getWorldPermissions(worldName);
        }
    }

    public boolean hasPermission(String name, String permission) {
        return hasPermission(null, name, permission);
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        AnjoPermissionsHandler permissionHandler = getPermissionHandler(name, worldName);
        if (permissionHandler == null) {
            return false;
        }
        return permissionHandler.permission(name, permission);
    }

    public boolean inGroup(String name, String group) {
        if (group == null || group.isEmpty()) {
            return false;
        }
        AnjoPermissionsHandler permissionHandler = getPermissionHandler(name, null);
        if (permissionHandler == null) {
            return false;
        }
        return permissionHandler.inGroup(name, group);
    }

    public String[] getGroups(String name) {
        AnjoPermissionsHandler permissionHandler = getPermissionHandler(name, null);
        if (permissionHandler == null) {
            return new String[0];
        }
        return permissionHandler.getGroups(name);
    }

    public boolean hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(player.getName(), permission);
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
        return "GroupManager detected! Using GroupManager for permissions.";
    }
}
