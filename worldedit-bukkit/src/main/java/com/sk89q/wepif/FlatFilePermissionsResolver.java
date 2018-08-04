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
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatFilePermissionsResolver implements PermissionsResolver {

    private static final Logger log = Logger.getLogger(FlatFilePermissionsResolver.class.getCanonicalName());

    private Map<String, Set<String>> userPermissionsCache;
    private Set<String> defaultPermissionsCache;
    private Map<String, Set<String>> userGroups;

    private final File groupFile;
    private final File userFile;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        File groups = new File("perms_groups.txt");
        File users = new File("perms_users.txt");

        if (!groups.exists() || !users.exists()) {
            return null;
        }

        return new FlatFilePermissionsResolver(groups, users);
    }

    public FlatFilePermissionsResolver() {
        this(new File("perms_groups.txt"), new File("perms_users.txt"));
    }

    public FlatFilePermissionsResolver(File groupFile, File userFile) {
        this.groupFile = groupFile;
        this.userFile = userFile;
    }

    public Map<String, Set<String>> loadGroupPermissions() {
        Map<String, Set<String>> userGroupPermissions = new HashMap<>();

        BufferedReader buff = null;

        try {
            FileReader input = new FileReader(this.groupFile);
            buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                line = line.trim();

                // Blank line
                if (line.isEmpty()) {
                    continue;
                } else if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }

                String[] parts = line.split(":");

                String key = parts[0];

                if (parts.length > 1) {
                    String[] perms = parts[1].split(",");

                    Set<String> groupPerms = new HashSet<>(Arrays.asList(perms));
                    userGroupPermissions.put(key, groupPerms);
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load permissions", e);
        } finally {
            try {
                if (buff != null) {
                    buff.close();
                }
            } catch (IOException ignored) {
            }
        }

        return userGroupPermissions;
    }

    @Override
    public void load() {
        userGroups = new HashMap<>();
        userPermissionsCache = new HashMap<>();
        defaultPermissionsCache = new HashSet<>();

        Map<String, Set<String>> userGroupPermissions = loadGroupPermissions();

        if (userGroupPermissions.containsKey("default")) {
            defaultPermissionsCache = userGroupPermissions.get("default");
        }

        BufferedReader buff =  null;

        try {
            FileReader input = new FileReader(this.userFile);
            buff = new BufferedReader(input);

            String line;
            while ((line = buff.readLine()) != null) {
                Set<String> permsCache = new HashSet<>();

                line = line.trim();

                // Blank line
                if (line.isEmpty()) {
                    continue;
                } else if (line.charAt(0) == ';' || line.charAt(0) == '#') {
                    continue;
                }

                String[] parts = line.split(":");

                String key = parts[0];

                if (parts.length > 1) {
                    String[] groups = (parts[1] + ",default").split(",");
                    String[] perms = parts.length > 2 ? parts[2].split(",") : new String[0];

                    permsCache.addAll(Arrays.asList(perms));

                    for (String group : groups) {
                        Set<String> groupPerms = userGroupPermissions.get(group);
                        if (groupPerms != null) {
                            permsCache.addAll(groupPerms);
                        }
                    }

                    userPermissionsCache.put(key.toLowerCase(), permsCache);
                    userGroups.put(key.toLowerCase(), new HashSet<>(Arrays.asList(groups)));
                }
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load permissions", e);
        } finally {
            try {
                if (buff != null) {
                    buff.close();
                }
            } catch (IOException ignored) {
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
        return groups != null && groups.contains(group);
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
        return "perms_groups.txt and perms_users.txt detected! Using flat file permissions.";
    }

}
