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
package com.sk89q.worldedit.util;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.snapshots.SnapshotRepository;

/**
 * Simple LocalConfiguration that loads settings using
 * <code>java.util.Properties</code>.
 * 
 * @author sk89q
 */
public class PropertiesConfiguration extends LocalConfiguration {

    protected Properties properties;
    protected File path;

    /**
     * Construct the object. The configuration isn't loaded yet.
     * 
     * @param path
     */
    public PropertiesConfiguration(File path) {
        this.path = path;

        properties = new Properties();
    }

    /**
     * Load the configuration file.
     */
    @Override
    public void load() {
        InputStream stream = null;
        try {
            stream = new FileInputStream(path);
            properties.load(stream);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }

        profile = getBool("profile", profile);
        disallowedBlocks = getIntSet("disallowed-blocks", defaultDisallowedBlocks);
        defaultChangeLimit = getInt("default-max-changed-blocks", defaultChangeLimit);
        maxChangeLimit = getInt("max-changed-blocks", maxChangeLimit);
        shellSaveType = getString("shell-save-type", shellSaveType);
        maxRadius = getInt("max-radius", maxRadius);
        maxSuperPickaxeSize = getInt("max-super-pickaxe-size", maxSuperPickaxeSize);
        maxBrushRadius = getInt("max-brush-radius", maxBrushRadius);
        logCommands = getBool("log-commands", logCommands);
        registerHelp = getBool("register-help", registerHelp);
        wandItem = getInt("wand-item", wandItem);
        superPickaxeDrop = getBool("super-pickaxe-drop-items", superPickaxeDrop);
        superPickaxeManyDrop = getBool("super-pickaxe-many-drop-items", superPickaxeManyDrop);
        noDoubleSlash = getBool("no-double-slash", noDoubleSlash);
        useInventory = getBool("use-inventory", useInventory);
        useInventoryOverride = getBool("use-inventory-override", useInventoryOverride);
        navigationWand = getInt("nav-wand-item", navigationWand);
        navigationWandMaxDistance = getInt("nav-wand-distance", navigationWandMaxDistance);
        scriptTimeout = getInt("scripting-timeout", scriptTimeout);

        LocalSession.MAX_HISTORY_SIZE = Math.max(15, getInt("history-size", 15));

        String snapshotsDir = getString("snapshots-dir", "");
        if (!snapshotsDir.isEmpty()) {
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        }

        OutputStream output = null;
        path.getParentFile().mkdirs();
        try {
            output = new FileOutputStream(path);
            properties.store(output, "Don't put comments; they get removed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Get a string value.
     * 
     * @param key
     * @param def
     * @return
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
     * @param key
     * @param def
     * @return
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
     * @param key
     * @param def
     * @return
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
     * @param key
     * @param def
     * @return
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
     * @param key
     * @param def
     * @return
     */
    protected Set<Integer> getIntSet(String key, int[] def) {
        String val = properties.getProperty(key);
        if (val == null) {
            properties.setProperty(key, StringUtil.joinString(def, ",", 0));
            Set<Integer> set = new HashSet<Integer>();
            for (int i : def) {
                set.add(i);
            }
            return set;
        } else {
            Set<Integer> set = new HashSet<Integer>();
            String[] parts = val.split(",");
            for (String part : parts) {
                try {
                    int v = Integer.parseInt(part.trim());
                    set.add(v);
                } catch (NumberFormatException e) {
                }
            }
            return set;
        }
    }
}
