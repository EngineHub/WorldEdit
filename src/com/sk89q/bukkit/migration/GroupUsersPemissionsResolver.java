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

import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import com.bukkit.authorblues.GroupUsers.GroupUsers;

public class GroupUsersPemissionsResolver implements PermissionsResolver {
    private Server server;
    private GroupUsers groupUsers;
    
    public void load() {
        
    }
    
    public GroupUsersPemissionsResolver(Server server)
            throws PluginAccessException, MissingPluginException {
        this.server = server;
        PluginManager manager = server.getPluginManager();
        
        Plugin plugin = manager.getPlugin("GroupUsers");
        if (plugin == null) {
            throw new MissingPluginException();
        }
        
        try {
            groupUsers = (GroupUsers)plugin;
        } catch (ClassCastException e) {
            throw new PluginAccessException();
        }
    }
    
    public boolean hasPermission(String name, String permission) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return false;
            int dotPos = permission.lastIndexOf(".");
            if (dotPos > -1) {
                if (hasPermission(name, permission.substring(0, dotPos))) {
                    return true;
                }
            }
            return groupUsers.playerCanUseCommand(player, permission);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public boolean inGroup(String name, String group) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return false;
            return groupUsers.isInGroup(player, group);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }
    
    public String[] getGroups(String name) {
        try {
            Player player = server.getPlayer(name);
            if (player == null) return new String[0];
            List<String> groups = groupUsers.getGroups();
            if (groups == null) return new String[0];
            return groups.toArray(new String[groups.size()]);
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
