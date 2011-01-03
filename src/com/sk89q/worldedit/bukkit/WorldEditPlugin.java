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
import java.util.HashSet;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldedit.*;

public class WorldEditPlugin extends JavaPlugin {
    public final ServerInterface server;
    public final WorldEditController controller;
    
    private final WorldEditPlayerListener playerListener =
        new WorldEditPlayerListener(this);
    private final WorldEditBlockListener blockListener =
        new WorldEditBlockListener(this);

    public WorldEditPlugin(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, plugin, cLoader);
        
        LocalConfiguration config = new LocalConfiguration() {
            @Override
            public void load() {
                // TODO Auto-generated method stub
                disallowedBlocks = new HashSet<Integer>();
            }
        };

        server = new BukkitServerInterface(getServer());
        controller = new WorldEditController(server, config);

        registerEvents();
    }

    public void onEnable() {
        //loadConfiguration();
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
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACED,
                blockListener, Priority.Normal, this);
    }
}
