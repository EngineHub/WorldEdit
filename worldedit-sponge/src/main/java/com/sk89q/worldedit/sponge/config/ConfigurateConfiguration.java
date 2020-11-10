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

package com.sk89q.worldedit.sponge.config;

import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

public class ConfigurateConfiguration extends LocalConfiguration {

    @Unreported protected final ConfigurationLoader<CommentedConfigurationNode> config;
    @Unreported protected final Logger logger;

    @Unreported protected CommentedConfigurationNode node;

    public ConfigurateConfiguration(ConfigurationLoader<CommentedConfigurationNode> config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {
        try {
            ConfigurationOptions options = ConfigurationOptions.defaults();
            options = options.setShouldCopyDefaults(true);

            node = config.load(options);
        } catch (IOException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }

        profile = node.getNode("debug").getBoolean(profile);
        traceUnflushedSessions = node.getNode("debugging", "trace-unflushed-sessions").getBoolean(traceUnflushedSessions);
        wandItem = node.getNode("wand-item").getString(wandItem).toLowerCase(Locale.ROOT);
        try {
            wandItem = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(wandItem)).getId();
        } catch (Throwable ignored) {
        }

        defaultChangeLimit = Math.max(-1, node.getNode("limits", "max-blocks-changed", "default").getInt(defaultChangeLimit));
        maxChangeLimit = Math.max(-1, node.getNode("limits", "max-blocks-changed", "maximum").getInt(maxChangeLimit));

        defaultVerticalHeight = Math.max(1, node.getNode("limits", "vertical-height", "default").getInt(defaultVerticalHeight));

        defaultMaxPolygonalPoints = Math.max(-1, node.getNode("limits", "max-polygonal-points", "default").getInt(defaultMaxPolygonalPoints));
        maxPolygonalPoints = Math.max(-1, node.getNode("limits", "max-polygonal-points", "maximum").getInt(maxPolygonalPoints));

        maxRadius = Math.max(-1, node.getNode("limits", "max-radius").getInt(maxRadius));
        maxBrushRadius = node.getNode("limits", "max-brush-radius").getInt(maxBrushRadius);
        maxSuperPickaxeSize = Math.max(1, node.getNode("limits", "max-super-pickaxe-size").getInt(maxSuperPickaxeSize));

        butcherDefaultRadius = Math.max(-1, node.getNode("limits", "butcher-radius", "default").getInt(butcherDefaultRadius));
        butcherMaxRadius = Math.max(-1, node.getNode("limits", "butcher-radius", "maximum").getInt(butcherMaxRadius));

        try {
            disallowedBlocks = new HashSet<>(node.getNode("limits", "disallowed-blocks").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }
        try {
            allowedDataCycleBlocks = new HashSet<>(node.getNode("limits", "allowed-data-cycle-blocks").getList(TypeToken.of(String.class)));
        } catch (ObjectMappingException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }

        registerHelp = node.getNode("register-help").getBoolean(true);
        logCommands = node.getNode("logging", "log-commands").getBoolean(logCommands);
        logFile = node.getNode("logging", "file").getString(logFile);
        logFormat = node.getNode("logging", "format").getString(logFormat);

        superPickaxeDrop = node.getNode("super-pickaxe", "drop-items").getBoolean(superPickaxeDrop);
        superPickaxeManyDrop = node.getNode("super-pickaxe", "many-drop-items").getBoolean(superPickaxeManyDrop);

        useInventory = node.getNode("use-inventory", "enable").getBoolean(useInventory);
        useInventoryOverride = node.getNode("use-inventory", "allow-override").getBoolean(useInventoryOverride);
        useInventoryCreativeOverride = node.getNode("use-inventory", "creative-mode-overrides").getBoolean(useInventoryCreativeOverride);

        navigationWand = node.getNode("navigation-wand", "item").getString(navigationWand).toLowerCase(Locale.ROOT);
        try {
            navigationWand = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(navigationWand)).getId();
        } catch (Throwable ignored) {
        }
        navigationWandMaxDistance = node.getNode("navigation-wand", "max-distance").getInt(navigationWandMaxDistance);
        navigationUseGlass = node.getNode("navigation", "use-glass").getBoolean(navigationUseGlass);

        scriptTimeout = node.getNode("scripting", "timeout").getInt(scriptTimeout);
        scriptsDir = node.getNode("scripting", "dir").getString(scriptsDir);

        saveDir = node.getNode("saving", "dir").getString(saveDir);

        allowSymlinks = node.getNode("files", "allow-symbolic-links").getBoolean(false);
        LocalSession.MAX_HISTORY_SIZE = Math.max(0, node.getNode("history", "size").getInt(15));
        SessionManager.EXPIRATION_GRACE = node.getNode("history", "expiration").getInt(10) * 60 * 1000;

        showHelpInfo = node.getNode("show-help-on-first-use").getBoolean(true);
        serverSideCUI = node.getNode("server-side-cui").getBoolean(true);

        String snapshotsDir = node.getNode("snapshots", "directory").getString("");
        boolean experimentalSnapshots = node.getNode("snapshots", "experimental").getBoolean(false);
        initializeSnapshotConfiguration(snapshotsDir, experimentalSnapshots);

        String type = node.getNode("shell-save-type").getString("").trim();
        shellSaveType = type.equals("") ? null : type;

        extendedYLimit = node.getNode("compat", "extended-y-limit").getBoolean(false);
        setDefaultLocaleName(node.getNode("default-locale").getString(defaultLocaleName));
    }
}
