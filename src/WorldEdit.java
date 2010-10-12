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
import java.util.HashSet;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import com.sk89q.worldedit.*;

/**
 * Plugin base.
 *
 * @author sk89q
 */
public class WorldEdit {
    /**
     * List of default allowed blocks.
     */
    private final static Integer[] DEFAULT_ALLOWED_BLOCKS = {
        0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 35, 41, 42, 43, 44, 45, 47, 48, 49, 52, 53, 54, 56, 57, 58, 60,
        61, 62, 67, 73, 78, 79, 80, 81, 82, 85
    };

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * Stores the WorldEdit sessions of players.
     */
    private HashMap<WorldEditPlayer,WorldEditSession> sessions =
            new HashMap<WorldEditPlayer,WorldEditSession>();
    /**
     * Stores the commands.
     */
    private HashMap<String,String> commands = new HashMap<String,String>();

    /**
     * List of the blocks that can be used. If null, all blocks can be used.
     */
    private HashSet<Integer> allowedBlocks;
    /**
     * Default block change limit. -1 for no limit.
     */
    private int defaultChangeLimit = -1;

    /**
     * Construct an instance of the plugin.
     */
    public WorldEdit() {
        commands.put("/editpos1", "Set editing position #1");
        commands.put("/editpos2", "Set editing position #2");
        commands.put("/editwand", "Gives you the \"edit wand\"");
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
        commands.put("/editstack", "[Dir] <Count> - Stacks the selection");
        commands.put("/editstackair", "[Dir] <Count> - Stacks the selection (with air)");
        commands.put("/editload", "[Filename] - Load .schematic into clipboard");
        commands.put("/editsave", "[Filename] - Save clipboard to .schematic");
        commands.put("/editfill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("/editdrain", "[Radius] - Drain nearby water/lava pools");
        commands.put("/editlimit", "[Num] - See documentation");
        commands.put("/unstuck", "Go up to the first free spot");
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    public WorldEditSession getSession(WorldEditPlayer player) {
        if (sessions.containsKey(player)) {
            return sessions.get(player);
        } else {
            WorldEditSession session = new WorldEditSession();
            session.setBlockChangeLimit(getDefaultChangeLimit());
            sessions.put(player, session);
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
    public int getItem(String id, boolean allAllowed)
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

        // Check if the item is allowed
        if (allAllowed || allowedBlocks.isEmpty() || allowedBlocks.contains(foundID)) {
            return foundID;
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
    public int getItem(String id) throws UnknownItemException,
                                          DisallowedItemException {
        return getItem(id, false);
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
     * The main meat of command processing.
     * 
     * @param player
     * @param editPlayer
     * @param session
     * @param editSession
     * @param split
     * @return
     * @throws UnknownItemException
     * @throws IncompleteRegionException
     * @throws InsufficientArgumentsException
     * @throws DisallowedItemException
     */
    public boolean performCommand(WorldEditPlayer player,
            WorldEditSession session, EditSession editSession, String[] split)
            throws WorldEditException
    {
        // Jump to the first free position
        if (split[0].equalsIgnoreCase("/unstuck")) {
            checkArgs(split, 0, 0, split[0]);
            player.print("There you go!");
            player.findFreePosition();
            return true;

        // Set edit position #1
        } else if (split[0].equalsIgnoreCase("/editpos1")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos1(player.getBlockIn());
            player.print("First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("/editpos2")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos2(player.getBlockIn());
            player.print("Second edit position set.");
            return true;

        // Edit wand
        } else if (split[0].equalsIgnoreCase("/editwand")) {
            checkArgs(split, 0, 0, split[0]);
            player.giveItem(271, 1);
            player.print("Right click = sel. pos 1; double right click = sel. pos 2");
            return true;

        // Set max number of blocks to change at a time
        } else if (split[0].equalsIgnoreCase("/editlimit")) {
            checkArgs(split, 1, 1, split[0]);
            int limit = Math.max(-1, Integer.parseInt(split[1]));
            session.setBlockChangeLimit(limit);
            player.print("Block change limit set to " + limit + ".");
            return true;

        // Undo
        } else if (split[0].equalsIgnoreCase("/editundo")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.undo()) {
                player.print("Undo successful.");
            } else {
                player.printError("Nothing to undo.");
            }
            return true;

        // Redo
        } else if (split[0].equalsIgnoreCase("/editredo")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.redo()) {
                player.print("Redo successful.");
            } else {
                player.printError("Nothing to redo.");
            }
            return true;

        // Clear undo history
        } else if (split[0].equalsIgnoreCase("/clearhistory")) {
            checkArgs(split, 0, 0, split[0]);
            session.clearHistory();
            player.print("History cleared.");
            return true;

        // Clear clipboard
        } else if (split[0].equalsIgnoreCase("/clearclipboard")) {
            checkArgs(split, 0, 0, split[0]);
            session.setClipboard(null);
            player.print("Clipboard cleared.");
            return true;

        // Paste
        } else if (split[0].equalsIgnoreCase("/editpasteair") ||
                   split[0].equalsIgnoreCase("/editpaste")) {
            if (session.getClipboard() == null) {
                player.printError("Nothing is in your clipboard.");
            } else {
                Point pos = player.getBlockIn();
                session.getClipboard().paste(editSession, pos,
                    split[0].equalsIgnoreCase("/editpaste"));
                player.findFreePosition();
                player.print("Pasted. Undo with /editundo");
            }

            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("/editfill")) {
            checkArgs(split, 2, 3, split[0]);
            int blockType = getItem(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            int depth = split.length > 3 ? Math.max(1, Integer.parseInt(split[3])) : 1;

            Point pos = player.getBlockIn();
            int affected = editSession.fillXZ((int)pos.getX(), (int)pos.getZ(),
                    pos, blockType, radius, depth);
            player.print(affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.min(128, Integer.parseInt(split[2]) + 2) : 128;

            int affected = editSession.removeAbove(player.getBlockIn(), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Remove blocks below current position
        } else if (split[0].equalsIgnoreCase("/removebelow")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 128;

            int affected = editSession.removeBelow(player.getBlockIn(), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Load .schematic to clipboard
        } else if (split[0].equalsIgnoreCase("/editload")) {
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.printError("Schematic could not read or it does not exist.");
                } else {
                    Point origin = player.getBlockIn();
                    session.setClipboard(CuboidClipboard.loadSchematic(filePath, origin));
                    logger.log(Level.INFO, player.getName() + " loaded " + filePath);
                    player.print(filename + " loaded.");
                }
            /*} catch (SchematicException e) {
                player.printError("Load error: " + e.getMessage());*/
            } catch (IOException e) {
                player.printError("Schematic could not read or it does not exist.");
            }

            return true;

        // Save clipboard to .schematic
        } else if (split[0].equalsIgnoreCase("/editsave")) {
            if (session.getClipboard() == null) {
                player.printError("Nothing is in your clipboard.");
                return true;
            }
            
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    player.printError("A schematics/ folder could not be created.");
                    return true;
                }
            }

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.printError("Invalid path for Schematic.");
                } else {
                    // Create parent directories
                    File parent = f.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    session.getClipboard().saveSchematic(filePath);
                    logger.log(Level.INFO, player.getName() + " saved " + filePath);
                    player.print(filename + " saved.");
                }
            } catch (SchematicException se) {
                player.printError("Save error: " + se.getMessage());
            } catch (IOException e) {
                player.printError("Schematic could not written.");
            }
            
            return true;

        // Get size
        } else if (split[0].equalsIgnoreCase("/editsize")) {
            player.print("# of blocks: " + session.getRegion().getSize());
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editset")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);
            int affected = editSession.setBlocks(session.getRegion(), blockType);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Set the outline of a region
        } else if(split[0].equalsIgnoreCase("/editoutline")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);
            int affected = editSession.makeCuboidFaces(session.getRegion(), blockType);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Drain pools
        } else if(split[0].equalsIgnoreCase("/editdrain")) {
            checkArgs(split, 1, 1, split[0]);
            int radius = Math.max(0, Integer.parseInt(split[1]));
            int affected = editSession.drainArea(player.getBlockIn(), radius);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/editreplace")) {
            checkArgs(split, 1, 2, split[0]);
            int from, to;
            if (split.length == 2) {
                from = -1;
                to = getItem(split[1]);
            } else {
                from = getItem(split[1]);
                to = getItem(split[2]);
            }
            
            int affected = editSession.replaceBlocks(session.getRegion(), from, to);
            player.print(affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("/editoverlay")) {
            checkArgs(split, 1, 1, split[0]);
            int blockType = getItem(split[1]);

            Region region = session.getRegion();
            int affected = editSession.overlayCuboidBlocks(region, blockType);
            player.print(affected + " block(s) have been overlayed.");

            return true;

        // Copy
        } else if (split[0].equalsIgnoreCase("/editcopy")) {
            checkArgs(split, 0, 0, split[0]);
            Region region = session.getRegion();
            Point min = region.getMinimumPoint();
            Point max = region.getMaximumPoint();
            Point pos = player.getBlockIn();

            CuboidClipboard clipboard = new CuboidClipboard(min, max, pos);
            clipboard.copy(editSession);
            session.setClipboard(clipboard);
            
            player.print("Block(s) copied.");

            return true;

        // Stack
        } else if (split[0].equalsIgnoreCase("/editstackair") ||
                   split[0].equalsIgnoreCase("/editstack")) {
            checkArgs(split, 0, 2, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            String dir = split.length > 2 ? split[2].toLowerCase() : "me";
            int xm = 0;
            int ym = 0;
            int zm = 0;
            boolean copyAir = split[0].equalsIgnoreCase("/editstackair");

            if (dir.equals("me")) {
                // From hey0's code
                double rot = (player.getYaw() - 90) % 360;
                if (rot < 0) {
                    rot += 360.0;
                }
                
                dir = etc.getCompassPointForDirection(rot).toLowerCase();
            }

            if (dir.charAt(0) == 'w') {
                zm += 1;
            } else if (dir.charAt(0) == 'e') {
                zm -= 1;
            } else if (dir.charAt(0) == 's') {
                xm += 1;
            } else if (dir.charAt(0) == 'n') {
                xm -= 1;
            } else if (dir.charAt(0) == 'u') {
                ym += 1;
            } else if (dir.charAt(0) == 'd') {
                ym -= 1;
            } else {
                player.printError("Unknown direction: " + dir);
                return true;
            }

            int affected = editSession.stackCuboidRegion(session.getRegion(),
                    xm, ym, zm, count, copyAir);
            player.print(affected + " blocks changed. Undo with /editundo");

            return true;
        }

        return false;
    }

    /**
     * Remove a session.
     * 
     * @param player
     */
    public void removeSession(WorldEditPlayer player) {
        sessions.remove(player);
    }

    /**
     * Remove all sessions.
     */
    public void clearSessions() {
        sessions.clear();
    }

    /**
     * Get the list of commands.
     *
     * @return List
     */
    public HashMap<String,String> getCommands() {
        return commands;
    }

    /**
     * Set the list of allowed blocks. Provided null to use the default list.
     * 
     * @param allowedBlocks
     */
    public void setAllowedBlocks(HashSet<Integer> allowedBlocks) {
        this.allowedBlocks = allowedBlocks != null ? allowedBlocks
                : new HashSet<Integer>(Arrays.asList(DEFAULT_ALLOWED_BLOCKS));
    }

    /**
     * Get a comma-delimited list of the default allowed blocks.
     * 
     * @return comma-delimited list
     */
    public static String getDefaultAllowedBlocks() {
        StringBuilder b = new StringBuilder();
        for (Integer id : DEFAULT_ALLOWED_BLOCKS) {
            b.append(id).append(",");
        }
        return b.substring(0, b.length() - 1);
    }

    /**
     * @return the defaultChangeLimit
     */
    public int getDefaultChangeLimit() {
        return defaultChangeLimit;
    }

    /**
     * Set the default limit on the number of blocks that can be changed
     * in one operation.
     * 
     * @param defaultChangeLimit the defaultChangeLimit to set
     */
    public void setDefaultChangeLimit(int defaultChangeLimit) {
        this.defaultChangeLimit = defaultChangeLimit;
    }
}
