// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.spout;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.spout.selections.CuboidSelection;
import com.sk89q.worldedit.spout.selections.Polygonal2DSelection;
import com.sk89q.worldedit.spout.selections.Selection;
import com.sk89q.worldedit.util.YAMLConfiguration;
import org.spout.api.command.CommandSource;
import org.spout.api.geo.World;
import org.spout.api.player.Player;
import org.spout.api.plugin.CommonPlugin;
import org.spout.api.util.Named;

import java.io.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Plugin for Spout.
 * 
 * @author sk89q
 */
public class WorldEditPlugin extends CommonPlugin implements Named {
    /**
     * WorldEdit messages get sent here.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");

    /**
     * The server interface that all server-related API goes through.
     */
    private ServerInterface server;
    /**
     * Main WorldEdit instance.
     */
    private WorldEdit controller;

    /**
     * Holds the configuration for WorldEdit.
     */
    private YAMLConfiguration config;

    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        final String pluginYmlVersion = getDescription().getVersion();
        final String manifestVersion = WorldEdit.getVersion();

        logger.info("WorldEdit " + pluginYmlVersion + " enabled.");
        if (!manifestVersion.equalsIgnoreCase(pluginYmlVersion)) {
            WorldEdit.setVersion(manifestVersion + " (" + pluginYmlVersion + ")");
        }

        // Make the data folders that WorldEdit uses
        getDataFolder().mkdirs();

        // Create the default configuration file
        createDefaultConfiguration("config.yml");

        // Set up configuration and such, including the permissions
        // resolver
        config = new YAMLConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true), logger);

        // Load the configuration
        loadConfiguration();

        // Setup interfaces
        server = new SpoutServerInterface(this, getGame());
        controller = new WorldEdit(server, config);

        // Now we can register events!
        registerEvents();

        getGame().getScheduler().scheduleAsyncRepeatingTask(this,
                new SessionTimer(controller, getGame()), 120, 120);
    }

    /**
     * Called on plugin disable.
     */
    public void onDisable() {
        for (Player player : getGame().getOnlinePlayers()) {
            LocalPlayer lPlayer = wrapPlayer(player);
            if (controller.getSession(lPlayer).hasCUISupport()) {
                lPlayer.dispatchCUIHandshake();
            }
        }
        controller.clearSessions();
        config.unload();
        getGame().getScheduler().cancelTasks(this);
    }

    /**
     * Loads and reloads all configuration.
     */
    protected void loadConfiguration() {
        config.unload();
        config.load();
    }

    /**
     * Register the events used by WorldEdit.
     */
    protected void registerEvents() {
        getGame().getEventManager().registerEvents(new WorldEditPlayerListener(this), this);
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
                logger.severe(getDescription().getName() + ": Unable to read default configuration: " + name);
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

                    logger.info(getDescription().getName()
                            + ": Default configuration file written: " + name);
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

        EditSession editSession =
                new EditSession(wePlayer.getWorld(), session.getBlockChangeLimit(), blockBag);
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
     * Returns the configuration used by WorldEdit.
     *
     * @return
     */
    public YAMLConfiguration getLocalConfiguration() {
        return config;
    }

    /**
     * Used to wrap a Bukkit Player as a LocalPlayer.
     *
     * @param player
     * @return
     */
    public SpoutPlayer wrapPlayer(Player player) {
        return new SpoutPlayer(this, this.server, player);
    }

    public LocalPlayer wrapCommandSender(CommandSource sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new SpoutCommandSender(this, this.server, sender);
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
        RegionSelector selector = session.getRegionSelector(SpoutUtil.getLocalWorld(player.getEntity().getTransform().getPosition().getWorld()));

        try {
            Region region = selector.getRegion();
            World world = ((SpoutWorld) session.getSelectionWorld()).getWorld();

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
        session.setRegionSelector(SpoutUtil.getLocalWorld(player.getEntity().getTransform().getPosition().getWorld()), sel);
        session.dispatchCUISelection(wrapPlayer(player));
    }

    @Override
    public String getName() {
        return getDescription().getName();
    }
}
