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

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

public class PermissionsResolverServerListener extends ServerListener {
    private PermissionsResolverManager manager;
    
    public PermissionsResolverServerListener(PermissionsResolverManager manager) {
        this.manager = manager;
    }
    
    /**
     * Called when a plugin is enabled
     *
     * @param event Relevant event details
     */
    public void onPluginEnabled(PluginEvent event) {
        String name = event.getPlugin().getDescription().getName();
        
        if (name.equalsIgnoreCase("GroupUsers") || name.equalsIgnoreCase("Permissions")) {
            manager.findResolver();
        }
    }

    /**
     * Called when a plugin is disabled
     *
     * @param event Relevant event details
     */
    public void onPluginDisabled(PluginEvent event) {
        String name = event.getPlugin().getDescription().getName();
        
        if (name.equalsIgnoreCase("GroupUsers") || name.equalsIgnoreCase("Permissions")) {
            manager.findResolver();
        }
    }
    
    public void register(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE,
                this, Priority.Normal, plugin);
        plugin.getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE,
                this, Priority.Normal, plugin);
    }
}
