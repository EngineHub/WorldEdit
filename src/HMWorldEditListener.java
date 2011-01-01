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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.io.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.data.*;
import com.sk89q.worldedit.filters.*;
import com.sk89q.worldedit.snapshots.*;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.patterns.*;

/**
 * Plugin base.
 *
 * @author sk89q
 */
public class HMWorldEditListener extends PluginListener {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    /**
     * WorldEditLibrary's properties file.
     */
    private PropertiesFile properties;
    
    /**
     * Main WorldEdit controller.
     */
    private WorldEditController controller = new WorldEditController();
    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        controller.handleDisconnect(new HMPlayer(player));
    }

    /**
     * Called on arm swing.
     * 
     * @param player
     */
    public void onArmSwing(Player player) {
        controller.handleArmSwing(new HMPlayer(player));
    }

    /**
     * Called on right click.
     *
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        Vector pos = new Vector(blockClicked.getX(),
                blockClicked.getY(),
                blockClicked.getZ());
        return controller.handleBlockRightClick(new HMPlayer(player), pos);
    }

    /**
     * Called on left click.
     *
     * @param player
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockDestroy(Player player, Block blockClicked) {
        Vector pos = new Vector(blockClicked.getX(),
                blockClicked.getY(),
                blockClicked.getZ());
        return controller.handleBlockLeftClick(new HMPlayer(player), pos);
    }

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        return controller.handleCommand(new HMPlayer(player), split);
    }

    /**
     * Loads the configuration.
     */
    public void loadConfiguration() {
        if (properties == null) {
            properties = new PropertiesFile("worldedit.properties");
        } else {
            try {
                properties.load();
            } catch (IOException e) {
                logger.warning("worldedit.properties could not be loaded: "
                        + e.getMessage());
            }
        }

        controller.profile = properties.getBoolean("debug-profile", false);
        controller.wandItem = properties.getInt("wand-item", 271);
        controller.defaultChangeLimit = Math.max(-1, properties.getInt("default-max-blocks-changed", -1));
        controller.maxChangeLimit = Math.max(-1, properties.getInt("max-blocks-changed", -1));
        controller.maxRadius = Math.max(-1, properties.getInt("max-radius", -1));
        controller.maxSuperPickaxeSize = Math.max(1, properties.getInt("max-super-pickaxe-size", 5));
        controller.registerHelp = properties.getBoolean("register-help", true);
        controller.logComands = properties.getBoolean("log-commands", false);
        controller.superPickaxeDrop = properties.getBoolean("super-pickaxe-drop-items", true);
        controller.superPickaxeManyDrop = properties.getBoolean("super-pickaxe-many-drop-items", false);
        controller.noDoubleSlash = properties.getBoolean("no-double-slash", false);
        controller.useInventory = properties.getBoolean("use-inventory", false);
        controller.useInventoryOverride = properties.getBoolean("use-inventory-override", false);
        
        // Get allowed blocks
        controller.allowedBlocks = new HashSet<Integer>();
        for (String b : properties.getString("allowed-blocks",
                WorldEditController.getDefaultAllowedBlocks()).split(",")) {
            try {
                controller.allowedBlocks.add(Integer.parseInt(b));
            } catch (NumberFormatException e) {
            }
        }


        String snapshotsDir = properties.getString("snapshots-dir", "");
        if (!snapshotsDir.trim().equals("")) {
            controller.snapshotRepo = new SnapshotRepository(snapshotsDir);
        } else {
            controller.snapshotRepo = null;
        }

        String type = properties.getString("shell-save-type", "").trim();
        controller.shellSaveType = type.equals("") ? null : type;

        String logFile = properties.getString("log-file", "");
        if (!logFile.equals("")) {
            try {
                FileHandler handler = new FileHandler(logFile, true);
                handler.setFormatter(new LogFormat());
                logger.addHandler(handler);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not use log file " + logFile + ": "
                        + e.getMessage());
            }
        } else {
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }
        }
    }

    /**
     * Register commands with help.
     */
    public void registerCommands() {
        if (controller.registerHelp) {
            for (Map.Entry<String,String> entry : controller.getCommands().entrySet()) {
                etc.getInstance().addCommand(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * De-register commands.
     */
    public void deregisterCommands() {
        for (String key : controller.getCommands().keySet()) {
            etc.getInstance().removeCommand(key);
        }
    }
    
    /**
     * Clear sessions.
     */
    public void clearSessions() {
        controller.clearSessions();
    }

    /**
     * Gets the WorldEditLibrary session for a player. Used for the bridge.
     *
     * @param player
     * @return
     */
    public WorldEditSession _bridgeSession(Player player) {
        return controller.getBridgeSession(new HMPlayer(player));
    }
}
