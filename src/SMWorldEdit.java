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

import com.sk89q.worldedit.snapshots.SnapshotRepository;
import java.util.Map;
import java.util.HashSet;
import com.sk89q.worldedit.ServerInterface;

/**
 * Entry point for the plugin for hey0's mod.
 * 
 * @author sk89q
 */
public class SMWorldEdit extends Plugin {
    /**
     * WorldEdit's properties file.
     */
    private PropertiesFile properties;
    /**
     * WorldEdit instance.
     */
    private static final WorldEdit worldEdit =
            WorldEdit.setup(new SMServerInterface());
    /**
     * Listener for the plugin system.
     */
    private static final SMWorldEditListener listener =
            new SMWorldEditListener();

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
                WorldEdit.getDefaultAllowedBlocks()).split(",")) {
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
}
