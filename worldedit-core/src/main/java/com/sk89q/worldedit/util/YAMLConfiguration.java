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

package com.sk89q.worldedit.util;

import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashSet;

/**
 * A less simple implementation of {@link LocalConfiguration}
 * using YAML configuration files.
 */
public class YAMLConfiguration extends LocalConfiguration {

    @Unreported protected final YAMLProcessor config;
    @Unreported protected final Logger logger;

    public YAMLConfiguration(YAMLProcessor config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {
        try {
            config.load();
        } catch (IOException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }

        profile = config.getBoolean("debug", profile);
        traceUnflushedSessions = config.getBoolean("debugging.trace-unflushed-sessions", traceUnflushedSessions);
        wandItem = convertLegacyItem(config.getString("wand-item", wandItem));

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

        disallowedBlocks = new HashSet<>(config.getStringList("limits.disallowed-blocks", Lists.newArrayList(getDefaultDisallowedBlocks())));
        allowedDataCycleBlocks = new HashSet<>(config.getStringList("limits.allowed-data-cycle-blocks", null));

        registerHelp = config.getBoolean("register-help", true);
        logCommands = config.getBoolean("logging.log-commands", logCommands);
        logFile = config.getString("logging.file", logFile);
        logFormat = config.getString("logging.format", logFormat);

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

        navigationWand = convertLegacyItem(config.getString("navigation-wand.item", navigationWand));
        navigationWandMaxDistance = config.getInt("navigation-wand.max-distance", navigationWandMaxDistance);
        navigationUseGlass = config.getBoolean("navigation.use-glass", navigationUseGlass);

        scriptTimeout = config.getInt("scripting.timeout", scriptTimeout);
        scriptsDir = config.getString("scripting.dir", scriptsDir);

        calculationTimeout = config.getInt("calculation.timeout", calculationTimeout);
        maxCalculationTimeout = config.getInt("calculation.max-timeout", maxCalculationTimeout);

        saveDir = config.getString("saving.dir", saveDir);

        allowSymlinks = config.getBoolean("files.allow-symbolic-links", false);
        LocalSession.MAX_HISTORY_SIZE = Math.max(0, config.getInt("history.size", 15));
        SessionManager.EXPIRATION_GRACE = config.getInt("history.expiration", 10) * 60 * 1000;

        showHelpInfo = config.getBoolean("show-help-on-first-use", true);
        serverSideCUI = config.getBoolean("server-side-cui", true);

        String snapshotsDir = config.getString("snapshots.directory", "");
        if (!snapshotsDir.isEmpty()) {
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        }

        String type = config.getString("shell-save-type", "").trim();
        shellSaveType = type.isEmpty() ? null : type;

    }

    public void unload() {
    }

}
