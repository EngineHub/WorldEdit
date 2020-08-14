/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.YAMLConfiguration;
import com.sk89q.worldedit.util.report.Unreported;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * YAMLConfiguration but with setting for no op permissions and plugin root data folder.
 */
public class BukkitConfiguration extends YAMLConfiguration {

    public boolean noOpPermissions = false;
    public boolean commandBlockSupport = false;
    @Unreported private final WorldEditPlugin plugin;

    public BukkitConfiguration(YAMLProcessor config, WorldEditPlugin plugin) {
        super(config, LoggerFactory.getLogger(plugin.getLogger().getName()));
        this.plugin = plugin;
    }

    @Override
    public void load() {
        super.load();
        noOpPermissions = config.getBoolean("no-op-permissions", false);
        commandBlockSupport = config.getBoolean("command-block-support", false);
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
            if (fromDir.renameTo(toDir)) {
                plugin.getLogger().info("Migrated " + name + " folder '" + file
                    + "' from server root to plugin data folder.");
            } else {
                plugin.getLogger().warning("Error while migrating " + name + " folder!");
            }
        }
    }

    @Override
    public Path getWorkingDirectoryPath() {
        return plugin.getDataFolder().toPath();
    }
}
