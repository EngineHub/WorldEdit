// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.worldedit.bukkit;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.command.CommandContext;
import org.enginehub.command.CommandException;
import org.enginehub.command.CommandManager;
import org.enginehub.worldedit.WorldEdit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;

/**
 * Implementation of WorldEdit for Bukkit.
 *
 * <p>Interested parties probably want to call
 * {@link org.enginehub.worldedit.WorldEdit#getInstance()} to get a reference to
 * the underlying WorldEdit object.</p>
 */
public class WorldEditPlugin extends JavaPlugin implements Listener {

    private BukkitConfiguration config;

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        config = new BukkitConfiguration(new YAMLProcessor(
                new File(getDataFolder(), "config.yml"), true), this);
        PermissionsResolverManager.initialize(this);

        // Load the configuration
        config.load(); // @TOOD: Handle configuration failure
        
        WorldEdit.getInstance().setConfiguration(config); // Initialize WorldEdit
        
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        try {
            CommandContext context = new CommandContext(event.getMessage());
            context.putObject(LocalPlayer.class, new PlayerActor(event.getPlayer()));
            context.putObject(LocalWorld.class, new LoadedWorld(event.getPlayer().getWorld()));
            CommandManager commands = WorldEdit.getInstance().getCommands();
            commands.execute(context);
        } catch (CommandException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getLocalizedMessage());

            // Write the actual error to the log
            if (e.getCause() != null) {
                getLogger().log(Level.SEVERE,
                        "An exception occurred during command processing", e);
            }
        }
    }

    // @TODO: Tab complete
}
