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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.bukkit.migration.PermissionsResolverServerListener;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.regions.Region;

/**
 * Plugin for Bukkit.
 * 
 * @author sk89qs
 */
public class WorldEditPlugin extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    final ServerInterface server;
    final WorldEdit controller;
    final WorldEditAPI api;
    
    private final BukkitConfiguration config;
    private final PermissionsResolverManager perms;
    
    private final WorldEditPlayerListener playerListener =
        new WorldEditPlayerListener(this);
    private final WorldEditBlockListener blockListener =
        new WorldEditBlockListener(this);
    private final PermissionsResolverServerListener permsListener;

    public WorldEditPlugin(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        logger.info("WorldEdit " + desc.getVersion() + " loaded.");
        
        folder.mkdirs();

        createDefaultConfiguration("config.yml");
        
        config = new BukkitConfiguration(getConfiguration(), logger);
        perms = new PermissionsResolverManager(getConfiguration(), getServer(),
                "WorldEdit", logger);
        permsListener = new PermissionsResolverServerListener(perms);
        loadConfiguration();
        
        server = new BukkitServerInterface(this, getServer());
        controller = new WorldEdit(server, config);
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
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND,
                playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DAMAGED,
                blockListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.BLOCK_RIGHTCLICKED,
                blockListener, Priority.Normal, this);
        
        permsListener.register(this);
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
    
    void loadConfiguration() {
        getConfiguration().load();
        config.load();
        perms.load();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        
        Player player = (Player)sender;
        
        if (cmd.getName().equalsIgnoreCase("reloadwe")
                && hasPermission(player, "worldedit.reload")) {
            try {
                loadConfiguration();
                player.sendMessage("WorldEdit configuration reloaded.");
            } catch (Throwable t) {
                player.sendMessage("Error while reloading: "
                        + t.getMessage());
            }
            
            return true;
        }
        
        String[] split = new String[args.length + 1];
        System.arraycopy(args, 0, split, 1, args.length);
        split[0] = "/" + cmd.getName();
        
        controller.handleCommand(wrapPlayer(player), split);
        
        return true;
    }
    
    /**
     * Get a reference to the WorldEdit object.
     * 
     * @return
     */
    public WorldEdit getWorldEdit() {
        return controller;
    }
    
    /**
     * Gets the session for the player.
     * 
     * @param player
     * @return
     */
    public LocalSession getSession(Player player) {
        return controller.getSession(wrapPlayer(player));
    }
    
    /**
     * Gets the region selection for the player.
     * 
     * @param player
     * @return
     * @throws IncompleteRegionException 
     */
    public Region getPlayerSelection(Player player)
            throws IncompleteRegionException {
        return controller.getSession(wrapPlayer(player))
                .getSelection(new BukkitWorld(player.getWorld()));
    }
    
    /**
     * Gets the session for the player.
     * 
     * @param player
     * @return
     */
    public EditSession createEditSession(Player player) {
        LocalPlayer wePlayer = wrapPlayer(player);
        LocalSession session = controller.getSession(wePlayer);
        BlockBag blockBag = session.getBlockBag(wePlayer);
        
        EditSession editSession =
            new EditSession(wePlayer.getWorld(),
                    session.getBlockChangeLimit(), blockBag);
        editSession.enableQueue();
        
        return editSession;
    }
    
    /**
     * Remember an edit session.
     * 
     * @param player
     * @param editSession
     */
    public void remember(Player player, EditSession editSession) {
        LocalPlayer wePlayer = wrapPlayer(player);
        LocalSession session = controller.getSession(wePlayer);
        
        session.remember(editSession);
        editSession.flushQueue();
        
        controller.flushBlockBag(wePlayer, editSession);
    }
    
    /**
     * Wrap an operation into an EditSession.
     * 
     * @param player
     * @param op
     * @throws Throwable
     */
    public void perform(Player player, WorldEditOperation op)
            throws Throwable {
        LocalPlayer wePlayer = wrapPlayer(player);
        LocalSession session = controller.getSession(wePlayer);
        
        EditSession editSession = createEditSession(player);
        try {
            op.run(session, wePlayer, editSession);
        } finally {
            remember(player, editSession);
        }
    }
    
    @Deprecated
    public WorldEditAPI getAPI() {
        return api;
    }
    
    String[] getGroups(Player player) {
        return perms.getGroups(player.getName());
    }
    
    boolean inGroup(Player player, String group) {
        return perms.inGroup(player.getName(), group);
    }
    
    boolean hasPermission(Player player, String perm) {
        return (!config.noOpPermissions && player.isOp())
                || perms.hasPermission(player.getName(), perm);
    }
    
    BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, this.server, player);
    }
}
