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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

public class ConfigurateConfiguration extends LocalConfiguration {

    @Unreported
    protected final ConfigurationLoader<CommentedConfigurationNode> config;
    @Unreported
    protected final Logger logger;

    @Unreported
    protected CommentedConfigurationNode node;

    public ConfigurateConfiguration(ConfigurationLoader<CommentedConfigurationNode> config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void load() {
        try {
            ConfigurationOptions options = ConfigurationOptions.defaults();
            options = options.shouldCopyDefaults(true);

            node = config.load(options);
        } catch (IOException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }

        profile = node.node("debug").getBoolean(profile);
        traceUnflushedSessions = node.node("debugging", "trace-unflushed-sessions").getBoolean(traceUnflushedSessions);
        wandItem = node.node("wand-item").getString(wandItem).toLowerCase(Locale.ROOT);
        try {
            wandItem = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(wandItem)).id();
        } catch (Throwable ignored) {
        }

        defaultChangeLimit = Math.max(-1, node.node("limits", "max-blocks-changed", "default").getInt(defaultChangeLimit));
        maxChangeLimit = Math.max(-1, node.node("limits", "max-blocks-changed", "maximum").getInt(maxChangeLimit));

        defaultVerticalHeight = Math.max(1, node.node("limits", "vertical-height", "default").getInt(defaultVerticalHeight));

        defaultMaxPolygonalPoints = Math.max(-1, node.node("limits", "max-polygonal-points", "default").getInt(defaultMaxPolygonalPoints));
        maxPolygonalPoints = Math.max(-1, node.node("limits", "max-polygonal-points", "maximum").getInt(maxPolygonalPoints));

        maxRadius = Math.max(-1, node.node("limits", "max-radius").getInt(maxRadius));
        maxBrushRadius = node.node("limits", "max-brush-radius").getInt(maxBrushRadius);
        maxSuperPickaxeSize = Math.max(1, node.node("limits", "max-super-pickaxe-size").getInt(maxSuperPickaxeSize));

        butcherDefaultRadius = Math.max(-1, node.node("limits", "butcher-radius", "default").getInt(butcherDefaultRadius));
        butcherMaxRadius = Math.max(-1, node.node("limits", "butcher-radius", "maximum").getInt(butcherMaxRadius));

        try {
            disallowedBlocks = new HashSet<>(
                node.node("limits", "disallowed-blocks").getList(
                    String.class,
                    ImmutableList.copyOf(getDefaultDisallowedBlocks())
                )
            );
        } catch (SerializationException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }
        try {
            allowedDataCycleBlocks = new HashSet<>(
                node.node("limits", "allowed-data-cycle-blocks").getList(String.class, ImmutableList.of())
            );
        } catch (SerializationException e) {
            logger.warn("Error loading WorldEdit configuration", e);
        }

        registerHelp = node.node("register-help").getBoolean(true);
        logCommands = node.node("logging", "log-commands").getBoolean(logCommands);
        logFile = node.node("logging", "file").getString(logFile);
        logFormat = node.node("logging", "format").getString(logFormat);

        superPickaxeDrop = node.node("super-pickaxe", "drop-items").getBoolean(superPickaxeDrop);
        superPickaxeManyDrop = node.node("super-pickaxe", "many-drop-items").getBoolean(superPickaxeManyDrop);

        useInventory = node.node("use-inventory", "enable").getBoolean(useInventory);
        useInventoryOverride = node.node("use-inventory", "allow-override").getBoolean(useInventoryOverride);
        useInventoryCreativeOverride = node.node("use-inventory", "creative-mode-overrides").getBoolean(useInventoryCreativeOverride);

        navigationWand = node.node("navigation-wand", "item").getString(navigationWand).toLowerCase(Locale.ROOT);
        try {
            navigationWand = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(navigationWand)).id();
        } catch (Throwable ignored) {
        }
        navigationWandMaxDistance = node.node("navigation-wand", "max-distance").getInt(navigationWandMaxDistance);
        navigationUseGlass = node.node("navigation", "use-glass").getBoolean(navigationUseGlass);

        scriptTimeout = node.node("scripting", "timeout").getInt(scriptTimeout);
        scriptsDir = node.node("scripting", "dir").getString(scriptsDir);

        saveDir = node.node("saving", "dir").getString(saveDir);

        allowSymlinks = node.node("files", "allow-symbolic-links").getBoolean(false);
        LocalSession.MAX_HISTORY_SIZE = Math.max(0, node.node("history", "size").getInt(15));
        SessionManager.EXPIRATION_GRACE = node.node("history", "expiration").getInt(10) * 60 * 1000;

        showHelpInfo = node.node("show-help-on-first-use").getBoolean(true);
        serverSideCUI = node.node("server-side-cui").getBoolean(true);

        String snapshotsDir = node.node("snapshots", "directory").getString("");
        boolean experimentalSnapshots = node.node("snapshots", "experimental").getBoolean(false);
        initializeSnapshotConfiguration(snapshotsDir, experimentalSnapshots);

        String type = node.node("shell-save-type").getString("").trim();
        shellSaveType = type.isEmpty() ? null : type;

        extendedYLimit = node.node("compat", "extended-y-limit").getBoolean(false);
        setDefaultLocaleName(node.node("default-locale").getString(defaultLocaleName));

        commandBlockSupport = node.node("command-block-support").getBoolean(false);
    }
}
