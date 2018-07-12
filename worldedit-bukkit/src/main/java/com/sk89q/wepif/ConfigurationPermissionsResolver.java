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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sk89q.util.yaml.YAMLNode;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;

public class ConfigurationPermissionsResolver implements PermissionsResolver {
    private YAMLProcessor config;
    private Map<String, Set<String>> userPermissionsCache;
    private Set<String> defaultPermissionsCache;
    private Map<String, Set<String>> userGroups;

    public ConfigurationPermissionsResolver(YAMLProcessor config) {
        this.config = config;
    }

    public static YAMLNode generateDefaultPerms(YAMLNode section) {
        section.setProperty("groups.default.permissions", new String[] {
                "worldedit.reload",
                "worldedit.selection",
                "worlds.creative.worldedit.region"});
        section.setProperty("groups.admins.permissions", new String[] { "*" });
        section.setProperty("users.sk89q.permissions", new String[] { "worldedit" });
        section.setProperty("users.sk89q.groups", new String[] { "admins" });
        return section;
    }

    @Override
    public void load() {
        userGroups = new HashMap<>();
        userPermissionsCache = new HashMap<>();
        defaultPermissionsCache = new HashSet<>();

        Map<String, Set<String>> userGroupPermissions = new HashMap<>();

        List<String> groupKeys = config.getStringList("permissions.groups", null);

        if (groupKeys != null) {
            for (String key : groupKeys) {
                List<String> permissions =
                        config.getStringList("permissions.groups." + key + ".permissions", null);

                if (!permissions.isEmpty()) {
                    Set<String> groupPerms = new HashSet<>(permissions);
                    userGroupPermissions.put(key, groupPerms);

                    if (key.equals("default")) {
                        defaultPermissionsCache.addAll(permissions);
                    }
                }
            }
        }

        List<String> userKeys = config.getStringList("permissions.users", null);

        if (userKeys != null) {
            for (String key : userKeys) {
                Set<String> permsCache = new HashSet<>();

                List<String> permissions =
                        config.getStringList("permissions.users." + key + ".permissions", null);

                if (!permissions.isEmpty()) {
                    permsCache.addAll(permissions);
                }

                List<String> groups =
                        config.getStringList("permissions.users." + key + ".groups", null);
                groups.add("default");

                if (!groups.isEmpty()) {
                    for (String group : groups) {
                        Set<String> groupPerms = userGroupPermissions.get(group);
                        if (groupPerms != null) {
                            permsCache.addAll(groupPerms);
                        }
                    }
                }

                userPermissionsCache.put(key.toLowerCase(), permsCache);
                userGroups.put(key.toLowerCase(), new HashSet<>(groups));
            }
        }
    }

    @Override
    public boolean hasPermission(String player, String permission) {
        int dotPos = permission.lastIndexOf(".");
        if (dotPos > -1) {
            if (hasPermission(player, permission.substring(0, dotPos))) {
                return true;
            }
        }

        Set<String> perms = userPermissionsCache.get(player.toLowerCase());
        if (perms == null) {
            return defaultPermissionsCache.contains(permission)
                    || defaultPermissionsCache.contains("*");
        }

        return perms.contains("*") || perms.contains(permission);
    }

    @Override
    public boolean hasPermission(String worldName, String player, String permission) {
        return hasPermission(player, "worlds." + worldName + "." + permission)
                || hasPermission(player, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        Set<String> groups = userGroups.get(player.toLowerCase());
        if (groups == null) {
            return false;
        }

        return groups.contains(group);
    }

    @Override
    public String[] getGroups(String player) {
        Set<String> groups = userGroups.get(player.toLowerCase());
        if (groups == null) {
            return new String[0];
        }

        return groups.toArray(new String[groups.size()]);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return getGroups(player.getName());
    }

    @Override
    public String getDetectionMessage() {
        return "No known permissions plugin detected. Using configuration file for permissions.";
    }

}
