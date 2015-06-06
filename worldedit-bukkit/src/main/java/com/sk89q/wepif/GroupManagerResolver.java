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
import org.bukkit.permissions.Permissible;

public class GroupManagerResolver extends DinnerPermsResolver {
    private final WorldsHolder worldsHolder;

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
        super(server);
        this.worldsHolder = worldsHolder;
    }

    @Override
    public void load() {

    }

    /*
     * True if the string is null or empty
     */
    private boolean nameNotSafe(String perm) {
        return perm == null || perm.isEmpty();
    }

    private AnjoPermissionsHandler getPermissionHandler(World world) {
        if (world != null) {
            return worldsHolder.getWorldPermissions(world.getName());
        } else {
            return worldsHolder.getDefaultWorld().getPermissionsHandler();
        }
    }

    @Override
    public String[] getGroups(String name) {
        AnjoPermissionsHandler permissionHandler = getPermissionHandler(null);
        if (permissionHandler == null) {
            return new String[0];
        }
        return permissionHandler.getGroups(name);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        if (nameNotSafe(permission)) {
            return false;
        }

        Permissible permissible = getPermissible(player);
        if (permissible == null) {
            return getPermissionHandler(player.getPlayer().getWorld()).permission(player.getName(), permission);
        } else {
            return permissible.hasPermission(permission);
        }
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        if (nameNotSafe(permission)) {
            return false;
        }

        String name = player.getName();
        World world = worldName != null ? server.getWorld(worldName) : player.getPlayer().getWorld();

        AnjoPermissionsHandler permissionHandler = getPermissionHandler(world);
        return permissionHandler != null && permissionHandler.permission(name, permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        if (super.inGroup(player, group)) {
            return true;
        }

        if (nameNotSafe(group)) {
            return false;
        }

        AnjoPermissionsHandler permissionHandler = getPermissionHandler(null);
        return permissionHandler != null && permissionHandler.inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return getGroups(player.getName());
    }

    @Override
    public String getDetectionMessage() {
        return "GroupManager detected! Using GroupManager for permissions.";
    }
}
