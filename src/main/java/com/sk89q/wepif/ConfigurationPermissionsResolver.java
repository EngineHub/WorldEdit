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

import java.util.*;

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

    public void load() {
        userGroups = new HashMap<String, Set<String>>();
        userPermissionsCache = new HashMap<String, Set<String>>();
        defaultPermissionsCache = new HashSet<String>();

        Map<String, Set<String>> userGroupPermissions = new HashMap<String, Set<String>>();

        List<String> groupKeys = config.getStringList("permissions.groups", null);

        if (groupKeys != null) {
            for (String key : groupKeys) {
                List<String> permissions =
                        config.getStringList("permissions.groups." + key + ".permissions", null);

                if (permissions.size() > 0) {
                    Set<String> groupPerms = new HashSet<String>(permissions);
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
                Set<String> permsCache = new HashSet<String>();

                List<String> permissions =
                        config.getStringList("permissions.users." + key + ".permissions", null);

                if (permissions.size() > 0) {
                    permsCache.addAll(permissions);
                }

                List<String> groups =
                        config.getStringList("permissions.users." + key + ".groups", null);
                groups.add("default");

                if (groups.size() > 0) {
                    for (String group : groups) {
                        Set<String> groupPerms = userGroupPermissions.get(group);
                        if (groupPerms != null) {
                            permsCache.addAll(groupPerms);
                        }
                    }
                }

                userPermissionsCache.put(key.toLowerCase(), permsCache);
                userGroups.put(key.toLowerCase(), new HashSet<String>(groups));
            }
        }
    }

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

    public boolean hasPermission(String worldName, String player, String permission) {
        return hasPermission(player, "worlds." + worldName + "." + permission)
                || hasPermission(player, permission);
    }

    public boolean inGroup(String player, String group) {
        Set<String> groups = userGroups.get(player.toLowerCase());
        if (groups == null) {
            return false;
        }

        return groups.contains(group);
    }

    public String[] getGroups(String player) {
        Set<String> groups = userGroups.get(player.toLowerCase());
        if (groups == null) {
            return new String[0];
        }

        return groups.toArray(new String[groups.size()]);
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
        return "No known permissions plugin detected. Using configuration file for permissions.";
    }

}
