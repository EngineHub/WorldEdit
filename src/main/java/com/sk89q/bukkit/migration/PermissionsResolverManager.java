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

package com.sk89q.bukkit.migration;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

@Deprecated
public class PermissionsResolverManager implements PermissionsProvider {
    private static boolean setUp;

    @Deprecated
    public PermissionsResolverManager(org.bukkit.util.config.Configuration config, Server server, String name, Logger logger) {}

    @Deprecated
    public PermissionsResolverManager(Plugin plugin, String name, Logger logger) {
        if (!setUp) {
            setUp(plugin);
        }
    }

    @Deprecated
    public void findResolver() {
        getRealResolver().findResolver();
    }

    @Deprecated
    public void setPluginPermissionsResolver(Plugin plugin) {
        getRealResolver().setPluginPermissionsResolver(plugin);
    }

    @Deprecated
    public void load() {
        getRealResolver().load();
    }

    public boolean hasPermission(String name, String permission) {
        return getRealResolver().hasPermission(name, permission);
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return getRealResolver().hasPermission(worldName, name, permission);
    }

    public boolean inGroup(String player, String group) {
        return getRealResolver().inGroup(player, group);
    }

    public String[] getGroups(String player) {
        return getRealResolver().getGroups(player);
    }

    public String getDetectionMessage() {
        return getRealResolver().getDetectionMessage();
    }

    boolean isSetUp() {
        return setUp;
    }
    
    void setUp(Plugin plugin) {
        com.sk89q.wepif.PermissionsResolverManager.initialize(plugin);
        setUp = true;
    }

    public com.sk89q.wepif.PermissionsResolverManager getRealResolver() {
        return com.sk89q.wepif.PermissionsResolverManager.getInstance();
    }

}
