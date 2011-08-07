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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

public class PermissionsResolverManager implements PermissionsResolver {
    private static final String CONFIG_HEADER = "#\r\n" +
            "# WEPIF Configuration File\r\n" +
            "#\r\n" +
            "# This file handles permissions configuration for every plugin using WEPIF\r\n" +
            "#\r\n" +
            "# About editing this file:\r\n" +
            "# - DO NOT USE TABS. You MUST use spaces or Bukkit will complain. If\r\n" +
            "#   you use an editor like Notepad++ (recommended for Windows users), you\r\n" +
            "#   must configure it to \"replace tabs with spaces.\" In Notepad++, this can\r\n" +
            "#   be changed in Settings > Preferences > Language Menu.\r\n" +
            "# - Don't get rid of the indents. They are indented so some entries are\r\n" +
            "#   in categories (like \"enforce-single-session\" is in the \"protection\"\r\n" +
            "#   category.\r\n" +
            "# - If you want to check the format of this file before putting it\r\n" +
            "#   into WEPIF, paste it into http://yaml-online-parser.appspot.com/\r\n" +
            "#   and see if it gives \"ERROR:\".\r\n" +
            "# - Lines starting with # are comments and so they are ignored.\r\n" +
            "#\r\n" +
            "# About Configuration Permissions\r\n" +
            "# - See http://wiki.sk89q.com/wiki/WorldEdit/Permissions/Bukkit\r\n" +
            "# - Now with multiworld support (see example)\r\n" +
            "\r\n";

    private Server server;
    private PermissionsResolver perms;
    private Configuration permsConfig;
    private String name;
    private Logger logger;
    protected boolean ignoreNijiPermsBridges;

    public PermissionsResolverManager(Configuration config, Server server, String name, Logger logger) {
        this.server = server;
        this.name = name;
        this.logger = logger;
        loadConfig(new File("wepif.yml")); // TODO: config migration, maybe
        findResolver();
    }
    public void findResolver() {
        if (tryPluginPermissionsResolver()) return;
        if (tryNijiPermissions()) return;
        if (tryDinnerPerms()) return;
        if (tryFlatFilePermissions()) return;
        
        perms = new ConfigurationPermissionsResolver(permsConfig);
        logger.info(name + ": No known permissions plugin detected. Using configuration file for permissions.");
    }
    
    private boolean tryNijiPermissions() {
        try {
            perms = new NijiPermissionsResolver(server, ignoreNijiPermsBridges);
            logger.info(name + ": Permissions plugin detected! Using Permissions plugin for permissions.");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
    
    private boolean tryFlatFilePermissions() {
        if (FlatFilePermissionsResolver.filesExists()) {
            perms = new FlatFilePermissionsResolver();
            logger.info(name + ": perms_groups.txt and perms_users.txt detected! Using flat file permissions.");
            return true;
        }
        
        return false;
    }
    
    private boolean tryPluginPermissionsResolver() {
        for (Plugin plugin : server.getPluginManager().getPlugins()) {
            if (plugin instanceof PermissionsProvider) {
                perms = new PluginPermissionsResolver(
                        (PermissionsProvider) plugin);
                logger.info(name + ": Using plugin '"
                        + plugin.getDescription().getName() + "' for permissions.");
                return true;
            }
        }
        
        return false;
    }

    private boolean tryDinnerPerms() {
        if (!permsConfig.getBoolean("dinnerperms", true))
            return false;
        perms = new DinnerPermsResolver(server);
        logger.info(name + ": Using the Bukkit Permissions API.");
        return true;
    }
    
    public void setPluginPermissionsResolver(Plugin plugin) {
        if (!(plugin instanceof PermissionsProvider)) {
            return;
        }
        
        perms = new PluginPermissionsResolver(
                (PermissionsProvider) plugin);
        logger.info(name + ": Using plugin '"
                + plugin.getDescription().getName() + "' for permissions.");
    }

    public void load() {
        perms.load();
    }

    public boolean hasPermission(String name, String permission) {
        return perms.hasPermission(name, permission);
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return perms.hasPermission(worldName, name, permission);
    }

    public boolean inGroup(String player, String group) {
        return perms.inGroup(player, group);
    }

    public String[] getGroups(String player) {
        return perms.getGroups(player);
    }

    private boolean loadConfig(File file) {
        boolean isUpdated = false;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        permsConfig = new Configuration(file);
        permsConfig.load();
        List<String> keys = permsConfig.getKeys();
            permsConfig.setHeader(CONFIG_HEADER);
        if (!keys.contains("dinnerperms")) {
            permsConfig.setProperty("dinnerperms", permsConfig.getBoolean("dinner-perms", true));
            isUpdated = true;
        }
        if (!keys.contains("ignore-nijiperms-bridges")) {
            permsConfig.setProperty("ignore-nijiperms-bridges", true);
            isUpdated = true;
        }
        ignoreNijiPermsBridges = permsConfig.getBoolean("ignore-nijiperms-bridges", true);
        if (keys.contains("dinner-perms")) {
            permsConfig.removeProperty("dinner-perms");
            isUpdated = true;
        }
        if (!keys.contains("permissions")) {
            ConfigurationPermissionsResolver.generateDefaultPerms(permsConfig);
            isUpdated = true;
        }
        if (isUpdated) {
            logger.info("WEPIF: Updated config file");
            permsConfig.save();
        }
        return isUpdated;
    }

}
