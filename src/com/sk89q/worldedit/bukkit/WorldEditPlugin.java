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

package com.sk89q.worldedit.bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.migration.ConfigurationPermissionsResolver;
import com.sk89q.worldedit.*;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89qs
 */
public class WorldEditPlugin extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    public final ServerInterface server;
    public final WorldEditController controller;
    public final WorldEditAPI api;
    
    private final LocalConfiguration config;
    private final WorldEditPlayerListener playerListener =
        new WorldEditPlayerListener(this);
    private final WorldEditBlockListener blockListener =
        new WorldEditBlockListener(this);
    private final ConfigurationPermissionsResolver perms;

    public WorldEditPlugin(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        logger.info("WorldEdit " + desc.getVersion() + " loaded.");
        
        folder.mkdirs();

        createDefaultConfiguration("config.yml");
        
        config = new BukkitConfiguration(getConfiguration(), logger);
        perms = new ConfigurationPermissionsResolver(getConfiguration());
        loadConfiguration();
        
        server = new BukkitServerInterface(getServer());
        controller = new WorldEditController(server, config);
        api = new WorldEditAPI(this);

        registerEvents();
    }

    public void onEnable() {
    }

    public void onDisable() {
        controller.clearSessions();
    }

    private void registerEvents() {        
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED,
                blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED,
                blockListener, Priority.Normal, this);
    }
    
    private void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            
            InputStream input =
                    WorldEditPlugin.class.getResourceAsStream("/defaults/" + name);
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }
                    
                    logger.info("WorldEdit: Default configuration file written: "
                            + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {}

                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {}
                }
            }
        }
    }
    
    public void loadConfiguration() {
        getConfiguration().load();
        config.load();
        perms.load();
    }
    
    String[] getGroups(Player player) {
        return perms.getGroups(player.getName());
    }
    
    boolean inGroup(Player player, String group) {
        return perms.inGroup(player.getName(), group);
    }
    
    boolean hasPermission(Player player, String perm) {
        return perms.hasPermission(player.getName(), perm);
    }
    
    public WorldEditAPI getAPI() {
        return api;
    }
}
