/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.extension.platform.PlatformRejectionException;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Plugin for Bukkit.
 *
 * @author sk89q
 */
public class WorldEditPlugin extends JavaPlugin {

    /**
     * The name of the CUI's plugin channel registration
     */
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";

    /**
     * The server interface that all server-related API goes through.
     */
    private BukkitServerInterface server;
    /**
     * Main WorldEdit instance.
     */
    private WorldEdit controller;
    /**
     * Deprecated API.
     */
    private WorldEditAPI api;

    /**
     * Holds the configuration for WorldEdit.
     */
    private BukkitConfiguration config;

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {
        final String pluginYmlVersion = getDescription().getVersion();
        final String manifestVersion = WorldEdit.getVersion();

        if (!manifestVersion.equalsIgnoreCase(pluginYmlVersion)) {
            WorldEdit.setVersion(manifestVersion + " (" + pluginYmlVersion + ")");
        }

        // Make the data folders that WorldEdit uses
        getDataFolder().mkdirs();
        File targetDir = new File(getDataFolder() + File.separator + "nmsblocks");
        targetDir.mkdir();
        copyNmsBlockClasses(targetDir);

        // Create the default configuration file
        createDefaultConfiguration("config.yml");

        // Set up configuration and such, including the permissions
        // resolver
        config = new BukkitConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true), this);
        PermissionsResolverManager.initialize(this);

        // Load the configuration
        config.load();

        // Setup interfaces
        server = new BukkitServerInterface(this, getServer());
        controller = WorldEdit.getInstance();
        try {
            controller.getPlatformManager().register(server);
            api = new WorldEditAPI(this);
            getServer().getMessenger().registerIncomingPluginChannel(this, CUI_PLUGIN_CHANNEL, new CUIChannelListener(this));
            getServer().getMessenger().registerOutgoingPluginChannel(this, CUI_PLUGIN_CHANNEL);
            // Now we can register events!
            getServer().getPluginManager().registerEvents(new WorldEditListener(this), this);

            getServer().getScheduler().runTaskTimerAsynchronously(this,
                    new SessionTimer(controller, getServer()), 120, 120);
        } catch (PlatformRejectionException e) {
            throw new RuntimeException(
                    "WorldEdit rejected the Bukkit implementation of WorldEdit! This is strange and should " +
                            "not have happened. Please report this error.", e);
        }
    }

    private void copyNmsBlockClasses(File target) {
        try {
            JarFile jar = new JarFile(getFile());
            @SuppressWarnings("rawtypes")
            Enumeration entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                if (!jarEntry.getName().startsWith("nmsblocks") || jarEntry.isDirectory()) continue;

                File file = new File(target + File.separator + jarEntry.getName().replace("nmsblocks", ""));
                if (file.exists()) continue;

                InputStream is = jar.getInputStream(jarEntry);
                FileOutputStream fos = new FileOutputStream(file);

                fos = new FileOutputStream(file);
                byte[] buf = new byte[8192];
                int length = 0;
                while ((length = is.read(buf)) > 0) {
                    fos.write(buf, 0, length);
                }
                fos.close();
                is.close();
            }
        } catch (Throwable e) {}
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {
        controller.clearSessions();
        controller.getPlatformManager().unregister(server);
        config.unload();
        server.unregisterCommands();
        this.getServer().getScheduler().cancelTasks(this);
    }

    /**
     * Loads and reloads all configuration.
     */
    protected void loadConfiguration() {
        config.unload();
        config.load();
        getPermissionsResolver().load();
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name
     */
    protected void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            InputStream input =
                    null;
            try {
                JarFile file = new JarFile(getFile());
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null) throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                getLogger().severe("Unable to read default configuration: " + name);
            }
            if (input != null) {
                FileOutputStream output = null;

                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    getLogger().info("Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (IOException e) {}

                    try {
                        if (output != null) {
                            output.close();
                        }
                    } catch (IOException e) {}
                }
            }
        }
    }

    /**
     * Called on WorldEdit command.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd,
            String commandLabel, String[] args) {

        // Add the command to the array because the underlying command handling
        // code of WorldEdit expects it
        String[] split = new String[args.length + 1];
        System.arraycopy(args, 0, split, 1, args.length);
        split[0] = "/" + cmd.getName();

        controller.handleCommand(wrapCommandSender(sender), split);

        return true;
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
     * Gets the session for the player.
     *
     * @param player
     * @return
     */
    public EditSession createEditSession(Player player) {
        LocalPlayer wePlayer = wrapPlayer(player);
        LocalSession session = controller.getSession(wePlayer);
        BlockBag blockBag = session.getBlockBag(wePlayer);

        EditSession editSession = controller.getEditSessionFactory()
                .getEditSession(wePlayer.getWorld(), session.getBlockChangeLimit(), blockBag, wePlayer);
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

    /**
     * Get the API.
     *
     * @return
     */
    @Deprecated
    public WorldEditAPI getAPI() {
        return api;
    }

    /**
     * Returns the configuration used by WorldEdit.
     *
     * @return
     */
    public BukkitConfiguration getLocalConfiguration() {
        return config;
    }

    /**
     * Get the permissions resolver in use.
     *
     * @return
     */
    public PermissionsResolverManager getPermissionsResolver() {
        return PermissionsResolverManager.getInstance();
    }

    /**
     * Used to wrap a Bukkit Player as a LocalPlayer.
     *
     * @param player
     * @return
     */
    public BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, this.server, player);
    }

    public LocalPlayer wrapCommandSender(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new BukkitCommandSender(this, this.server, sender);
    }

    /**
     * Get the server interface.
     *
     * @return
     */
    public ServerInterface getServerInterface() {
        return server;
    }

    /**
     * Get WorldEdit.
     *
     * @return
     */
    public WorldEdit getWorldEdit() {
        return controller;
    }

    /**
     * Gets the region selection for the player.
     *
     * @param player
     * @return the selection or null if there was none
     */
    public Selection getSelection(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Null player not allowed");
        }
        if (!player.isOnline()) {
            throw new IllegalArgumentException("Offline player not allowed");
        }

        LocalSession session = controller.getSession(wrapPlayer(player));
        RegionSelector selector = session.getRegionSelector(BukkitUtil.getLocalWorld(player.getWorld()));

        try {
            Region region = selector.getRegion();
            World world = ((BukkitWorld) session.getSelectionWorld()).getWorld();

            if (region instanceof CuboidRegion) {
                return new CuboidSelection(world, selector, (CuboidRegion) region);
            } else if (region instanceof Polygonal2DRegion) {
                return new Polygonal2DSelection(world, selector, (Polygonal2DRegion) region);
            } else {
                return null;
            }
        } catch (IncompleteRegionException e) {
            return null;
        }
    }

    /**
     * Sets the region selection for a player.
     *
     * @param player
     * @param selection
     */
    public void setSelection(Player player, Selection selection) {
        if (player == null) {
            throw new IllegalArgumentException("Null player not allowed");
        }
        if (!player.isOnline()) {
            throw new IllegalArgumentException("Offline player not allowed");
        }
        if (selection == null) {
            throw new IllegalArgumentException("Null selection not allowed");
        }

        LocalSession session = controller.getSession(wrapPlayer(player));
        RegionSelector sel = selection.getRegionSelector();
        session.setRegionSelector(BukkitUtil.getLocalWorld(player.getWorld()), sel);
        session.dispatchCUISelection(wrapPlayer(player));
    }
}
