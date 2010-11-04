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

import com.sk89q.worldedit.snapshots.SnapshotRepository;
import java.util.Map;
import java.util.HashSet;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import java.io.IOException;
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
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * WorldEditLibrary's properties file.
     */
    private PropertiesFile properties;
    /**
     * WorldEditLibrary instance.
     */
    private static final WorldEditLibrary worldEdit =
            WorldEditLibrary.setup(new HmodServerInterface());
    /**
     * Listener for the plugin system.
     */
    private static final HmodWorldEditListener listener =
            new HmodWorldEditListener();

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

        logger.log(Level.INFO, "WorldEdit version " + getWorldEditVersion() + " loaded.");
    }

    /**
     * Enables the plugin.
     */
    @Override
    public void enable() {
        if (properties == null) {
            properties = new PropertiesFile("worldedit.properties");
        } else {
            properties.load();
        }

        // Get allowed blocks
        HashSet<Integer> allowedBlocks = new HashSet<Integer>();
        for (String b : properties.getString("allowed-blocks",
                WorldEditLibrary.getDefaultAllowedBlocks()).split(",")) {
            try {
                allowedBlocks.add(Integer.parseInt(b));
            } catch (NumberFormatException e) {
            }
        }
        worldEdit.setAllowedBlocks(allowedBlocks);
        
        worldEdit.setDefaultChangeLimit(
                Math.max(-1, properties.getInt("max-blocks-changed", -1)));

        String snapshotsDir = properties.getString("snapshots-dir", "");
        if (!snapshotsDir.trim().equals("")) {
            worldEdit.setSnapshotRepository(new SnapshotRepository(snapshotsDir));
        }

        String shellSaveType = properties.getString("shell-save-type", "").trim();
        worldEdit.setShellSaveType(shellSaveType.equals("") ? null : shellSaveType);

        for (Map.Entry<String,String> entry : worldEdit.getCommands().entrySet()) {
            etc.getInstance().addCommand(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void disable() {
        for (String key : worldEdit.getCommands().keySet()) {
            etc.getInstance().removeCommand(key);
        }

        worldEdit.clearSessions();
    }

    /**
     * Get the WorldEdit version.
     * 
     * @return
     */
    private String getWorldEditVersion() {
        try {
            String classContainer = WorldEdit.class.getProtectionDomain()
                    .getCodeSource().getLocation().toString();
            URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes attrib = manifest.getMainAttributes();
            String ver = (String)attrib.getValue("WorldEdit-Version");
            return ver != null ? ver : "(unavailable)";
        } catch (IOException e) {
            return "(unknown)";
        }
    }
}
