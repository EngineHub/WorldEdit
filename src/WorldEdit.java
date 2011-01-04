// $Id$
/*
 * WorldEditLibrary
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the plugin for hey0's mod.
 * 
 * @author sk89q
 */
public class WorldEdit extends Plugin {
    /**
     * Logger.
     */
    private static final Logger logger = Logger
            .getLogger("Minecraft.WorldEdit");
    
    /**
     * The event listener for WorldEdit an hMod. Configuration and such is
     * also loaded here as well, although the core of the WorldEdit is
     * actually in com.sk89q.worldedit.WorldEditController and is merely
     * loaded by this listener.
     */
    private final HMWorldEditListener listener;

    /**
     * WorldEdit version, fetched from the .jar's manifest.
     */
    private String version;

    /**
     * Construct an instance of the plugin.
     */
    public WorldEdit() {
        listener = new HMWorldEditListener(new HMServerInterface());
    }

    /**
     * Initializes the plugin.
     */
    @Override
    public void initialize() {
        PluginLoader loader = etc.getLoader();

        loader.addListener(PluginLoader.Hook.BLOCK_CREATED, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.COMMAND, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.DISCONNECT, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.LOGIN, listener, this,
                PluginListener.Priority.MEDIUM);
        loader.addListener(PluginLoader.Hook.ARM_SWING, listener, this,
                PluginListener.Priority.MEDIUM);

        logger.log(Level.INFO, "WorldEdit version " + getVersion() + " loaded");
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        listener.loadConfiguration();
        listener.registerCommands();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        listener.deregisterCommands();
        listener.clearSessions();
    }

    /**
     * Get the WorldEdit version.
     * 
     * @return
     */
    public String getVersion() {
        if (version != null) {
            return version;
        }

        Package p = WorldEdit.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.sk89q.worldedit");
        }

        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();

            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }
    
    /**
     * Returns the listener for the WorldEdit bridge.
     * 
     * @return
     */
    public HMWorldEditListener getListener() {
        return listener;
    }
}
