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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.command.CommandContext;
import org.enginehub.command.CommandException;
import org.enginehub.command.CommandManager;
import org.enginehub.common.WorldObject;
import org.enginehub.event.EventSystem;
import org.enginehub.util.ControllerFunction;
import org.enginehub.util.Owner;
import org.enginehub.worldedit.InteractiveProxy;
import org.enginehub.worldedit.WorldEdit;
import org.enginehub.worldedit.event.ActorInteractEvent;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.WorldVector;

/**
 * Implementation of WorldEdit for Bukkit.
 *
 * <p>Interested parties probably want to call
 * {@link org.enginehub.worldedit.WorldEdit#getInstance()} to get a reference to
 * the underlying WorldEdit object.</p>
 */
public class WorldEditPlugin extends JavaPlugin implements Listener, Owner {

    private BukkitConfiguration config;
    
    /**
     * A hack to work around limitations with handling interaction events.
     */
    @Deprecated
    private long lastLeftClickAir; // @TODO: This is really bad -- not per-player

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        config = new BukkitConfiguration(new YAMLProcessor(
                new File(getDataFolder(), "config.yml"), true), this);
        PermissionsResolverManager.initialize(this);

        // Load the configuration
        config.load(); // @TOOD: Handle configuration failure
        
        WorldEdit.getInstance().setConfiguration(config); // Initialize WorldEdit
        
        new InteractiveProxy(this, config);
        
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
    }
    
    /**
     * Convenient method to get a native {@link LocalPlayer} from a {@link Player}.
     * 
     * @param player the Bukkit player
     * @return the native player
     */
    public LocalPlayer toNative(Player player) {
        return new PlayerActor(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().length() < 2) {
            return; // Too short
        }
        
        try {
            CommandContext context = new CommandContext(event.getMessage().substring(1));
            context.putObject(LocalPlayer.class, toNative(event.getPlayer()));
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
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItemInHand() == Result.DENY) {
            return;
        }

        EventSystem dispatcher = EventSystem.getInstance();
        LocalPlayer actor = toNative(event.getPlayer());
        
        Block block;
        WorldObject worldObject;
        WorldVector pos;
        
        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            block = event.getClickedBlock();
            worldObject = new BukkitBlock(block);
            pos = BukkitUtils.toWorldVector(block);
            
            // Block hit
            if (dispatcher.testDispatch(new ActorInteractEvent(
                    actor, ControllerFunction.PRIMARY, pos, worldObject))) {
                event.setCancelled(true);
            }
            
            // Arm swing
            if (dispatcher.testDispatch(new ActorInteractEvent(
                    actor, ControllerFunction.PRIMARY))) {
                event.setCancelled(true);
            }
            
            lastLeftClickAir = System.currentTimeMillis();
            break;
            
        case RIGHT_CLICK_BLOCK:
            block = event.getClickedBlock();
            worldObject = new BukkitBlock(block);
            pos = BukkitUtils.toWorldVector(block);
            
            // Block hit
            if (dispatcher.testDispatch(new ActorInteractEvent(
                    actor, ControllerFunction.SECONDARY, pos, worldObject))) {
                event.setCancelled(true);
            }
            
            // Arm swing
            if (dispatcher.testDispatch(new ActorInteractEvent(
                    actor, ControllerFunction.SECONDARY))) {
                event.setCancelled(true);
            }
            
            break;
            
        case LEFT_CLICK_AIR:
            if (System.currentTimeMillis() - lastLeftClickAir > 20) {
                // Arm swing
                if (dispatcher.testDispatch(new ActorInteractEvent(
                        actor, ControllerFunction.PRIMARY))) {
                    event.setCancelled(true);
                }
                
                lastLeftClickAir = 0;
            }
            
            break;
            
        case RIGHT_CLICK_AIR:
            // Arm swing
            if (dispatcher.testDispatch(new ActorInteractEvent(
                    actor, ControllerFunction.SECONDARY))) {
                event.setCancelled(true);
            }
            
            break;
        
        case PHYSICAL:
            // Do nothing
            break;
        }
    }

    // @TODO: Tab complete
}
