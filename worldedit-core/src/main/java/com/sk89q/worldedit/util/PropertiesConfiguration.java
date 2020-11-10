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

// $Id$

package com.sk89q.worldedit.util;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.util.report.Unreported;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Simple LocalConfiguration that loads settings using
 * {@code java.util.Properties}.
 */
public class PropertiesConfiguration extends LocalConfiguration {

    @Unreported private static final Logger log = LoggerFactory.getLogger(PropertiesConfiguration.class);

    @Unreported protected Properties properties;
    @Unreported protected File path;

    /**
     * Construct the object. The configuration isn't loaded yet.
     *
     * @param path the path to the configuration
     */
    public PropertiesConfiguration(Path path) {
        this.path = path.toFile();

        properties = new Properties();
    }

    /**
     * Construct the object. The configuration isn't loaded yet.
     *
     * @param path the path to the configuration
     * @deprecated Use {@link PropertiesConfiguration#PropertiesConfiguration(Path)}
     */
    @Deprecated
    public PropertiesConfiguration(File path) {
        this(path.toPath());
    }

    @Override
    public void load() {
        try (InputStream stream = new FileInputStream(path)) {
            properties.load(stream);
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            log.warn("Failed to read configuration", e);
        }

        loadExtra();

        profile = getBool("profile", profile);
        traceUnflushedSessions = getBool("trace-unflushed-sessions", traceUnflushedSessions);
        disallowedBlocks = getStringSet("disallowed-blocks", getDefaultDisallowedBlocks());
        defaultChangeLimit = getInt("default-max-changed-blocks", defaultChangeLimit);
        maxChangeLimit = getInt("max-changed-blocks", maxChangeLimit);
        defaultVerticalHeight = getInt("default-vertical-height", defaultVerticalHeight);
        defaultMaxPolygonalPoints = getInt("default-max-polygon-points", defaultMaxPolygonalPoints);
        maxPolygonalPoints = getInt("max-polygon-points", maxPolygonalPoints);
        defaultMaxPolyhedronPoints = getInt("default-max-polyhedron-points", defaultMaxPolyhedronPoints);
        maxPolyhedronPoints = getInt("max-polyhedron-points", maxPolyhedronPoints);
        shellSaveType = getString("shell-save-type", shellSaveType);
        maxRadius = getInt("max-radius", maxRadius);
        maxSuperPickaxeSize = getInt("max-super-pickaxe-size", maxSuperPickaxeSize);
        maxBrushRadius = getInt("max-brush-radius", maxBrushRadius);
        logCommands = getBool("log-commands", logCommands);
        logFile = getString("log-file", logFile);
        logFormat = getString("log-format", logFormat);
        registerHelp = getBool("register-help", registerHelp);
        wandItem = getString("wand-item", wandItem).toLowerCase(Locale.ROOT);
        try {
            wandItem = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(wandItem)).getId();
        } catch (Throwable ignored) {
        }
        superPickaxeDrop = getBool("super-pickaxe-drop-items", superPickaxeDrop);
        superPickaxeManyDrop = getBool("super-pickaxe-many-drop-items", superPickaxeManyDrop);
        useInventory = getBool("use-inventory", useInventory);
        useInventoryOverride = getBool("use-inventory-override", useInventoryOverride);
        useInventoryCreativeOverride = getBool("use-inventory-creative-override", useInventoryCreativeOverride);
        navigationWand = getString("nav-wand-item", navigationWand).toLowerCase(Locale.ROOT);
        try {
            navigationWand = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(navigationWand)).getId();
        } catch (Throwable ignored) {
        }
        navigationWandMaxDistance = getInt("nav-wand-distance", navigationWandMaxDistance);
        navigationUseGlass = getBool("nav-use-glass", navigationUseGlass);
        scriptTimeout = getInt("scripting-timeout", scriptTimeout);
        calculationTimeout = getInt("calculation-timeout", calculationTimeout);
        maxCalculationTimeout = getInt("max-calculation-timeout", maxCalculationTimeout);
        saveDir = getString("schematic-save-dir", saveDir);
        scriptsDir = getString("craftscript-dir", scriptsDir);
        butcherDefaultRadius = getInt("butcher-default-radius", butcherDefaultRadius);
        butcherMaxRadius = getInt("butcher-max-radius", butcherMaxRadius);
        allowSymlinks = getBool("allow-symbolic-links", allowSymlinks);
        serverSideCUI = getBool("server-side-cui", serverSideCUI);
        extendedYLimit = getBool("extended-y-limit", extendedYLimit);
        setDefaultLocaleName(getString("default-locale", defaultLocaleName));

        LocalSession.MAX_HISTORY_SIZE = Math.max(15, getInt("history-size", 15));

        String snapshotsDir = getString("snapshots-dir", "");
        boolean experimentalSnapshots = getBool("snapshots-experimental", false);
        initializeSnapshotConfiguration(snapshotsDir, experimentalSnapshots);

        path.getParentFile().mkdirs();
        try (OutputStream output = new FileOutputStream(path)) {
            properties.store(output, "Don't put comments; they get removed");
        } catch (IOException e) {
            log.warn("Failed to write configuration", e);
        }
    }

    /**
     * Called to load extra configuration.
     */
    protected void loadExtra() {
    }

    /**
     * Get a string value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected String getString(String key, String def) {
        if (def == null) {
            def = "";
        }
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, def);
            return def;
        } else {
            return val;
        }
    }

    /**
     * Get a boolean value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected boolean getBool(String key, boolean def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, def ? "true" : "false");
            return def;
        } else {
            return val.equalsIgnoreCase("true")
                    || val.equals("1");
        }
    }

    /**
     * Get an integer value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected int getInt(String key, int def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, String.valueOf(def));
            return def;
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                properties.setProperty(key, String.valueOf(def));
                return def;
            }
        }
    }

    /**
     * Get a double value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected double getDouble(String key, double def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, String.valueOf(def));
            return def;
        } else {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e) {
                properties.setProperty(key, String.valueOf(def));
                return def;
            }
        }
    }

    /**
     * Get a double value.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected Set<Integer> getIntSet(String key, int[] def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, StringUtil.joinString(def, ",", 0));
            Set<Integer> set = new HashSet<>();
            for (int i : def) {
                set.add(i);
            }
            return set;
        } else {
            Set<Integer> set = new HashSet<>();
            String[] parts = val.split(",");
            for (String part : parts) {
                try {
                    int v = Integer.parseInt(part.trim());
                    set.add(v);
                } catch (NumberFormatException ignored) {
                }
            }
            return set;
        }
    }

    /**
     * Get a String set.
     *
     * @param key the key
     * @param def the default value
     * @return the value
     */
    protected Set<String> getStringSet(String key, String[] def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, StringUtil.joinString(def, ",", 0));
            return new HashSet<>(Arrays.asList(def));
        } else {
            Set<String> set = new HashSet<>();
            String[] parts = val.split(",");
            for (String part : parts) {
                try {
                    String v = part.trim();
                    set.add(v);
                } catch (NumberFormatException ignored) {
                }
            }
            return set;
        }
    }

}
