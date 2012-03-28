// $Id$
/*
 * WorldEdit
 * Copyright (C) 2011 sk89q <http://www.sk89q.com> and contributors
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

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.YAMLConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * YAMLConfiguration but with setting for no op permissions and plugin root data folder
 */
public class BukkitConfiguration extends YAMLConfiguration {

    public boolean noOpPermissions = false;
    private final WorldEditPlugin plugin;

    public BukkitConfiguration(YAMLProcessor config, WorldEditPlugin plugin) {
        super(config, plugin.getLogger());
        this.plugin = plugin;
    }

    @Override
    public void load() {
        super.load();
        noOpPermissions = config.getBoolean("no-op-permissions", false);
        migrateLegacyFolders();
    }

    private void migrateLegacyFolders() {
        migrate(scriptsDir, "craftscripts");
        migrate(saveDir, "schematics");
        migrate("drawings", "draw.js images");
    }

    private void migrate(String file, String name) {
        File fromDir = new File(".", file);
        File toDir = new File(getWorkingDirectory(), file);
        if (fromDir.exists() & !toDir.exists()) {
            try {
                FileUtils.moveDirectory(fromDir, toDir);
                plugin.getLogger().info("Migrated " + name + " folder '" + file +
                        "' from server root to plugin data folder." );
            } catch (IOException e) {
                plugin.getLogger().warning("Error while migrating " + name + " folder: " +
                        e.getMessage());
            }
        }
    }

    @Override
    public File getWorkingDirectory() {
        return plugin.getDataFolder();
    }
}
