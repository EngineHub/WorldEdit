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
import org.bukkit.util.config.Configuration;
import com.sk89q.bukkit.migration.GroupUsersPemissionsResolver.MissingPluginException;
import com.sk89q.bukkit.migration.GroupUsersPemissionsResolver.PluginAccessException;

public class PermissionsResolverManager implements PermissionsResolver {
    private Configuration config;
    private Server server;
    private PermissionsResolver perms;
    private String name;
    private Logger logger;
    
    public PermissionsResolverManager(Configuration config, Server server, String name, Logger logger) {
        this.config = config;
        this.server = server;
        this.name = name;
        this.logger = logger;
        
        findResolver();
    }
    
    public void findResolver() {
        try {
            perms = new GroupUsersPemissionsResolver(server);
            logger.info(name + ": GroupUsers detected! Using GroupUsers for permissions.");
        } catch (PluginAccessException e) {
            perms = new ConfigurationPermissionsResolver(config);
            logger.warning(name + ": Failed to access GroupUsers. Falling back to configuration file for permissions.");
        } catch (NoClassDefFoundError e) {
            perms = new ConfigurationPermissionsResolver(config);
            logger.info(name + ": No known permissions plugin detected. Using configuration file for permissions. (code: 10)");
        } catch (MissingPluginException e) {
            perms = new ConfigurationPermissionsResolver(config);
            logger.info(name + ": No known permissions plugin detected. Using configuration file for permissions. (code: 11)");
        } catch (Throwable e) {
            perms = new ConfigurationPermissionsResolver(config);
            logger.info(name + ": No known permissions plugin detected. Using configuration file for permissions. (code: 12)");
        }
    }

    public void load() {
        perms.load();
    }

    public boolean hasPermission(String name, String permission) {
        return perms.hasPermission(name, permission);
    }

    public boolean inGroup(String player, String group) {
        return perms.inGroup(player, group);
    }

    public String[] getGroups(String player) {
        return perms.getGroups(player);
    }

}
