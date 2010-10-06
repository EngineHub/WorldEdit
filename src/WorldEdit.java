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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.*;
import org.mozilla.javascript.*;
import com.sk89q.worldedit.*;

/**
 * Plugin entry point for Hey0's mod.
 *
 * @author sk89q
 */
public class WorldEdit extends Plugin {
    private final static String DEFAULT_ALLOWED_BLOCKS =
        "0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,35,41,42,43," +
        "44,45,47,48,49,52,53,54,56,57,58,60,61,62,67,73,78,79,80,81,82,85";

    private static final Logger logger = Logger.getLogger("Minecraft");
    private HashMap<String,WorldEditSession> sessions = new HashMap<String,WorldEditSession>();
    private HashMap<String,String> commands = new HashMap<String,String>();

    private PropertiesFile properties;
    private String[] allowedBlocks;
    private int defaultMaxBlocksChanged;
    private boolean mapScriptCommands = false;

    /**
     * Construct an instance of the plugin.
     */
    public WorldEdit() {
        super();

        commands.put("/editpos1", "Set editing position #1");
        commands.put("/editpos2", "Set editing position #2");
        commands.put("/editundo", "Undo");
        commands.put("/editredo", "Redo");
        commands.put("/clearhistory", "Clear history");
        commands.put("/clearclipboard", "Clear clipboard");
        commands.put("/editsize", "Get size of selected region");
        commands.put("/editset", "[ID] - Set all blocks inside region");
        commands.put("/editoutline", "[ID] - Outline the region with blocks");
        commands.put("/editreplace", "[ID] <ToReplaceID> - Replace all existing blocks inside region");
        commands.put("/editoverlay", "[ID] - Overlay the area one layer");
        commands.put("/removeabove", "<Size> <Height> - Remove blocks above head");
        commands.put("/removebelow", "<Size> <Height> - Remove blocks below position");
        commands.put("/editcopy", "Copies the currently selected region");
        commands.put("/editpaste", "Pastes the clipboard");
        commands.put("/editpasteair", "Pastes the clipboard (with air)");
        commands.put("/editstack [Dir] <Count>", "Stacks the clipboard");
        commands.put("/editstackair [Dir] <Count>", "Stacks the clipboard (with air)");
        commands.put("/editload", "[Filename] - Load .schematic into clipboard");
        commands.put("/editsave", "[Filename] - Save clipboard to .schematic");
        commands.put("/editfill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("/editcyl", "[ID] [Radius] <Height> - Create cylinder");
        commands.put("/editscript", "[Filename] <Args...> - Run a WorldEdit script");
        commands.put("/editlimit", "[Num] - See documentation");
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    private WorldEditSession getSession(Player player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            WorldEditSession session = new WorldEditSession();
            session.setBlockChangeLimit(defaultMaxBlocksChanged);
            sessions.put(player.getName(), session);
            return session;
        }
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    private int getItem(String id, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        int foundID;

        try {
            foundID = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            try {
                foundID = etc.getDataSource().getItem(id);
            } catch (NumberFormatException e2) {
                throw new UnknownItemException();
            }
        }

        // All items allowed
        if (allAllowed || allowedBlocks[0].equals("")) {
            return foundID;
        }

        for (String s : allowedBlocks) {
            if (s.equals(String.valueOf(foundID))) {
                return foundID;
            }
        }

        throw new DisallowedItemException();
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    private int getItem(String id) throws UnknownItemException,
                                          DisallowedItemException {
        return getItem(id, false);
    }

    /**
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        sessions.remove(player.getName());
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min || (max != -1 && args.length - 1 > max)) {
            if (commands.containsKey(cmd)) {
                throw new InsufficientArgumentsException(cmd + " usage: " +
                        commands.get(cmd));
            } else {
                throw new InsufficientArgumentsException("Invalid number of arguments");
            }
        }
    }

    /**
     * Enables the plugin.
     */
    public void enable() {
        if (properties == null) {
            properties = new PropertiesFile("worldedit.properties");
        } else {
            properties.load();
        }

        allowedBlocks = properties.getString("allowed-blocks", DEFAULT_ALLOWED_BLOCKS).split(",");
        mapScriptCommands = properties.getBoolean("map-script-commands", true);
        defaultMaxBlocksChanged = 
                Math.max(-1, properties.getInt("max-blocks-changed", -1));

        etc controller = etc.getInstance();

        for (Map.Entry<String,String> entry : commands.entrySet()) {
            controller.addCommand(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Disables the plugin.
     */
    public void disable() {
        etc controller = etc.getInstance();

        for (String key : commands.keySet()) {
            controller.removeCommand(key);
        }

        sessions.clear();
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
    public boolean onBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        if (itemInHand == 271) { // Wooden axe
            if (!player.canUseCommand("/editpos1")
                    || !player.canUseCommand("/editpos2")) {
                return false;
            }
            
            WorldEditSession session = getSession(player);

            int x = (int)Math.floor(blockClicked.getX());
            int y = (int)Math.floor(blockClicked.getY());
            int z = (int)Math.floor(blockClicked.getZ());

            if (session.isToolControlEnabled()) {
                try {
                    if (session.hasToolBeenDoubleClicked()
                            && x == session.getPos1()[0]
                            && y == session.getPos1()[1]
                            && z == session.getPos1()[2]) { // Pos 2
                        session.setPos2(x, y, z);
                        session.setPos1(session.getLastToolPos1());
                        player.sendMessage(Colors.LightPurple + "Second edit position set; first one restored.");
                    } else {
                        // Have to remember the original position because on
                        // double click, we are going to restore it
                        try {
                            session.setLastToolPos1(session.getPos1());
                        } catch (IncompleteRegionException e) {}
                        
                        session.setPos1(x, y, z);
                        player.sendMessage(Colors.LightPurple + "First edit position set.");
                    }
                } catch (IncompleteRegionException e) {}

                session.triggerToolClick();

                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param player
     * @param split
     * @return
     */
    @Override
    public boolean onCommand(Player player, String[] split) {
        try {
            if (commands.containsKey(split[0])) {
                if (player.canUseCommand(split[0])) {
                    WorldEditSession session = getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();
                    
                    try {
                        return performCommand(player, session, editSession, split);
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();
                    }
                }
            } else {
                // See if there is a script by the same name
                if (mapScriptCommands && player.canUseCommand("/editscript")) {
                    WorldEditSession session = getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();

                    String filename = split[0].substring(1) + ".js";
                    String[] args = new String[split.length - 1];
                    System.arraycopy(split, 1, args, 0, split.length - 1);

                    try {
                        return runScript(player, session, editSession, filename, args);
                    } catch (NoSuchScriptException nse) {
                        return false;
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();
                    }
                }
            }

            return false;
        } catch (NumberFormatException e) {
            player.sendMessage(Colors.Rose + "Number expected; string given.");
        } catch (IncompleteRegionException e2) {
            player.sendMessage(Colors.Rose + "The edit region has not been fully defined.");
        } catch (UnknownItemException e3) {
            player.sendMessage(Colors.Rose + "Unknown item.");
        } catch (DisallowedItemException e4) {
            player.sendMessage(Colors.Rose + "Disallowed item.");
        } catch (MaxChangedBlocksException e5) {
            player.sendMessage(Colors.Rose + "The maximum number of blocks changed ("
                    + e5.getBlockLimit() + ") in an instance was reached.");
        } catch (InsufficientArgumentsException e6) {
            player.sendMessage(Colors.Rose + e6.getMessage());
        } catch (WorldEditException e7) {
            player.sendMessage(Colors.Rose + e7.getMessage());
        }

        return true;
    }

    /**
     * The main meat of command processing.
     * 
     * @param player
     * @param session
     * @param editSession
     * @param split
     * @return
     * @throws UnknownItemException
     * @throws IncompleteRegionException
     * @throws InsufficientArgumentsException
     * @throws DisallowedItemException
     */
    private boolean performCommand(Player player, WorldEditSession session,
            EditSession editSession, String[] split)
            throws WorldEditException
    {
        // Set edit position #1
        if (split[0].equalsIgnoreCase("/editpos1")) {
            session.setPos1((int)Math.floor(player.getX()),
                            (int)Math.floor(player.getY()),
                            (int)Math.floor(player.getZ()));
            player.sendMessage(Colors.LightPurple + "First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("/editpos2")) {
            session.setPos2((int)Math.floor(player.getX()),
                            (int)Math.floor(player.getY()),
                            (int)Math.floor(player.getZ()));
            player.sendMessage(Colors.LightPurple + "Second edit position set.");
            return true;

        // Set max number of blocks to change at a time
        } else if (split[0].equalsIgnoreCase("/editlimit")) {
            checkArgs(split, 1, 1, "/editlimit");
            int limit = Math.max(-1, Integer.parseInt(split[1]));
            session.setBlockChangeLimit(limit);
            player.sendMessage(Colors.LightPurple + "Block change limit set to "
                    + limit + ".");
            return true;

        // Undo
        } else if (split[0].equalsIgnoreCase("/editundo")) {
            if (session.undo()) {
                player.sendMessage(Colors.LightPurple + "Undo successful.");
            } else {
                player.sendMessage(Colors.Rose + "Nothing to undo.");
            }
            return true;

        // Redo
        } else if (split[0].equalsIgnoreCase("/editredo")) {
            if (session.redo()) {
                player.sendMessage(Colors.LightPurple + "Redo successful.");
            } else {
                player.sendMessage(Colors.Rose + "Nothing to redo.");
            }
            return true;

        // Clear undo history
        } else if (split[0].equalsIgnoreCase("/clearhistory")) {
            session.clearHistory();
            player.sendMessage(Colors.LightPurple + "History cleared.");
            return true;

        // Clear clipboard
        } else if (split[0].equalsIgnoreCase("/clearclipboard")) {
            session.setClipboard(null);
            player.sendMessage(Colors.LightPurple + "Clipboard cleared.");
            return true;

        // Paste
        } else if (split[0].equalsIgnoreCase("/editpasteair") ||
                   split[0].equalsIgnoreCase("/editpaste")) {
            if (session.getClipboard() == null) {
                player.sendMessage(Colors.Rose + "Nothing is in your clipboard.");
            } else {
                Point<Integer> pos = new Point<Integer>((int)Math.floor(player.getX()),
                                                        (int)Math.floor(player.getY()),
                                                        (int)Math.floor(player.getZ()));
                session.getClipboard().paste(editSession, pos,
                    split[0].equalsIgnoreCase("/editpaste"));
                teleportToStandPosition(player);
                logger.log(Level.INFO, player.getName() + " used " + split[0]);
                player.sendMessage(Colors.LightPurple + "Pasted. Undo with /editundo");
            }

            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("/editfill")) {
            checkArgs(split, 2, 3, "/editfill");
            int blockType = getItem(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            int depth = split.length > 3 ? Math.max(1, Integer.parseInt(split[3])) : 1;

            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int minY = Math.max(-128, cy - depth);

            int affected = fill(editSession, cx, cz, cx, cy, cz,
                                blockType, radius, minY);

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1]) - 1) : 0;
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 127;

            int affected = 0;
            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int maxY = Math.min(127, cy + height - 1);

            for (int x = cx - size; x <= cx + size; x++) {
                for (int z = cz - size; z <= cz + size; z++) {
                    for (int y = cy; y <= maxY; y++) {
                        if (editSession.getBlock(x, y, z) != 0) {
                            editSession.setBlock(x, y, z, 0);
                            affected++;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been removed.");

            return true;

        // Remove blocks below current position
        } else if (split[0].equalsIgnoreCase("/removebelow")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1]) - 1) : 0;
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 127;

            int affected = 0;
            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int minY = Math.max(0, cy - height + 1);

            for (int x = cx - size; x <= cx + size; x++) {
                for (int z = cz - size; z <= cz + size; z++) {
                    for (int y = cy; y >= minY; y--) {
                        if (editSession.getBlock(x, y, z) != 0) {
                            editSession.setBlock(x, y, z, 0);
                            affected++;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been removed.");

            return true;

        // Make a cylinder
        } else if (split[0].equalsIgnoreCase("/editcyl")) {
            checkArgs(split, 2, 3, "/editcyl");
            int blockType = getItem(split[1]);
            int radius = Math.abs(Integer.parseInt(split[2]));
            int height = split.length > 3 ? getItem(split[3], true) : 1;

            // We don't want to pass beyond boundaries
            int cx = (int)Math.floor(player.getX());
            int cy = (int)Math.floor(player.getY());
            int cz = (int)Math.floor(player.getZ());
            int maxY = Math.min(127, cy + height);

            int affected = 0;

            for (int x = 0; x <= radius; x++) {
                int z = (int)(Math.sqrt(radius - x * x) + 0.5);
                for (int y = cy; y <= maxY; y++) {
                    for (int x2 = cx - x; x2 <= cx + x; x2++) {
                        editSession.setBlock(x2, y, cz + z, blockType);
                        affected++;
                    }
                }
            }

            teleportToStandPosition(player);

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Load .schematic to clipboard
        } else if (split[0].equalsIgnoreCase("/editload")) {
            checkArgs(split, 1, 1, "/editload");
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.sendMessage(Colors.Rose + "Schematic could not read or it does not exist.");
                } else {
                    int cx = (int)Math.floor(player.getX());
                    int cy = (int)Math.floor(player.getY());
                    int cz = (int)Math.floor(player.getZ());
                    Point<Integer> origin = new Point<Integer>(cx, cy, cz);
                    session.setClipboard(RegionClipboard.loadSchematic(filePath, origin));
                    logger.log(Level.INFO, player.getName() + " loaded " + filePath);
                    player.sendMessage(Colors.LightPurple + filename + " loaded.");
                }
            } catch (SchematicException e) {
                player.sendMessage(Colors.Rose + "Load error: " + e.getMessage());
            } catch (IOException e) {
                player.sendMessage(Colors.Rose + "Schematic could not read or it does not exist.");
            }

            return true;

        // Save clipboard to .schematic
        } else if (split[0].equalsIgnoreCase("/editsave")) {
            if (session.getClipboard() == null) {
                player.sendMessage(Colors.Rose + "Nothing is in your clipboard.");
                return true;
            }
            
            checkArgs(split, 1, 1, "/editsave");
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    player.sendMessage(Colors.Rose + "A schematics/ folder could not be created.");
                    return true;
                }
            }

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.sendMessage(Colors.Rose + "Invalid path for Schematic.");
                } else {
                    // Create parent directories
                    File parent = f.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    session.getClipboard().saveSchematic(filePath);
                    logger.log(Level.INFO, player.getName() + " saved " + filePath);
                    player.sendMessage(Colors.LightPurple + filename + " saved.");
                }
            } catch (SchematicException se) {
                player.sendMessage(Colors.Rose + "Save error: " + se.getMessage());
            } catch (IOException e) {
                player.sendMessage(Colors.Rose + "Schematic could not written.");
            }
            
            return true;

        // Run a script
        } else if (split[0].equalsIgnoreCase("/editscript")) {
            checkArgs(split, 1, -1, "/editscript");
            String filename = split[1].replace("\0", "") + ".js";
            String[] args = new String[split.length - 2];
            System.arraycopy(split, 2, args, 0, split.length - 2);
            try {
                runScript(player, session, editSession, filename, args);
            } catch (NoSuchScriptException e) {
                player.sendMessage(Colors.Rose + "Script file does not exist.");
            }
            return true;
        }

        int lowerX = session.getLowerX();
        int upperX = session.getUpperX();
        int lowerY = session.getLowerY();
        int upperY = session.getUpperY();
        int lowerZ = session.getLowerZ();
        int upperZ = session.getUpperZ();
        
        // Get size of area
        if (split[0].equalsIgnoreCase("/editsize")) {
            player.sendMessage(Colors.LightPurple + "# of blocks: " + getSession(player).getSize());
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editset")) {
            checkArgs(split, 1, 1, "/editset");
            int blockType = getItem(split[1]);
            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        editSession.setBlock(x, y, z, blockType);
                        affected++;
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Set the outline of a region
        } else if(split[0].equalsIgnoreCase("/editoutline")) {
            checkArgs(split, 1, 1, "/editoutline");
            int blockType = getItem(split[1]);
            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    editSession.setBlock(x, y, lowerZ, blockType);
                    editSession.setBlock(x, y, upperZ, blockType);
                    affected++;
                }
            }

            for (int y = lowerY; y <= upperY; y++) {
                for (int z = lowerZ; z <= upperZ; z++) {
                    editSession.setBlock(lowerX, y, z, blockType);
                    editSession.setBlock(upperX, y, z, blockType);
                    affected++;
                }
            }

            for (int z = lowerZ; z <= upperZ; z++) {
                for (int x = lowerX; x <= upperX; x++) {
                    editSession.setBlock(x, lowerY, z, blockType);
                    editSession.setBlock(x, upperY, z, blockType);
                    affected++;
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been set.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editreplace")) {
            checkArgs(split, 1, 2, "/editreplace");
            int blockType = getItem(split[1]);
            int replaceType = split.length > 2 ? getItem(split[2], true) : -1;

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int y = lowerY; y <= upperY; y++) {
                    for (int z = lowerZ; z <= upperZ; z++) {
                        if ((replaceType == -1 && editSession.getBlock(x, y, z) != 0) ||
                            (editSession.getBlock(x, y, z) == replaceType)) {
                            editSession.setBlock(x, y, z, blockType);
                            affected++;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("/editoverlay")) {
            checkArgs(split, 1, 1, "/editoverlay");
            int blockType = getItem(split[1]);

            // We don't want to pass beyond boundaries
            upperY = Math.min(127, upperY + 1);
            lowerY = Math.max(-128, lowerY - 1);

            int affected = 0;

            for (int x = lowerX; x <= upperX; x++) {
                for (int z = lowerZ; z <= upperZ; z++) {
                    for (int y = upperY; y >= lowerY; y--) {
                        if (y + 1 <= 127 && editSession.getBlock(x, y, z) != 0 && editSession.getBlock(x, y + 1, z) == 0) {
                            editSession.setBlock(x, y + 1, z, blockType);
                            affected++;
                            break;
                        }
                    }
                }
            }

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + affected + " block(s) have been overlayed.");

            return true;

        // Copy
        } else if (split[0].equalsIgnoreCase("/editcopy")) {
            Point<Integer> min = new Point<Integer>(lowerX, lowerY, lowerZ);
            Point<Integer> max = new Point<Integer>(upperX, upperY, upperZ);
            Point<Integer> pos = new Point<Integer>((int)Math.floor(player.getX()),
                                                    (int)Math.floor(player.getY()),
                                                    (int)Math.floor(player.getZ()));

            RegionClipboard clipboard = new RegionClipboard(min, max, pos);
            clipboard.copy(editSession);
            session.setClipboard(clipboard);

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + "Block(s) copied.");

            return true;

        // Stack
        } else if (split[0].equalsIgnoreCase("/editstackair") ||
                   split[0].equalsIgnoreCase("/editstack")) {
            checkArgs(split, 1, 2, split[0]);
            String dir = split[1];
            int count = Math.max(1, Integer.parseInt(split[2]));
            int xm = 0;
            int ym = 0;
            int zm = 0;
            boolean copyAir = split[0].equalsIgnoreCase("/editstackair");

            if (dir.equalsIgnoreCase("me")) {
                dir = etc.getCompassPointForDirection(player.getRotation());
            }

            if (dir.equalsIgnoreCase("w")) {
                zm += 1;
            } else if (dir.equalsIgnoreCase("e")) {
                zm -= 1;
            } else if (dir.equalsIgnoreCase("s")) {
                xm += 1;
            } else if (dir.equalsIgnoreCase("n")) {
                xm -= 1;
            } else if (dir.equalsIgnoreCase("u")) {
                ym += 1;
            } else if (dir.equalsIgnoreCase("d")) {
                ym -= 1;
            } else {
                player.sendMessage(Colors.Rose + "Unknown direction: " + dir);
                return true;
            }

            int xs = session.getWidth();
            int ys = session.getHeight();
            int zs = session.getLength();

            int affected = 0;
            
            for (int x = lowerX; x <= upperX; x++) {
                for (int z = lowerZ; z <= upperZ; z++) {
                    for (int y = lowerY; y <= lowerY; y++) {
                        int blockType = editSession.getBlock(x, y, z);

                        if (blockType != 0 || copyAir) {
                            for (int i = 1; i <= count; i++) {
                                editSession.setBlock(x + xs * xm * i, y + ys * ym * i,
                                        z + zs * zm * i, blockType);
                                affected++;
                            }
                        }
                    }
                }
            }

            int shiftX = xs * xm * count;
            int shiftY = ys * ym * count;
            int shiftZ = zs * zm * count;

            int[] pos1 = session.getPos1();
            int[] pos2 = session.getPos2();
            session.setPos1(pos1[0] + shiftX, pos1[1] + shiftY, pos1[2] + shiftZ);
            session.setPos2(pos2[0] + shiftX, pos2[1] + shiftY, pos2[2] + shiftZ);

            logger.log(Level.INFO, player.getName() + " used " + split[0]);
            player.sendMessage(Colors.LightPurple + "Stacked. Undo with /editundo");

            return true;
        }

        return false;
    }

    /**
     * Fills an area recursively in the X/Z directions.
     * 
     * @param editSession
     * @param x
     * @param z
     * @param cx
     * @param cy
     * @param cz
     * @param blockType
     * @param radius
     * @param minY
     * @return
     */
    private int fill(EditSession editSession, int x, int z, int cx, int cy,
            int cz, int blockType, int radius, int minY)
            throws MaxChangedBlocksException {
        double dist = Math.sqrt(Math.pow(cx - x, 2) + Math.pow(cz - z, 2));
        int affected = 0;
        
        if (dist > radius) {
            return 0;
        }

        if (editSession.getBlock(x, cy, z) == 0) {
            affected = fillY(editSession, x, cy, z, blockType, minY);
        } else {
            return 0;
        }
        
        affected += fill(editSession, x + 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(editSession, x - 1, z, cx, cy, cz, blockType, radius, minY);
        affected += fill(editSession, x, z + 1, cx, cy, cz, blockType, radius, minY);
        affected += fill(editSession, x, z - 1, cx, cy, cz, blockType, radius, minY);

        return affected;
    }

    /**
     * Recursively fills a block and below until it hits another block.
     * 
     * @param editSession
     * @param x
     * @param cy
     * @param z
     * @param blockType
     * @param minY
     * @throws MaxChangedBlocksException
     * @return
     */
    private int fillY(EditSession editSession, int x, int cy,
        int z, int blockType, int minY)
        throws MaxChangedBlocksException {
        int affected = 0;
        
        for (int y = cy; y > minY; y--) {
            if (editSession.getBlock(x, y, z) == 0) {
                editSession.setBlock(x, y, z, blockType);
                affected++;
            } else {
                break;
            }
        }

        return affected;
    }

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     * 
     * @param player
     */
    private void teleportToStandPosition(Player player) {
        int x = (int)Math.floor(player.getX());
        int y = (int)Math.floor(player.getY());
        int origY = y;
        int z = (int)Math.floor(player.getZ());
        
        byte free = 0;

        while (y <= 129) {
            if (getBlock(x, y, z) == 0) {
                free++;
            } else {
                free = 0;
            }

            if (free == 2) {
                if (y - 1 != origY) {
                    Location loc = new Location();
                    loc.x = x + 0.5;
                    loc.y = y - 1;
                    loc.z = z + 0.5;
                    loc.rotX = player.getRotation();
                    loc.rotY = player.getPitch();
                    player.teleportTo(loc);
                    return;
                }
            }

            y++;
        }
    }

    /**
     * Execute a script.
     *
     * @param player
     * @param filename
     * @param args
     * @return Whether the file was attempted execution
     */
    private boolean runScript(Player player, WorldEditSession session,
            EditSession editSession, String filename, String[] args) throws
            NoSuchScriptException {
        File dir = new File("editscripts");
        File f = new File("editscripts", filename);

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                throw new NoSuchScriptException();
            } else if (!f.exists()) {
                throw new NoSuchScriptException();
            } else {                
                // Read file
                StringBuffer buffer = new StringBuffer();
                FileInputStream stream = new FileInputStream(f);
                BufferedReader in = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                int c;
                while ((c = in.read()) > -1) {
                    buffer.append((char)c);
                }
                in.close();
                String code = buffer.toString();

                // Evaluate
                ScriptContextFactory factory = new ScriptContextFactory();
                Context cx = factory.enterContext();
                try {                    
                    ScriptableObject scope = cx.initStandardObjects();

                    // Add args
                    ScriptableObject.putProperty(scope, "args",
                        Context.javaToJS(args, scope));

                    // Add context
                    ScriptPlayer scriptPlayer = new ScriptPlayer(player);
                    ScriptContext context = new ScriptContext(
                        scriptPlayer);
                    ScriptableObject.putProperty(scope, "context",
                        Context.javaToJS(context, scope));
                    ScriptableObject.putProperty(scope, "player",
                        Context.javaToJS(scriptPlayer, scope));

                    // Add Minecraft context
                    ScriptMinecraftContext minecraft =
                        new ScriptMinecraftContext(editSession);
                    ScriptableObject.putProperty(scope, "minecraft",
                        Context.javaToJS(minecraft, scope));

                    logger.log(Level.INFO, player.getName() + ": executing " + filename + "...");
                    cx.evaluateString(scope, code, filename, 1, null);
                    logger.log(Level.INFO, player.getName() + ": script " + filename + " executed successfully.");
                    player.sendMessage(Colors.LightPurple + filename + " executed successfully.");
                } catch (RhinoException re) {
                    player.sendMessage(Colors.Rose + filename + ": JS error: " + re.getMessage());
                    re.printStackTrace();
                } catch (Error err) {
                    player.sendMessage(Colors.Rose + filename + ": execution error: " + err.getMessage());
                } finally {
                    Context.exit();
                }
            }

            return true;
        } catch (IOException e) {
            player.sendMessage(Colors.Rose + "Script could not read or it does not exist.");
        }

        return false;
    }

    /**
     * Gets the block type at a position x, y, z. Use an instance of
     * EditSession if possible.
     *
     * @param x
     * @param y
     * @param z
     * @return Block type
     */
    public int getBlock(int x, int y, int z) {
        return etc.getMCServer().e.a(x, y, z);
    }
}
