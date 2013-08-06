package com.sk89q.worldedit.canarymod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import net.canarymod.config.Configuration;
import net.canarymod.logger.Logman;
import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UtilityException;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LogFormat;
import com.sk89q.worldedit.snapshots.SnapshotRepository;

public class CanaryConfiguration extends LocalConfiguration {
    private Logman logger = WorldEdit.logger;

    private PropertiesFile properties = Configuration.getPluginConfig("WorldEdit");

    @Override
    public void load() {
        showFirstUseVersion = properties.getBoolean("show-version-on-first-use", false);
        defaultMaxPolygonalPoints = Math.max(-1, properties.getInt("max-polygonal-points-default", defaultMaxPolygonalPoints));
        maxPolygonalPoints = Math.max(-1, properties.getInt("max-polygonal-points", maxPolygonalPoints));
        butcherDefaultRadius = Math.max(-1, properties.getInt("default-butcher-radius", butcherDefaultRadius));
        butcherMaxRadius = Math.max(-1, properties.getInt("max-butcher-radius", butcherMaxRadius));
        allowExtraDataValues = properties.getBoolean("allow-extra-data-values", false);
        allowSymlinks = properties.getBoolean("allow-symbolic-links", false);


        profile = properties.getBoolean("debug-profile", profile);
        wandItem = properties.getInt("wand-item", wandItem);
        defaultChangeLimit = Math.max(-1, properties.getInt("default-max-blocks-changed", defaultChangeLimit));
        maxChangeLimit = Math.max(-1, properties.getInt("max-blocks-changed", maxChangeLimit));
        maxRadius = Math.max(-1, properties.getInt("max-radius", maxRadius));
        maxSuperPickaxeSize = Math.max(1, properties.getInt("max-super-pickaxe-size", maxSuperPickaxeSize));
        registerHelp = properties.getBoolean("register-help", registerHelp);
        logCommands = properties.getBoolean("log-commands", logCommands);
        superPickaxeDrop = properties.getBoolean("super-pickaxe-drop-items", superPickaxeDrop);
        superPickaxeManyDrop = properties.getBoolean("super-pickaxe-many-drop-items", superPickaxeManyDrop);
        noDoubleSlash = properties.getBoolean("no-double-slash", noDoubleSlash);
        useInventory = properties.getBoolean("use-inventory", useInventory);
        useInventoryOverride = properties.getBoolean("use-inventory-override", useInventoryOverride);
        maxBrushRadius = properties.getInt("max-brush-radius", maxBrushRadius);

        navigationWand = properties.getInt("navigation-wand-item", navigationWand);
        navigationWandMaxDistance = properties.getInt("navigation-wand-max-distance", navigationWandMaxDistance);

        scriptTimeout = properties.getInt("script-timeout", scriptTimeout);
        scriptsDir = properties.getString("scripts-dir", scriptsDir);

        saveDir = properties.getString("save-dir", saveDir);

        // Get disallowed blocks
        try {
            disallowedBlocks = new HashSet<Integer>(toIntegerList(properties.getIntArray("disallowed-blocks")));
        }
        catch(UtilityException e) {
            //Derp
        }
        try {
            allowedDataCycleBlocks = new HashSet<Integer>(toIntegerList(properties.getIntArray("allowed-data-cycle-blocks")));
        }
        catch(UtilityException e) {
            //Derp
        }

        LocalSession.MAX_HISTORY_SIZE = Math.max(0, properties.getInt("history-size", 15));
        LocalSession.EXPIRATION_GRACE = properties.getInt("history-expiration", 10) * 60 * 1000;

        String snapshotsDir = properties.getString("snapshots-dir", "");
        if (!snapshotsDir.trim().equals("")) {
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        } else {
            snapshotRepo = null;
        }

        String type = properties.getString("shell-save-type", "").trim();
        shellSaveType = type.equals("") ? null : type;

        String logFile = properties.getString("log-file", "");
        if (!logFile.equals("")) {
            try {
                FileHandler handler = new FileHandler(logFile, true);
                handler.setFormatter(new LogFormat());
                logger.addHandler(handler);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not use log file " + logFile + ": " + e.getMessage());
            }
        } else {
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }
        }
        properties.save();
    }

    public static List<Integer> toIntegerList(int[] arr) {
        ArrayList<Integer> l = new ArrayList<Integer>();
        for(int i : arr) {
            l.add(i);
        }
        return l;
    }
}
