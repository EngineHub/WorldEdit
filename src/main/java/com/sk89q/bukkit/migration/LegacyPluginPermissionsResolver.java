// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.bukkit.migration;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

@Deprecated
public class LegacyPluginPermissionsResolver implements com.sk89q.wepif.PermissionsResolver {

    protected PermissionsProvider resolver;
    protected Plugin plugin;

    public static com.sk89q.wepif.PermissionsResolver factory(Server server, YAMLProcessor config) {
        // Looking for service
        RegisteredServiceProvider<PermissionsProvider> serviceProvider = server.getServicesManager().getRegistration(PermissionsProvider.class);

        if (serviceProvider != null) {
            return new LegacyPluginPermissionsResolver(serviceProvider.getProvider(), serviceProvider.getPlugin());
        }

        // Looking for plugin
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            if (plugin instanceof PermissionsProvider) {
                return new LegacyPluginPermissionsResolver((PermissionsProvider) plugin, plugin);
            }
        }

        return null;
    }

    public LegacyPluginPermissionsResolver(PermissionsProvider resolver, Plugin permissionsPlugin) {
        this.resolver = resolver;
        this.plugin = permissionsPlugin;
    }

    public void load() {
    }

    public boolean hasPermission(String name, String permission) {
        return resolver.hasPermission(name, permission);
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return resolver.hasPermission(worldName, name, permission);
    }

    public boolean inGroup(String player, String group) {
        return resolver.inGroup(player, group);
    }

    public String[] getGroups(String player) {
        return resolver.getGroups(player);
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
        String author = "the author";
        List<String> authors = plugin.getDescription().getAuthors();
        if (authors != null && authors.size() > 0) {
            author = authors.get(0);
        }
        return "Using legacy plugin '" + this.plugin.getDescription().getName() + "' for permissions. Bug " + author + " to update it! ";
    }

}
