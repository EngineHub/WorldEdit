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

package com.sk89q.worldedit.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.snapshots.SnapshotRepository;

/**
 * A less simple implementation of {@link LocalConfiguration} using YAML configuration files.
 *
 * @author sk89q
 */
public class YAMLConfiguration extends LocalConfiguration {
    protected final YAMLProcessor config;
    protected final Logger logger;

    public YAMLConfiguration(YAMLProcessor config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {
        try {
            config.load();
        } catch (IOException e) {
            logger.severe("Error loading WorldEdit configuration: " + e);
            e.printStackTrace();
        }
        showFirstUseVersion = false;

        profile = config.getBoolean("debug", profile);
        wandItem = config.getInt("wand-item", wandItem);

        defaultChangeLimit = Math.max(-1, config.getInt(
                "limits.max-blocks-changed.default", defaultChangeLimit));
        maxChangeLimit = Math.max(-1,
                config.getInt("limits.max-blocks-changed.maximum", maxChangeLimit));

        defaultMaxPolygonalPoints = Math.max(-1,
                config.getInt("limits.max-polygonal-points.default", defaultMaxPolygonalPoints));
        maxPolygonalPoints = Math.max(-1,
                config.getInt("limits.max-polygonal-points.maximum", maxPolygonalPoints));

        defaultMaxPolyhedronPoints = Math.max(-1, config.getInt("limits.max-polyhedron-points.default", defaultMaxPolyhedronPoints));
        maxPolyhedronPoints = Math.max(-1, config.getInt("limits.max-polyhedron-points.maximum", maxPolyhedronPoints));

        maxRadius = Math.max(-1, config.getInt("limits.max-radius", maxRadius));
        maxBrushRadius = config.getInt("limits.max-brush-radius", maxBrushRadius);
        maxSuperPickaxeSize = Math.max(1, config.getInt(
                "limits.max-super-pickaxe-size", maxSuperPickaxeSize));

        butcherDefaultRadius = Math.max(-1, config.getInt("limits.butcher-radius.default", butcherDefaultRadius));
        butcherMaxRadius = Math.max(-1, config.getInt("limits.butcher-radius.maximum", butcherMaxRadius));

        disallowedBlocks = new HashSet<Integer>(config.getIntList("limits.disallowed-blocks", null));
        allowedDataCycleBlocks = new HashSet<Integer>(config.getIntList("limits.allowed-data-cycle-blocks", null));

        allowExtraDataValues = config.getBoolean("limits.allow-extra-data-values", false);


        registerHelp = config.getBoolean("register-help", true);
        logCommands = config.getBoolean("logging.log-commands", logCommands);
        logFile = config.getString("logging.file", logFile);

        superPickaxeDrop = config.getBoolean("super-pickaxe.drop-items",
                superPickaxeDrop);
        superPickaxeManyDrop = config.getBoolean(
                "super-pickaxe.many-drop-items", superPickaxeManyDrop);

        noDoubleSlash = config.getBoolean("no-double-slash", noDoubleSlash);

        useInventory = config.getBoolean("use-inventory.enable", useInventory);
        useInventoryOverride = config.getBoolean("use-inventory.allow-override",
                useInventoryOverride);
        useInventoryCreativeOverride = config.getBoolean("use-inventory.creative-mode-overrides",
                useInventoryCreativeOverride);

        navigationWand = config.getInt("navigation-wand.item", navigationWand);
        navigationWandMaxDistance = config.getInt("navigation-wand.max-distance", navigationWandMaxDistance);

        scriptTimeout = config.getInt("scripting.timeout", scriptTimeout);
        scriptsDir = config.getString("scripting.dir", scriptsDir);

        saveDir = config.getString("saving.dir", saveDir);

        allowSymlinks = config.getBoolean("files.allow-symbolic-links", false);
        LocalSession.MAX_HISTORY_SIZE = Math.max(0, config.getInt("history.size", 15));
        LocalSession.EXPIRATION_GRACE = config.getInt("history.expiration", 10) * 60 * 1000;

        String snapshotsDir = config.getString("snapshots.directory", "");
        if (snapshotsDir.length() > 0) {
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        }

        String type = config.getString("shell-save-type", "").trim();
        shellSaveType = type.equals("") ? null : type;

    }

    public void unload() {
    }
}
