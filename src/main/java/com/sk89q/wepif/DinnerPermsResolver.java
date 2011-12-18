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
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class DinnerPermsResolver implements PermissionsResolver {

    private static final String GROUP_PREFIX = "group.";
    private final Server server;

    public DinnerPermsResolver(Server server) {
        this.server = server;
    }

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        return new DinnerPermsResolver(server);
    }

    public void load() {
    }

    public boolean hasPermission(String name, String permission) {
        return hasPermission(server.getOfflinePlayer(name), permission);
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(worldName, server.getOfflinePlayer(name), permission);
    }

    public boolean inGroup(String name, String group) {
        return inGroup(server.getOfflinePlayer(name), group);
    }

    public String[] getGroups(String name) {
        return getGroups(server.getOfflinePlayer(name));
    }

    public boolean hasPermission(OfflinePlayer player, String permission) {
        Permissible perms = getPermissible(player);
        if (perms == null) {
            return false; // Permissions are only registered for objects with a Permissible
        }
        switch (internalHasPermission(perms, permission)) {
            case -1:
                return false;
            case 1:
                return true;
        }
        int dotPos = permission.lastIndexOf(".");
        while (dotPos > -1) {
            switch (internalHasPermission(perms, permission.substring(0, dotPos + 1) + "*")) {
                case -1:
                    return false;
                case 1:
                    return true;
            }
            dotPos = permission.lastIndexOf(".", dotPos - 1);
        }
        return internalHasPermission(perms, "*") == 1;
    }

    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(player, permission); // no per-world ability to check permissions in dinnerperms
    }

    public boolean inGroup(OfflinePlayer player, String group) {
        final Permissible perms = getPermissible(player);
        if (perms == null) {
            return false;
        }

        final String perm = GROUP_PREFIX + group;
        return perms.isPermissionSet(perm) && perms.hasPermission(perm);
    }

    public String[] getGroups(OfflinePlayer player) {
        Permissible perms = getPermissible(player);
        if (perms == null) {
            return new String[0];
        }
        List<String> groupNames = new ArrayList<String>();
        for (PermissionAttachmentInfo permAttach : perms.getEffectivePermissions()) {
            String perm = permAttach.getPermission();
            if (!(perm.startsWith(GROUP_PREFIX) && permAttach.getValue())) {
                continue;
            }
            groupNames.add(perm.substring(GROUP_PREFIX.length(), perm.length()));
        }
        return groupNames.toArray(new String[groupNames.size()]);
    }
    
    public Permissible getPermissible(OfflinePlayer offline) {
        if (offline == null) return null;
        Permissible perm = null;
        if (offline instanceof Permissible) {
            perm = (Permissible) offline;
        } else {
            Player player = offline.getPlayer();
            if (player != null) perm = player;
        }
        return perm;
    }

    /**
     * Checks the permission from dinnerperms
     * @param perms Permissible to check for
     * @param permission The permission to check
     * @return -1 if the permission is explicitly denied, 1 if the permission is allowed,
     *         0 if the permission is denied by a default.
     */
    public int internalHasPermission(Permissible perms, String permission) {
        if (perms.isPermissionSet(permission)) {
            return perms.hasPermission(permission) ? 1 : -1;
        } else {
            Permission perm = server.getPluginManager().getPermission(permission);
            if (perm != null) {
                return perm.getDefault().getValue(perms.isOp()) ? 1 : 0;
            } else {
                return 0;
            }
        }
    }

    public String getDetectionMessage() {
        return "Using the Bukkit Permissions API.";
    }
}
