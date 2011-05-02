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

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import com.nijikokun.bukkit.Permissions.Permissions;

public class NijiPermissionsResolver implements PermissionsResolver {
    private Server server;
    private Permissions api;
    
    public void load() {
        
    }
    
    public NijiPermissionsResolver(Server server)
            throws PluginAccessException, MissingPluginException {
        this.server = server;
        PluginManager manager = server.getPluginManager();
        
        Plugin plugin = manager.getPlugin("Permissions");
        if (plugin == null) {
            throw new MissingPluginException();
        }
        
        try {
            api = (Permissions)plugin;
        } catch (ClassCastException e) {
            throw new PluginAccessException();
        }
    }
    
    @SuppressWarnings("static-access")
    public boolean hasPermission(String name, String permission) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return false;
            try {
                return api.getHandler().has(player, permission);
            } catch (Throwable t) {
                return api.Security.permission(player, permission);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings({ "static-access" })
    public boolean inGroup(String name, String group) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return false;
            try {
                return api.getHandler().inGroup(player.getWorld().getName(), name, group);
            } catch (Throwable t) {
                return api.Security.inGroup(name, group);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings({ "static-access" })
    public String[] getGroups(String name) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return new String[0];
            String group;
            try {
                group = api.getHandler().getGroup(player.getWorld().getName(), player.getName());
            } catch (Throwable t) {
                group = api.Security.getGroup(player.getName());
            }
            if (group == null) {
                return new String[0];
            } else {
                return new String[]{ group };
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return new String[0];
        }
    }
    
    public static class PluginAccessException extends Exception {
        private static final long serialVersionUID = 7044832912491608706L;
    }
    
    public static class MissingPluginException extends Exception {
        private static final long serialVersionUID = 7044832912491608706L;
    }
}
