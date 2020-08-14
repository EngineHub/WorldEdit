/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PluginPermissionsResolver implements PermissionsResolver {

    protected PermissionsProvider resolver;
    protected Plugin plugin;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        // Looking for service
        RegisteredServiceProvider<PermissionsProvider> serviceProvider = server.getServicesManager().getRegistration(PermissionsProvider.class);

        if (serviceProvider != null) {
            return new PluginPermissionsResolver(serviceProvider.getProvider(), serviceProvider.getPlugin());
        }

        // Looking for plugin
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            if (plugin instanceof PermissionsProvider) {
                return new PluginPermissionsResolver((PermissionsProvider) plugin, plugin);
            }
        }

        return null;
    }

    public PluginPermissionsResolver(PermissionsProvider resolver, Plugin permissionsPlugin) {
        this.resolver = resolver;
        this.plugin = permissionsPlugin;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return resolver.hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return resolver.hasPermission(worldName, name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return resolver.inGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        return resolver.getGroups(player);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return resolver.hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return resolver.hasPermission(worldName, player, permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return resolver.inGroup(player, group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return resolver.getGroups(player);
    }

    @Override
    public String getDetectionMessage() {
        return "Using plugin '" + this.plugin.getDescription().getName() + "' for permissions.";
    }

}
