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

import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;

/**
 * Plugin base.
 *
 * @author sk89q
 */
public class WorldEdit {
    /**
     * WorldEdit instance.
     */
    private static WorldEdit instance;
    /**
     * Server interface.
     */
    private ServerInterface server;
    
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
     * Set up an instance.
     * 
     * @param server
     * @return
     */
    public static WorldEdit setup(ServerInterface server) {
        WorldEdit worldEdit = new WorldEdit();
        worldEdit.server = server;
        instance = worldEdit;
        return worldEdit;
    }

    /**
     * Get WorldEdit instance.
     * 
     * @return
     */
    public static WorldEdit getInstance() {
        return instance;
    }

    /**
     * Get server interface.
     * 
     * @return
     */
    public static ServerInterface getServer() {
        return instance.server;
    }

    /**
     * Construct an instance of the plugin.
     */
    private WorldEdit() {
        commands.put("//pos1", "Set editing position #1");
        commands.put("//pos2", "Set editing position #2");
        commands.put("/toggleplace", "Toggle placing at pos #1");
        commands.put("//wand", "Gives you the \"edit wand\"");
        commands.put("/toggleeditwand", "Toggles edit wand selection");
        commands.put("/,", "Toggles super pick axe.");
        commands.put("//undo", "Undo");
        commands.put("//redo", "Redo");
        commands.put("/clearhistory", "Clear history");
        commands.put("/clearclipboard", "Clear clipboard");
        commands.put("//size", "Get size of selected region");
        commands.put("//set", "[ID] - Set all blocks inside region");
        commands.put("//outline", "[ID] - Outline the region with blocks");
        commands.put("//walls", "[ID] - Build walls");
        commands.put("//replace", "<FromID> [ToID] - Replace all existing blocks inside region");
        commands.put("//overlay", "[ID] - Overlay the area one layer");
        commands.put("/removeabove", "<Size> <Height> - Remove blocks above head");
        commands.put("/removebelow", "<Size> <Height> - Remove blocks below position");
        commands.put("//copy", "Copies the currently selected region");
        commands.put("//cut", "Cuts the currently selected region");
        commands.put("//paste", "Pastes the clipboard");
        commands.put("//pasteair", "Pastes the clipboard (with air)");
        commands.put("//stack", "<Count> <Dir> - Stacks the selection");
        commands.put("//stackair", "<Count> <Dir> - Stacks the selection (with air)");
        commands.put("//load", "[Filename] - Load .schematic into clipboard");
        commands.put("//save", "[Filename] - Save clipboard to .schematic");
        commands.put("//fill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("//drain", "[Radius] - Drain nearby water/lava pools");
        commands.put("//limit", "[Num] - See documentation");
        commands.put("//expand", "<Dir> [Num] - Expands the selection");
        commands.put("//contract", "<Dir> [Num] - Contracts the selection");
        commands.put("//rotate", "[Angle] - Rotate the clipboard");
        commands.put("//hcyl", "[ID] [Radius] <Height> - Create a vertical hollow cylinder");
        commands.put("//cyl", "[ID] [Radius] <Height> - Create a vertical cylinder");
        commands.put("//sphere", "[ID] [Radius] [Raised?] - Create a sphere");
        commands.put("//hsphere", "[ID] [Radius] [Raised?] - Create a hollow sphere");
        commands.put("/fixwater", "[Radius] - Level nearby pools of water");
        commands.put("/forestgen", "<Size> - Make an ugly pine tree forest");
        commands.put("/unstuck", "Go up to the first free spot");
        commands.put("/ascend", "Go up one level");
        commands.put("/descend", "Go down one level");
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
     * @param arg
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(String arg, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        BlockType blockType;
        arg = arg.replace("_", " ");
        String[] args0 = arg.split("\\|");
        String[] args1 = args0[0].split(":", 2);
        String testID = args1[0];
        
        int data;
        
        try {
            data = args1.length > 1 ? Integer.parseInt(args1[1]) : 0;
            if (data > 15 || data < 0) {
                data = 0;
            }
        } catch (NumberFormatException e) {
            data = 0;
        }

        try {
            blockType = BlockType.fromID(Integer.parseInt(testID));
        } catch (NumberFormatException e) {
            blockType = BlockType.lookup(testID);
        }

        if (blockType == null) {
            throw new UnknownItemException();
        }

        // Check if the item is allowed
        if (allAllowed || allowedBlocks.isEmpty()
                || allowedBlocks.contains(blockType.getID())) {
            if (blockType == BlockType.SIGN_POST
                    || blockType == BlockType.WALL_SIGN) {
                String[] text = new String[4];
                text[0] = args0.length > 1 ? args0[1] : "";
                text[1] = args0.length > 2 ? args0[2] : "";
                text[2] = args0.length > 3 ? args0[3] : "";
                text[3] = args0.length > 4 ? args0[4] : "";
                return new SignBlock(blockType.getID(), data, text);
            }
            return new BaseBlock(blockType.getID(), data);
        }

        throw new DisallowedItemException();
    }

    /**
     * Get a block.
     *
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(String id) throws UnknownItemException,
                                          DisallowedItemException {
        return getBlock(id, false);
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

        // Ascend a level
        } else if(split[0].equalsIgnoreCase("/ascend")) {
            checkArgs(split, 0, 0, split[0]);
            if (player.ascendLevel()) {
                player.print("Ascended a level.");
            } else {
                player.printError("No free spot above you found.");
            }
            return true;

        // Descend a level
        } else if(split[0].equalsIgnoreCase("/descend")) {
            checkArgs(split, 0, 0, split[0]);
            if (player.descendLevel()) {
                player.print("Descended a level.");
            } else {
                player.printError("No free spot below you found.");
            }
            return true;

        // Set edit position #1
        } else if (split[0].equalsIgnoreCase("//pos1")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos1(player.getBlockIn());
            player.print("First edit position set.");
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("//pos2")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos2(player.getBlockIn());
            player.print("Second edit position set.");
            return true;

        // Edit wand
        } else if (split[0].equalsIgnoreCase("//wand")) {
            checkArgs(split, 0, 0, split[0]);
            player.giveItem(271, 1);
            player.print("Right click = sel. pos 1; double right click = sel. pos 2");
            return true;

        // Toggle placing at pos #1
        } else if (split[0].equalsIgnoreCase("/toggleplace")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.togglePlacementPosition()) {
                player.print("Now placing at pos #1.");
            } else {
                player.print("Now placing at the block you stand in.");
            }
            return true;

        // Toggle edit wand
        } else if (split[0].equalsIgnoreCase("/toggleeditwand")) {
            checkArgs(split, 0, 0, split[0]);
            session.setToolControl(!session.isToolControlEnabled());
            if (session.isToolControlEnabled()) {
                player.print("Edit wand enabled.");
            } else {
                player.print("Edit wand disabled.");
            }
            return true;

        // Toggle super pick axe
        } else if (split[0].equalsIgnoreCase("/,")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.toggleSuperPickAxe()) {
                player.print("Super pick axe enabled.");
            } else {
                player.print("Super pick axe disabled.");
            }
            return true;

        // Set max number of blocks to change at a time
        } else if (split[0].equalsIgnoreCase("//limit")) {
            checkArgs(split, 1, 1, split[0]);
            int limit = Math.max(-1, Integer.parseInt(split[1]));
            session.setBlockChangeLimit(limit);
            player.print("Block change limit set to " + limit + ".");
            return true;

        // Undo
        } else if (split[0].equalsIgnoreCase("//undo")) {
            checkArgs(split, 0, 0, split[0]);
            if (session.undo()) {
                player.print("Undo successful.");
            } else {
                player.printError("Nothing to undo.");
            }
            return true;

        // Redo
        } else if (split[0].equalsIgnoreCase("//redo")) {
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
        } else if (split[0].equalsIgnoreCase("//pasteair") ||
                   split[0].equalsIgnoreCase("//paste")) {
            Vector pos = session.getPlacementPosition(player);
            session.getClipboard().paste(editSession, pos,
                split[0].equalsIgnoreCase("//paste"));
            player.findFreePosition();
            player.print("Pasted. Undo with //undo");

            return true;

        // Draw a hollow cylinder
        } else if (split[0].equalsIgnoreCase("//hcyl")
                || split[0].equalsIgnoreCase("//cyl")) {
            checkArgs(split, 2, 3, split[0]);
            BaseBlock block = getBlock(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            int height = split.length > 3 ? Integer.parseInt(split[3]) : 1;
            boolean filled = split[0].equalsIgnoreCase("//cyl");

            Vector pos = session.getPlacementPosition(player);
            int affected;
            if (filled) {
                affected = editSession.makeCylinder(pos, block, radius, height);
            } else {
                affected = editSession.makeHollowCylinder(pos, block, radius, height);
            }
            player.print(affected + " block(s) have been created.");

            return true;

        // Draw a sphere
        } else if (split[0].equalsIgnoreCase("//sphere")
                || split[0].equalsIgnoreCase("//hsphere")) {
            checkArgs(split, 2, 3, split[0]);
            BaseBlock block = getBlock(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            boolean raised = split.length > 3
                    ? (split[3].equalsIgnoreCase("true")
                            || split[3].equalsIgnoreCase("yes"))
                    : false;
            boolean filled = split[0].equalsIgnoreCase("//sphere");

            Vector pos = session.getPlacementPosition(player);
            if (raised) {
                pos = pos.add(0, radius, 0);
            }

            int affected = editSession.makeSphere(pos, block, radius, filled);
            player.findFreePosition();
            player.print(affected + " block(s) have been created.");

            return true;

        // Fill a hole
        } else if (split[0].equalsIgnoreCase("//fill")) {
            checkArgs(split, 2, 3, split[0]);
            BaseBlock block = getBlock(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            int depth = split.length > 3 ? Math.max(1, Integer.parseInt(split[3])) : 1;

            Vector pos = session.getPlacementPosition(player);
            int affected = editSession.fillXZ((int)pos.getX(), (int)pos.getZ(),
                    pos, block, radius, depth);
            player.print(affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.min(128, Integer.parseInt(split[2]) + 2) : 128;

            int affected = editSession.removeAbove(
                    session.getPlacementPosition(player), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Remove blocks below current position
        } else if (split[0].equalsIgnoreCase("/removebelow")) {
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 128;

            int affected = editSession.removeBelow(
                    session.getPlacementPosition(player), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Load .schematic to clipboard
        } else if (split[0].equalsIgnoreCase("//load")) {
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
                    session.setClipboard(CuboidClipboard.loadSchematic(filePath));
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
        } else if (split[0].equalsIgnoreCase("//save")) {
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
        } else if (split[0].equalsIgnoreCase("//size")) {
            player.print("# of blocks: " + session.getRegion().getSize());
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("//set")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock block = getBlock(split[1]);
            int affected = editSession.setBlocks(session.getRegion(), block);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Set the outline of a region
        } else if(split[0].equalsIgnoreCase("//outline")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock block = getBlock(split[1]);
            int affected = editSession.makeCuboidFaces(session.getRegion(), block);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Set the walls of a region
        } else if(split[0].equalsIgnoreCase("//walls")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock block = getBlock(split[1]);
            int affected = editSession.makeCuboidWalls(session.getRegion(), block);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Drain pools
        } else if(split[0].equalsIgnoreCase("//drain")) {
            checkArgs(split, 1, 1, split[0]);
            int radius = Math.max(0, Integer.parseInt(split[1]));
            int affected = editSession.drainArea(
                    session.getPlacementPosition(player), radius);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Fix water
        } else if(split[0].equalsIgnoreCase("/fixwater")) {
            checkArgs(split, 1, 1, split[0]);
            int radius = Math.max(0, Integer.parseInt(split[1]));
            int affected = editSession.fixWater(
                    session.getPlacementPosition(player), radius);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("//replace")) {
            checkArgs(split, 1, 2, split[0]);
            int from;
            BaseBlock to;
            if (split.length == 2) {
                from = -1;
                to = getBlock(split[1]);
            } else {
                from = getBlock(split[1]).getID();
                to = getBlock(split[2]);
            }
            
            int affected = editSession.replaceBlocks(session.getRegion(), from, to);
            player.print(affected + " block(s) have been replaced.");

            return true;

        // Lay blocks over an area
        } else if (split[0].equalsIgnoreCase("//overlay")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock block = getBlock(split[1]);

            Region region = session.getRegion();
            int affected = editSession.overlayCuboidBlocks(region, block);
            player.print(affected + " block(s) have been overlayed.");

            return true;

        // Copy
        } else if (split[0].equalsIgnoreCase("//copy")
                || split[0].equalsIgnoreCase("//cut")) {
            boolean cut = split[0].equalsIgnoreCase("//cut");
            BaseBlock block = new BaseBlock(0);

            if (cut) {
                checkArgs(split, 0, 1, split[0]);
                if (split.length > 1) {
                    getBlock(split[1]);
                }
            } else {
                checkArgs(split, 0, 0, split[0]);
            }
                
            Region region = session.getRegion();
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();
            Vector pos = player.getBlockIn();

            CuboidClipboard clipboard = new CuboidClipboard(
                    max.subtract(min).add(new Vector(1, 1, 1)),
                    min, min.subtract(pos));
            clipboard.copy(editSession);
            session.setClipboard(clipboard);

            if (cut) {
                editSession.setBlocks(session.getRegion(), block);
                player.print("Block(s) cut.");
            } else {
                player.print("Block(s) copied.");
            }

            return true;

        // Make pine tree forest
        } else if (split[0].equalsIgnoreCase("/forestgen")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;

            int affected = editSession.makePineTreeForest(player.getPosition(), size);
            player.print(affected + " pine trees created.");

            return true;

        // Stack
        } else if (split[0].equalsIgnoreCase("//stackair") ||
                   split[0].equalsIgnoreCase("//stack")) {
            checkArgs(split, 0, 2, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            Vector dir = getDirection(player,
                    split.length > 2 ? split[2].toLowerCase() : "me");
            boolean copyAir = split[0].equalsIgnoreCase("//stackair");

            int affected = editSession.stackCuboidRegion(session.getRegion(),
                    dir, count, copyAir);
            player.print(affected + " blocks changed. Undo with //undo");

            return true;

        // Expand
        } else if (split[0].equalsIgnoreCase("//expand")) {
            checkArgs(split, 1, 2, split[0]);
            Vector dir;
            int change;
            if (split.length == 3) {
                dir = getDirection(player, split[1].toLowerCase());
                change = Integer.parseInt(split[2]);
            } else {
                dir = getDirection(player, "me");
                change = Integer.parseInt(split[1]);
            }

            Region region = session.getRegion();
            int oldSize = region.getSize();
            region.expand(dir.multiply(change));
            session.learnRegionChanges();
            int newSize = region.getSize();
            player.print("Region expanded " + (newSize - oldSize) + " blocks.");

            return true;

        // Contract
        } else if (split[0].equalsIgnoreCase("//contract")) {
            checkArgs(split, 1, 2, split[0]);
            Vector dir;
            int change;
            if (split.length == 3) {
                dir = getDirection(player, split[1].toLowerCase());
                change = Integer.parseInt(split[2]);
            } else {
                dir = getDirection(player, "me");
                change = Integer.parseInt(split[1]);
            }

            Region region = session.getRegion();
            int oldSize = region.getSize();
            region.contract(dir.multiply(change));
            session.learnRegionChanges();
            int newSize = region.getSize();
            player.print("Region contracted " + (oldSize - newSize) + " blocks.");

            return true;

        // Rotate
        } else if (split[0].equalsIgnoreCase("//rotate")) {
            checkArgs(split, 1, 1, split[0]);
            int angle = Integer.parseInt(split[1]);
            if (angle % 90 == 0) {
                CuboidClipboard clipboard = session.getClipboard();
                clipboard.rotate2D(angle);
                player.print("Clipboard rotated by " + angle + " degrees.");
            } else {
                player.printError("Angles must be divisible by 90 degrees.");
            }

            return true;
        }

        return false;
    }

    /**
     * Get the direction vector for a player's direction. May return
     * null if a direction could not be found.
     * 
     * @param player
     * @param dir
     * @return
     */
    public Vector getDirection(WorldEditPlayer player, String dir)
            throws UnknownDirectionException {
        int xm = 0;
        int ym = 0;
        int zm = 0;

        if (dir.equals("me")) {
            dir = player.getCardinalDirection();
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
            throw new UnknownDirectionException(dir);
        }

        return new Vector(xm, ym, zm);
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
