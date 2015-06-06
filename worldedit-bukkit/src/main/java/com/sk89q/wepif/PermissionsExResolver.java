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
import org.bukkit.permissions.Permissible;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

public class PermissionsExResolver extends DinnerPermsResolver {
    private final PermissionManager manager;

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
        super(server);
        this.manager = manager;
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return manager.has(name, permission, worldName);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        Permissible permissible = getPermissible(player);
        if (permissible == null) {
            return manager.has(player.getUniqueId(), permission, null);
        } else {
            return permissible.hasPermission(permission);
        }
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return manager.has(player.getUniqueId(), permission, worldName);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return super.inGroup(player, group) || manager.getUser(player.getUniqueId()).inGroup(group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        if (getPermissible(player) == null) {
            PermissionUser user = manager.getUser(player.getUniqueId());
            if (user == null) {
                return new String[0];
            }
            return user.getGroupsNames();
        } else {
            return super.getGroups(player);
        }
    }

    @Override
    public String getDetectionMessage() {
        return "PermissionsEx detected! Using PermissionsEx for permissions.";
    }
}
