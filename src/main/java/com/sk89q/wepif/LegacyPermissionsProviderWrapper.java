// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public class LegacyPermissionsProviderWrapper implements PermissionsProvider {
    private final com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider;

    static PermissionsProvider wrap(Plugin plugin) {
        if (!(plugin instanceof com.sk89q.bukkit.migration.PermissionsProvider)) {
            return null;
        }

        final com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider = (com.sk89q.bukkit.migration.PermissionsProvider) plugin;
        return new LegacyPermissionsProviderWrapper(legacyPermissionsProvider);
    }

    private LegacyPermissionsProviderWrapper(com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider) {
        this.legacyPermissionsProvider = legacyPermissionsProvider;
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return legacyPermissionsProvider.hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return legacyPermissionsProvider.hasPermission(worldName, name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return legacyPermissionsProvider.inGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        return legacyPermissionsProvider.getGroups(player);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return legacyPermissionsProvider.hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return legacyPermissionsProvider.hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return legacyPermissionsProvider.inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return legacyPermissionsProvider.getGroups(player.getName());
    }
}
