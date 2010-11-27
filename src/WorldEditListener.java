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
public class WorldEditListener extends PluginListener {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    /**
     * Default list of allowed block types.
     */
    private final static Integer[] DEFAULT_ALLOWED_BLOCKS = {
        0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 35, 41, 42, 43, 44, 45, 47, 48, 49, 52, 53, 54, 56, 57, 58, 60,
        61, 62, 67, 73, 78, 79, 80, 82, 85, 86, 87, 88, 89, 91
    };
    /**
     * WorldEditLibrary's properties file.
     */
    private PropertiesFile properties;
    /**
     * Stores a list of WorldEdit sessions, keyed by players' names. Sessions
     * persist only for the user's session. On disconnect, the session will be
     * removed. Sessions are created only when they are needed and those
     * without any WorldEdit abilities or never use WorldEdit in a session will
     * not have a session object generated for them.
     */
    private HashMap<WorldEditPlayer,WorldEditSession> sessions =
            new HashMap<WorldEditPlayer,WorldEditSession>();
    /**
     * List of commands. These are checked when onCommand() is called, so
     * the list must know about every command. On plugin load, the commands
     * will be loaded into help. On unload, they will be removed.
     */
    private HashMap<String,String> commands = new HashMap<String,String>();

    /**
     * Group restrictions manager.
     */
    private GroupRestrictionsManager restrictions = new GroupRestrictionsManager();

    private boolean profile;
    private HashSet<Integer> allowedBlocks;
    private int defaultChangeLimit = -1;
    private String shellSaveType;
    private SnapshotRepository snapshotRepo;
    private int maxRadius = -1;
    private int maxSuperPickaxeSize = 5;
    private boolean logComands = false;
    private boolean registerHelp = true;
    private int wandItem = 271;

    /**
     * Construct an instance of the plugin.
     */
    public WorldEditListener() {
        // Note: Commands should only have the phrase 'air' at the end
        // for now (see SMWorldEditListener.canUseCommand)
        commands.put("//pos1", "Set editing position #1");
        commands.put("//pos2", "Set editing position #2");
        commands.put("//hpos1", "Trace editing position #1");
        commands.put("//hpos2", "Trace editing position #2");
        commands.put("//chunk", "Select the chunk that you are in");
        commands.put("/toggleplace", "Toggle placing at pos #1");
        commands.put("//wand", "Gives you the \"edit wand\"");
        commands.put("/toggleeditwand", "Toggles edit wand selection");
        commands.put("//", "Toggles super pick axe.");
        commands.put("//undo", "Undo");
        commands.put("//redo", "Redo");
        commands.put("/clearhistory", "Clear history");
        commands.put("/clearclipboard", "Clear clipboard");
        commands.put("//size", "Get size of selected region");
        commands.put("//count", "[BlockIDs] - Count the number of blocks in the region");
        commands.put("//distr", "Get the top block distribution");
        commands.put("//set", "[ID] - Set all blocks inside region");
        commands.put("//outline", "[ID] - Outline the region with blocks");
        commands.put("//walls", "[ID] - Build walls");
        commands.put("//replace", "<FromID> [ToID] - Replace all existing blocks inside region");
        commands.put("/replacenear", "<Size> <FromID> [ToID] - Replace all existing blocks nearby");
        commands.put("//overlay", "[ID] - Overlay the area one layer");
        commands.put("/removeabove", "<Size> <Height> - Remove blocks above head");
        commands.put("/removebelow", "<Size> <Height> - Remove blocks below position");
        commands.put("/removenear", "<ID> <Size> - Remove blocks near you");
        commands.put("//copy", "Copies the currently selected region");
        commands.put("//cut", "Cuts the currently selected region");
        commands.put("//paste", "<AtOrigin?> - Pastes the clipboard");
        commands.put("//pasteair", "<AtOrigin?> - Pastes the clipboard (with air)");
        commands.put("//move", "<Count> <Dir> <LeaveID> - Move the selection");
        commands.put("//moveair", "<Count> <Dir> <LeaveID> - Move the selection (with air)");
        commands.put("//stack", "<Count> <Dir> - Stacks the selection");
        commands.put("//stackair", "<Count> <Dir> - Stacks the selection (with air)");
        commands.put("//load", "[Filename] - Load .schematic into clipboard");
        commands.put("//save", "[Filename] - Save clipboard to .schematic");
        commands.put("//fill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("//fillr", "[ID] [Radius] - Fill a hole fully recursively");
        commands.put("//drain", "[Radius] - Drain nearby water/lava pools");
        commands.put("//limit", "[Num] - See documentation");
        commands.put("//mode", "[Mode] <Size> - Set super pickaxe mode (single/recursive/area)");
        commands.put("//tool", "[Tool] - Set pickaxe tool (none/tree)");
        commands.put("//expand", "[Num] <Dir> - Expands the selection");
        commands.put("//contract", "[Num] <Dir> - Contracts the selection");
        commands.put("//rotate", "[Angle] - Rotate the clipboard");
        commands.put("//hcyl", "[ID] [Radius] <Height> - Create a vertical hollow cylinder");
        commands.put("//cyl", "[ID] [Radius] <Height> - Create a vertical cylinder");
        commands.put("//sphere", "[ID] [Radius] <Raised?> - Create a sphere");
        commands.put("//hsphere", "[ID] [Radius] <Raised?> - Create a hollow sphere");
        commands.put("/fixwater", "[Radius] - Level nearby pools of water");
        commands.put("/fixlava", "[Radius] - Level nearby pools of lava");
        commands.put("/ex", "[Size] - Extinguish fires");
        commands.put("/forestgen", "<Size> <Density> - Make Notch tree forest");
        commands.put("/pinegen", "<Size> <Density> - Make an ugly pine tree forest");
        commands.put("/snow", "<Radius> - Simulate snow cover");
        commands.put("/pumpkins", "<Size> - Make a pumpkin forest");
        commands.put("/unstuck", "Go up to the first free spot");
        commands.put("/ascend", "Go up one level");
        commands.put("/descend", "Go down one level");
        commands.put("/jumpto", "Jump to the block that you are looking at");
        commands.put("/thru", "Go through the wall that you are looking at");
        commands.put("/ceil", "<Clearance> - Get to the ceiling");
        commands.put("/up", "<Distance> - Go up some distance");
        commands.put("/chunkinfo", "Get the filename of the chunk that you are in");
        commands.put("/listchunks", "Print a list of used chunks");
        commands.put("/delchunks", "Generate a shell script to delete chunks");
        commands.put("/listsnapshots", "<Num> - List the 5 newest snapshots");
        commands.put("/butcher", "<Radius> - Kill nearby mobs");
        commands.put("//use", "[SnapshotID] - Use a particular snapshot");
        commands.put("//restore", "<SnapshotID> - Restore a particular snapshot");
        commands.put("//smooth", "<Iterations> - Smooth an area's heightmap");
    }

    /**
     * Gets the WorldEditLibrary session for a player.
     *
     * @param player
     * @return
     */
    public WorldEditSession getSession(WorldEditPlayer player) {
        if (sessions.containsKey(player)) {
            return sessions.get(player);
        } else {
            WorldEditSession session = new WorldEditSession();
            int changeLimit = restrictions.getGreatestChangeLimit(player.getGroups());
            if (changeLimit == -2) {
                changeLimit = defaultChangeLimit;
            }
            session.setBlockChangeLimit(changeLimit);
            sessions.put(player, session);
            return session;
        }
    }

    /**
     * Returns true if the player has a session.
     * 
     * @param player
     * @return
     */
    public boolean hasSession(WorldEditPlayer player) {
        return sessions.containsKey(player);
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
            if (blockType == null) {
                int t = etc.getDataSource().getItem(testID);
                if (t > 0 && t < 256) {
                    blockType = BlockType.fromID(t);
                }
            }
        }

        if (blockType == null) {
            throw new UnknownItemException(arg);
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
            } else if (blockType == BlockType.MOB_SPAWNER) {
                if (!ServerInterface.isValidMobType(args0[1])) {
                    throw new InvalidItemException(arg, "Unknown mob type '" + args0[1] + "'");
                }
                return new MobSpawnerBlock(data, args0[1]);
            }

            return new BaseBlock(blockType.getID(), data);
        }

        throw new DisallowedItemException(arg);
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
     * Get a list of blocks as a set. This returns a Pattern.
     *
     * @param list
     * @return pattern
     */
    public Pattern getBlockPattern(String list)
            throws UnknownItemException, DisallowedItemException {

        String[] items = list.split(",");

        if (items.length == 1) {
            return new SingleBlockPattern(getBlock(items[0]));
        }

        List<BlockChance> blockChances = new ArrayList<BlockChance>();

        for (String s : items) {
            BaseBlock block;
            
            double chance;
            if (s.matches("[0-9]+(?:\\.(?:[0-9]+)?)?%.*")) {
                String[] p = s.split("%");
                chance = Double.parseDouble(p[0]);
                block = getBlock(p[1]);
            } else {
                chance = 1;
                block = getBlock(s);
            }
            
            blockChances.add(new BlockChance(block, chance));
        }

        return new RandomFillPattern(blockChances);
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param list
     * @params allBlocksAllowed
     * @return set
     */
    public Set<Integer> getBlockIDs(String list, boolean allBlocksAllowed)
            throws UnknownItemException, DisallowedItemException {
        String[] items = list.split(",");
        Set<Integer> blocks = new HashSet<Integer>();
        for (String s : items) {
            blocks.add(getBlock(s, allBlocksAllowed).getID());
        }
        return blocks;
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
     * Checks to see if the specified radius is within bounds.
     *
     * @param radius
     * @throws MaxRadiusException
     */
    private void checkMaxRadius(int radius) throws MaxRadiusException {
        if (maxRadius > 0 && radius > maxRadius) {
            throw new MaxRadiusException();
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
        if (logComands) {
            logger.log(Level.INFO, "WorldEdit: " + player.getName() + ": "
                    + joinString(split, " "));
        }

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

        // Jump to the block in sight
        } else if (split[0].equalsIgnoreCase("/jumpto")) {
            checkArgs(split, 0, 0, split[0]);
            Vector pos = player.getSolidBlockTrace(300);
            if (pos != null) {
                player.findFreePosition(pos);
                player.print("Poof!");
            } else {
                player.printError("No block in sight!");
            }
            return true;

        // Go through a wall
        } else if (split[0].equalsIgnoreCase("/thru")) {
            checkArgs(split, 0, 0, split[0]);
            if (player.passThroughForwardWall(6)) {
                player.print("Whoosh!");
            } else {
                player.printError("No free spot ahead of you found.");
            }
            return true;

        // Go to the ceiling
        } else if (split[0].equalsIgnoreCase("/ceil")) {
            checkArgs(split, 0, 1, split[0]);
            int clearence = split.length > 1 ?
                Math.max(0, Integer.parseInt(split[1])) : 0;

            if (player.ascendToCeiling(clearence)) {
                player.print("Whoosh!");
            } else {
                player.printError("No free spot above you found.");
            }
            return true;

        // Go up
        } else if (split[0].equalsIgnoreCase("/up")) {
            checkArgs(split, 1, 1, split[0]);
            int distance = Integer.parseInt(split[1]);

            if (player.ascendUpwards(distance)) {
                player.print("Whoosh!");
            } else {
                player.printError("You would hit something above you.");
            }
            return true;

        // Set edit position #1
        } else if (split[0].equalsIgnoreCase("//pos1")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos1(player.getBlockIn());
            if (session.isRegionDefined()) {
                player.print("First position set to " + player.getBlockIn()
                        + " (" + session.getRegion().getSize() + ").");
            } else {
                player.print("First position set to " + player.getBlockIn() + ".");
            }
            return true;

        // Set edit position #2
        } else if (split[0].equalsIgnoreCase("//pos2")) {
            checkArgs(split, 0, 0, split[0]);
            session.setPos2(player.getBlockIn());
            if (session.isRegionDefined()) {
                player.print("Second position set to " + player.getBlockIn()
                        + " (" + session.getRegion().getSize() + ").");
            } else {
                player.print("Second position set to " + player.getBlockIn() + ".");
            }
            return true;

        // Trace edit position #1
        } else if (split[0].equalsIgnoreCase("//hpos1")) {
            checkArgs(split, 0, 0, split[0]);
            Vector pos = player.getBlockTrace(300);
            if (pos != null) {
                session.setPos1(pos);
                if (session.isRegionDefined()) {
                    player.print("First position set to " + pos
                            + " (" + session.getRegion().getSize() + ").");
                } else {
                    player.print("First position set to " + pos.toString() + " .");
                }
            } else {
                player.printError("No block in sight!");
            }
            return true;

        // Trace edit position #2
        } else if (split[0].equalsIgnoreCase("//hpos2")) {
            checkArgs(split, 0, 0, split[0]);
            Vector pos = player.getBlockTrace(300);
            if (pos != null) {
                session.setPos2(pos);
                if (session.isRegionDefined()) {
                    player.print("Second position set to " + pos
                            + " (" + session.getRegion().getSize() + ").");
                } else {
                    player.print("Second position set to " + pos.toString() + " .");
                }
            } else {
                player.printError("No block in sight!");
            }
            return true;

        // Select the chunk
        } else if(split[0].equalsIgnoreCase("//chunk")) {
            checkArgs(split, 0, 0, split[0]);

            Vector2D min2D = ChunkStore.toChunk(player.getBlockIn());
            Vector min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            Vector max = min.add(15, 127, 15);

            session.setPos1(min);
            session.setPos2(max);

            player.print("Chunk selected: "
                    + min2D.getBlockX() + ", " + min2D.getBlockZ());

            return true;

        // Edit wand
        } else if (split[0].equalsIgnoreCase("//wand")) {
            checkArgs(split, 0, 0, split[0]);
            player.giveItem(wandItem, 1);
            player.print("Left click: select pos #1; Right click: select pos #2");
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
        } else if (split[0].equalsIgnoreCase("//")) {
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
            int allowableMax = restrictions.getGreatestChangeLimit(player.getGroups());
            if (allowableMax >= 0 && (limit == -1 || limit > allowableMax)) {
                player.printError("Your maximum allowable limit is " + allowableMax + ".");
            } else {
                session.setBlockChangeLimit(limit);
                player.print("Block change limit set to " + limit + ".");
            }
            return true;

        // Set super pick axe mode
        } else if (split[0].equalsIgnoreCase("//mode")) {
            checkArgs(split, 1, 2, split[0]);

            if (split[1].equalsIgnoreCase("single")) {
                session.setSuperPickaxeMode(WorldEditSession.SuperPickaxeMode.SINGLE);
                player.print("Mode set to single block.");
            } else if (split[1].equalsIgnoreCase("recursive")
                    || split[1].equalsIgnoreCase("area")) {
                if (split.length == 3) {
                    int size = Math.max(1, Integer.parseInt(split[2]));
                    if (size <= maxSuperPickaxeSize) {
                        WorldEditSession.SuperPickaxeMode mode =
                                split[1].equalsIgnoreCase("recursive") ?
                                    WorldEditSession.SuperPickaxeMode.SAME_TYPE_RECURSIVE :
                                    WorldEditSession.SuperPickaxeMode.SAME_TYPE_AREA;
                        session.setSuperPickaxeMode(mode);
                        session.setSuperPickaxeRange(size);
                        player.print("Mode set to " + split[1].toLowerCase() + ".");
                    } else {
                        player.printError("Max size is " + maxSuperPickaxeSize + ".");
                    }
                } else {
                    player.printError("Size argument required for mode "
                            + split[1].toLowerCase() + ".");
                }
            } else {
                player.printError("Unknown super pick axe mode.");
            }
            return true;

        // Set tool
        } else if (split[0].equalsIgnoreCase("//tool")) {
            checkArgs(split, 1, 1, split[0]);

            if (split[1].equalsIgnoreCase("none")) {
                session.setTool(WorldEditSession.Tool.NONE);
                player.print("No tool equipped. -3 XP, +10 Manliness");
            } else if (split[1].equalsIgnoreCase("tree")) {
                session.setTool(WorldEditSession.Tool.TREE);
                player.print("Tree planting tool equipped. +5 XP");
            } else if (split[1].equalsIgnoreCase("info")) {
                session.setTool(WorldEditSession.Tool.INFO);
                player.print("Block information tool equipped.");
            } else {
                player.printError("Unknown tool.");
            }
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
            checkArgs(split, 0, 1, split[0]);
            boolean atOrigin = split.length > 1
                    ? (split[1].equalsIgnoreCase("true")
                            || split[1].equalsIgnoreCase("yes"))
                    : false;
            if (atOrigin) {
                Vector pos = session.getClipboard().getOrigin();
                session.getClipboard().place(editSession, pos,
                    split[0].equalsIgnoreCase("//paste"));
                player.findFreePosition();
                player.print("Pasted to copy origin. Undo with //undo");
            } else {
                Vector pos = session.getPlacementPosition(player);
                session.getClipboard().paste(editSession, pos,
                    split[0].equalsIgnoreCase("//paste"));
                player.findFreePosition();
                player.print("Pasted relative to you. Undo with //undo");
            }

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
        } else if (split[0].equalsIgnoreCase("//fill")
                || split[0].equalsIgnoreCase("//fillr")) {
            boolean recursive = split[0].equalsIgnoreCase("//fillr");
            checkArgs(split, 2, recursive ? 2 : 3, split[0]);
            Pattern pattern = getBlockPattern(split[1]);
            int radius = Math.max(1, Integer.parseInt(split[2]));
            checkMaxRadius(radius);
            int depth = split.length > 3 ? Math.max(1, Integer.parseInt(split[3])) : 1;

            Vector pos = session.getPlacementPosition(player);
            int affected = 0;
            if (pattern instanceof SingleBlockPattern) {
                affected = editSession.fillXZ(pos,
                        ((SingleBlockPattern)pattern).getBlock(),
                        radius, depth, recursive);
            } else {
                affected = editSession.fillXZ(pos, pattern, radius, depth, recursive);
            }
            player.print(affected + " block(s) have been created.");

            return true;

        // Remove blocks above current position
        } else if (split[0].equalsIgnoreCase("/removeabove")) {
            checkArgs(split, 0, 2, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            checkMaxRadius(size);
            int height = split.length > 2 ? Math.min(128, Integer.parseInt(split[2]) + 2) : 128;

            int affected = editSession.removeAbove(
                    session.getPlacementPosition(player), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Remove blocks below current position
        } else if (split[0].equalsIgnoreCase("/removebelow")) {
            checkArgs(split, 0, 2, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            checkMaxRadius(size);
            int height = split.length > 2 ? Math.max(1, Integer.parseInt(split[2])) : 128;

            int affected = editSession.removeBelow(
                    session.getPlacementPosition(player), size, height);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Remove blocks near
        } else if (split[0].equalsIgnoreCase("/removenear")) {
            checkArgs(split, 2, 2, split[0]);
            BaseBlock block = getBlock(split[1], true);
            int size = Math.max(1, Integer.parseInt(split[2]));
            checkMaxRadius(size);

            int affected = editSession.removeNear(
                    session.getPlacementPosition(player), block.getID(), size);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Extinguish
        } else if (split[0].equalsIgnoreCase("/ex")) {
            checkArgs(split, 0, 1, split[0]);
            int defaultRadius = maxRadius != -1 ? Math.min(40, maxRadius) : 40;
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1]))
                    : defaultRadius;
            checkMaxRadius(size);

            int affected = editSession.removeNear(
                    session.getPlacementPosition(player), 51, size);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Load .schematic to clipboard
        } else if (split[0].equalsIgnoreCase("//load")) {
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";
            File dir = new File("schematics");
            File f = new File("schematics", filename);

            if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
                player.printError("Valid characters: A-Z, a-z, 0-9, spaces, "
                        + "./\'$@~!%^*()[]+{},?");
                return true;
            }

            try {
                String filePath = f.getCanonicalPath();
                String dirPath = dir.getCanonicalPath();

                if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                    player.printError("Schematic could not read or it does not exist.");
                } else {
                    session.setClipboard(CuboidClipboard.loadSchematic(filePath));
                    logger.log(Level.INFO, player.getName() + " loaded " + filePath);
                    player.print(filename + " loaded. Paste it with //paste");
                }
            } catch (DataException e) {
                player.printError("Load error: " + e.getMessage());
            } catch (IOException e) {
                player.printError("Schematic could not read or it does not exist: " + e.getMessage());
            }

            return true;

        // Save clipboard to .schematic
        } else if (split[0].equalsIgnoreCase("//save")) {
            checkArgs(split, 1, 1, split[0]);
            String filename = split[1].replace("\0", "") + ".schematic";

            if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+$")) {
                player.printError("Valid characters: A-Z, a-z, 0-9, spaces, "
                        + "./\'$@~!%^*()[]+{},?");
                return true;
            }
            
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
            } catch (DataException se) {
                player.printError("Save error: " + se.getMessage());
            } catch (IOException e) {
                player.printError("Schematic could not written: " + e.getMessage());
            }
            
            return true;

        // Get size
        } else if (split[0].equalsIgnoreCase("//size")) {
            Region region = session.getRegion();
            Vector size = region.getMaximumPoint()
                    .subtract(region.getMinimumPoint())
                    .add(1, 1, 1);
            player.print("First position: " + session.getPos1());
            player.print("Second position: " + session.getPos2());
            player.print("Size: " + size);
            player.print("# of blocks: " + region.getSize());
            return true;

        // Get count
        } else if (split[0].equalsIgnoreCase("//count")) {
            checkArgs(split, 1, 1, split[0]);
            Set<Integer> searchIDs = getBlockIDs(split[1], true);
            player.print("Counted: " +
                    editSession.countBlocks(session.getRegion(), searchIDs));
            return true;

        // Get block distribution
        } else if (split[0].equalsIgnoreCase("//distr")) {
            checkArgs(split, 0, 0, split[0]);
            List<Countable<Integer>> distribution =
                    editSession.getBlockDistribution(session.getRegion());
            if (distribution.size() > 0) { // *Should* always be true
                int size = session.getRegion().getSize();

                player.print("# total blocks: " + size);
                
                for (Countable<Integer> c : distribution) {
                    player.print(String.format("%-7s (%.3f%%) %s #%d",
                            String.valueOf(c.getAmount()),
                            c.getAmount() / (double)size * 100,
                            BlockType.fromID(c.getID()).getName(), c.getID()));
                }
            } else {
                player.printError("No blocks counted.");
            }
            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("//set")) {
            checkArgs(split, 1, 1, split[0]);
            Pattern pattern = getBlockPattern(split[1]);
            int affected;
            if (pattern instanceof SingleBlockPattern) {
                affected = editSession.setBlocks(session.getRegion(),
                        ((SingleBlockPattern)pattern).getBlock());
            } else {
                affected = editSession.setBlocks(session.getRegion(), pattern);
            }
            player.print(affected + " block(s) have been changed.");

            return true;

        // Smooth the heightmap of a region
        } else if (split[0].equalsIgnoreCase("//smooth")) {
            checkArgs(split, 0, 1, split[0]);

            int iterations = 1;
            if (split.length >= 2)
                iterations = Integer.parseInt(split[1]);

            HeightMap heightMap = new HeightMap(editSession, session.getRegion());
            HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
            int affected = heightMap.applyFilter(filter, iterations);
            player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");

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
            checkMaxRadius(radius);
            int affected = editSession.drainArea(
                    session.getPlacementPosition(player), radius);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Fix water
        } else if(split[0].equalsIgnoreCase("/fixwater")) {
            checkArgs(split, 1, 1, split[0]);
            int radius = Math.max(0, Integer.parseInt(split[1]));
            checkMaxRadius(radius);
            int affected = editSession.fixLiquid(
                    session.getPlacementPosition(player), radius, 8, 9);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Fix lava
        } else if(split[0].equalsIgnoreCase("/fixlava")) {
            checkArgs(split, 1, 1, split[0]);
            int radius = Math.max(0, Integer.parseInt(split[1]));
            checkMaxRadius(radius);
            int affected = editSession.fixLiquid(
                    session.getPlacementPosition(player), radius, 10, 11);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("//replace")) {
            checkArgs(split, 1, 2, split[0]);

            Set<Integer> from;
            Pattern to;
            if (split.length == 2) {
                from = null;
                to = getBlockPattern(split[1]);
            } else {
                from = getBlockIDs(split[1], true);
                to = getBlockPattern(split[2]);
            }

            int affected = 0;
            if (to instanceof SingleBlockPattern) {
                affected = editSession.replaceBlocks(session.getRegion(), from,
                        ((SingleBlockPattern)to).getBlock());
            } else {
                affected = editSession.replaceBlocks(session.getRegion(), from, to);
            }
            player.print(affected + " block(s) have been replaced.");

            return true;

        // Replace all blocks in the region
        } else if(split[0].equalsIgnoreCase("/replacenear")) {
            checkArgs(split, 2, 3, split[0]);
            int size = Math.max(1, Integer.parseInt(split[1]));
            Set<Integer> from;
            BaseBlock to;
            if (split.length == 3) {
                from = null;
                to = getBlock(split[2]);
            } else {
                from = getBlockIDs(split[2], true);
                to = getBlock(split[3]);
            }

            Vector min = player.getBlockIn().subtract(size, size, size);
            Vector max = player.getBlockIn().add(size, size, size);
            Region region = new CuboidRegion(min, max);

            int affected = editSession.replaceBlocks(region, from, to);
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

        // Make tree forest
        } else if (split[0].equalsIgnoreCase("/forestgen")) {
            checkArgs(split, 0, 2, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;
            double density = split.length > 2 ? Double.parseDouble(split[2]) / 100 : 0.05;

            int affected = editSession.makeForest(player.getPosition(),
                    size, density, false);
            player.print(affected + " trees created.");

            return true;

        // Make pine tree forest
        } else if (split[0].equalsIgnoreCase("/pinegen")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;
            double density = split.length > 2 ? Double.parseDouble(split[2]) / 100 : 0.05;

            int affected = editSession.makeForest(player.getPosition(),
                    size, density, true);
            player.print(affected + " pine trees created.");

            return true;

        // Let it snow~
        } else if (split[0].equalsIgnoreCase("/snow")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;

            int affected = editSession.simulateSnow(player.getBlockIn(), size);
            player.print(affected + " surfaces covered. Let it snow~");

            return true;

        // Make pumpkin patches
        } else if (split[0].equalsIgnoreCase("/pumpkins")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;

            int affected = editSession.makePumpkinPatches(player.getPosition(), size);
            player.print(affected + " pumpkin patches created.");

            return true;

        // Move
        } else if (split[0].equalsIgnoreCase("//moveair") ||
                   split[0].equalsIgnoreCase("//move")) {
            checkArgs(split, 0, 3, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            Vector dir = getDirection(player,
                    split.length > 2 ? split[2].toLowerCase() : "me");
            BaseBlock replace;

            // Replacement block argument
            if (split.length > 3) {
                replace = getBlock(split[3]);
            } else {
                replace = new BaseBlock(0);
            }
            
            boolean copyAir = split[0].equalsIgnoreCase("//moveair");

            int affected = editSession.moveCuboidRegion(session.getRegion(),
                    dir, count, copyAir, replace);
            player.print(affected + " blocks moved.");

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
            int change = Integer.parseInt(split[1]);
            if (split.length == 3) {
                dir = getDirection(player, split[2].toLowerCase());
            } else {
                dir = getDirection(player, "me");
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
            int change = Integer.parseInt(split[1]);
            if (split.length == 3) {
                dir = getDirection(player, split[2].toLowerCase());
            } else {
                dir = getDirection(player, "me");
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

        // Kill mobs
        } else if (split[0].equalsIgnoreCase("/butcher")) {
            checkArgs(split, 0, 1, split[0]);

            int radius = split.length > 1 ?
                Math.max(1, Integer.parseInt(split[1])) : -1;

            Vector origin = session.getPlacementPosition(player);
            int killed = 0;

            for (Mob mob : etc.getServer().getMobList()) {
                Vector mobPos = new Vector(mob.getX(), mob.getY(), mob.getZ());
                if (mob.getHealth() > 0
                        && (radius == -1 || mobPos.distance(origin) <= radius)) {
                    mob.setHealth(0);
                    killed++;
                }
            }

            player.print("Killed " + killed + " mobs.");

            return true;

        // Get chunk filename
        } else if (split[0].equalsIgnoreCase("/chunkinfo")) {
            checkArgs(split, 0, 0, split[0]);

            Vector pos = player.getBlockIn();
            int chunkX = (int)Math.floor(pos.getBlockX() / 16.0);
            int chunkZ = (int)Math.floor(pos.getBlockZ() / 16.0);

            String folder1 = Integer.toString(divisorMod(chunkX, 64), 36);
            String folder2 = Integer.toString(divisorMod(chunkZ, 64), 36);
            String filename = "c." + Integer.toString(chunkX, 36)
                    + "." + Integer.toString(chunkZ, 36) + ".dat";

            player.print("Chunk: " + chunkX + ", " + chunkZ);
            player.print(folder1 + "/" + folder2 + "/" + filename);

            return true;

        // Dump a list of involved chunks
        } else if (split[0].equalsIgnoreCase("/listchunks")) {
            checkArgs(split, 0, 0, split[0]);

            Set<Vector2D> chunks = session.getRegion().getChunks();

            for (Vector2D chunk : chunks) {
                player.print(NestedFileChunkStore.getFilename(chunk));
            }

            return true;

        // Dump a list of involved chunks
        } else if (split[0].equalsIgnoreCase("/delchunks")) {
            checkArgs(split, 0, 0, split[0]);

            Set<Vector2D> chunks = session.getRegion().getChunks();
            FileOutputStream out = null;

            if (shellSaveType == null) {
                player.printError("shell-save-type has to be configured in worldedit.properties");
            } else if (shellSaveType.equalsIgnoreCase("bat")) {
                try {
                    out = new FileOutputStream("worldedit-delchunks.bat");
                    OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
                    writer.write("@ECHO off\r\n");
                    writer.write("ECHO This batch file was generated by WorldEdit.\r\n");
                    writer.write("ECHO It contains a list of chunks that were in the selected region\r\n");
                    writer.write("ECHO at the time that the /delchunks command was used. Run this file\r\n");
                    writer.write("ECHO in order to delete the chunk files listed in this file.\r\n");
                    writer.write("ECHO.\r\n");
                    writer.write("PAUSE\r\n");

                    for (Vector2D chunk : chunks) {
                        String filename = NestedFileChunkStore.getFilename(chunk);
                        writer.write("ECHO " + filename + "\r\n");
                        writer.write("DEL \"world/" + filename + "\"\r\n");
                    }

                    writer.write("ECHO Complete.\r\n");
                    writer.write("PAUSE\r\n");
                    writer.close();
                    player.print("worldedit-delchunks.bat written. Run it when no one is near the region.");
                } catch (IOException e) {
                    player.printError("Error occurred: " + e.getMessage());
                } finally {
                    if (out != null) {
                        try { out.close(); } catch (IOException ie) {}
                    }
                }
            } else if (shellSaveType.equalsIgnoreCase("bash")) {
                try {
                    out = new FileOutputStream("worldedit-delchunks.sh");
                    OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
                    writer.write("#!/bin/bash\n");
                    writer.write("echo This shell file was generated by WorldEdit.\n");
                    writer.write("echo It contains a list of chunks that were in the selected region\n");
                    writer.write("echo at the time that the /delchunks command was used. Run this file\n");
                    writer.write("echo in order to delete the chunk files listed in this file.\n");
                    writer.write("echo\n");
                    writer.write("read -p \"Press any key to continue...\"\n");

                    for (Vector2D chunk : chunks) {
                        String filename = NestedFileChunkStore.getFilename(chunk);
                        writer.write("echo " + filename + "\n");
                        writer.write("rm \"world/" + filename + "\"\n");
                    }

                    writer.write("echo Complete.\n");
                    writer.write("read -p \"Press any key to continue...\"\n");
                    writer.close();
                    player.print("worldedit-delchunks.sh written. Run it when no one is near the region.");
                    player.print("You will have to chmod it to be executable.");
                } catch (IOException e) {
                    player.printError("Error occurred: " + e.getMessage());
                } finally {
                    if (out != null) {
                        try { out.close(); } catch (IOException ie) {}
                    }
                }
            } else {
                player.printError("Unknown shell script save type. 'bat' or 'bash' expected.");
            }

            return true;

        // List snapshots
        } else if (split[0].equalsIgnoreCase("/listsnapshots")) {
            checkArgs(split, 0, 1, split[0]);

            int num = split.length > 1 ?
                Math.min(40, Math.max(5, Integer.parseInt(split[1]))) : 5;

            if (snapshotRepo != null) {
                Snapshot[] snapshots = snapshotRepo.getSnapshots();

                if (snapshots.length > 0) {
                    for (byte i = 0; i < Math.min(num, snapshots.length); i++) {
                        player.print((i + 1) + ". " + snapshots[i].getName());
                    }

                    player.print("Use //use [snapshot] or //use latest to set the snapshot.");
                } else {
                    player.printError("No snapshots are available.");
                }
            } else {
                player.printError("Snapshot/backup restore is not configured.");
            }

            return true;

        // Use a certain snapshot
        } else if (split[0].equalsIgnoreCase("//use")) {
            checkArgs(split, 1, 1, split[0]);

            if (snapshotRepo == null) {
                player.printError("Snapshot/backup restore is not configured.");
                return true;
            }

            String name = split[1];

            // Want the latest snapshot?
            if (name.equalsIgnoreCase("latest")) {
                Snapshot snapshot = snapshotRepo.getDefaultSnapshot();

                if (snapshot != null) {
                    session.setSnapshot(null);
                    player.print("Now using newest snapshot.");
                } else {
                    player.printError("No snapshots were found.");
                }
            } else {
                try {
                    session.setSnapshot(snapshotRepo.getSnapshot(name));
                    player.print("Snapshot set to: " + name);
                } catch (InvalidSnapshotException e) {
                    player.printError("That snapshot does not exist or is not available.");
                }
            }

            return true;

        // Restore
        } else if (split[0].equalsIgnoreCase("//restore")) {
            checkArgs(split, 0, 1, split[0]);

            if (snapshotRepo == null) {
                player.printError("Snapshot/backup restore is not configured.");
                return true;
            }

            Region region = session.getRegion();
            Snapshot snapshot;

            if (split.length > 1) {
                try {
                    snapshot = snapshotRepo.getSnapshot(split[1]);
                } catch (InvalidSnapshotException e) {
                    player.printError("That snapshot does not exist or is not available.");
                    return true;
                }
            } else {
                snapshot = session.getSnapshot();
            }
            
            ChunkStore chunkStore = null;

            // No snapshot set?
            if (snapshot == null) {
                snapshot = snapshotRepo.getDefaultSnapshot();

                if (snapshot == null) {
                    player.printError("No snapshots were found.");
                    return true;
                }
            }

            // Load chunk store
            try {
                chunkStore = snapshot.getChunkStore();
                player.print("Snapshot '" + snapshot.getName() + "' loaded; now restoring...");
            } catch (DataException e) {
                player.printError("Failed to load snapshot: " + e.getMessage());
                return true;
            } catch (IOException e) {
                player.printError("Failed to load snapshot: " + e.getMessage());
                return true;
            }

            try {
                // Restore snapshot
                SnapshotRestore restore = new SnapshotRestore(chunkStore, region);
                //player.print(restore.getChunksAffected() + " chunk(s) will be loaded.");

                restore.restore(editSession);

                if (restore.hadTotalFailure()) {
                    String error = restore.getLastErrorMessage();
                    if (error != null) {
                        player.printError("Errors prevented any blocks from being restored.");
                        player.printError("Last error: " + error);
                    } else {
                        player.printError("No chunks could be loaded. (Bad archive?)");
                    }
                } else {
                    player.print(String.format("Restored; %d "
                            + "missing chunks and %d other errors.",
                            restore.getMissingChunks().size(),
                            restore.getErrorChunks().size()));
                }
            } finally {
                try {
                    chunkStore.close();
                } catch (IOException e) {
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Modulus, divisor-style.
     * 
     * @param a
     * @param n
     * @return
     */
    private static int divisorMod(int a, int n) {
        return (int)(a - n * Math.floor(Math.floor(a) / (double)n));
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
     *
     * @param player
     */
    @Override
    public void onDisconnect(Player player) {
        removeSession(new WorldEditPlayer(player));
    }

    /**
     * Called on arm swing.
     * 
     * @param player
     */
    public void onArmSwing(Player modPlayer) {
        if (!canUseCommand(modPlayer, "//")) { return; }

        WorldEditPlayer player = new WorldEditPlayer(modPlayer);
        WorldEditSession session = getSession(player);

        if (player.isHoldingPickAxe()) {
            if (session.hasSuperPickAxe()) {
                HitBlox hitBlox = new HitBlox(modPlayer, 5, 0.2);
                Block block = null;
                Set<BlockVector> pathBlocks = new HashSet<BlockVector>();

                // Get blocks along the way.
                while (hitBlox.getNextBlock() != null
                        && BlockType.canPassThrough(hitBlox.getCurBlock().getType())) {
                    block = hitBlox.getCurBlock();
                    pathBlocks.add(new BlockVector(block.getX(), block.getY(), block.getZ()));
                }

                if (pathBlocks.size() > 0) {
                    // Loop through the list of mobs and find the ones to kill
                    for (Mob mob : etc.getServer().getMobList()) {
                        Vector mobPos = new BlockVector(mob.getX(), mob.getY(), mob.getZ());
                        if (mob.getHealth() > 0 && pathBlocks.contains(mobPos.toBlockVector())
                                || pathBlocks.contains(mobPos.add(0, 1, 0).toBlockVector())
                                || pathBlocks.contains(mobPos.add(0, -1, 0).toBlockVector())) {
                            mob.setHealth(0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Called on right click.
     *
     * @param modPlayer
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockCreate(Player modPlayer, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        WorldEditPlayer player = new WorldEditPlayer(modPlayer);

        // This prevents needless sessions from being created
        if (!hasSession(player) && !(itemInHand == wandItem &&
                canUseCommand(modPlayer, "//pos2"))) { return false; }

        WorldEditSession session = getSession(player);

        if (itemInHand == wandItem && session.isToolControlEnabled()
                && canUseCommand(modPlayer, "//pos2")) {
            Vector cur = Vector.toBlockPoint(blockClicked.getX(),
                                           blockClicked.getY(),
                                           blockClicked.getZ());

            session.setPos2(cur);
            try {
                player.print("Second position set to " + cur
                        + " (" + session.getRegion().getSize() + ").");
            } catch (IncompleteRegionException e) {
                player.print("Second position set to " + cur + ".");
            }

            return true;
        } else if (player.isHoldingPickAxe()
                && session.getTool() == WorldEditSession.Tool.TREE) {
            Vector pos = Vector.toBlockPoint(blockClicked.getX(),
                                             blockClicked.getY() + 1,
                                             blockClicked.getZ());
            
            EditSession editSession =
                    new EditSession(session.getBlockChangeLimit());

            try {
                if (!ServerInterface.generateTree(editSession, pos)) {
                    player.printError("Notch won't let you put a tree there.");
                }
            } finally {
                session.remember(editSession);
            }

            return true;
        } else if (player.isHoldingPickAxe()
                && session.getTool() == WorldEditSession.Tool.INFO) {
            Vector pos = Vector.toBlockPoint(blockClicked.getX(),
                                             blockClicked.getY(),
                                             blockClicked.getZ());

            BaseBlock block = EditSession.rawGetBlock(pos);

            player.printRaw(Colors.LightPurple + "@" + pos + ": " + Colors.Yellow
                    + "Type: " + block.getID() + Colors.LightGray + " ("
                    + BlockType.fromID(block.getID()).getName() + ") "
                    + Colors.White
                    + "[" + block.getData() + "]");

            if (block instanceof MobSpawnerBlock) {
                player.printRaw(Colors.Yellow + "Mob Type: " + ((MobSpawnerBlock)block).getMobType());
            }

            return true;
        }

        return false;
    }

    /**
     * Called on left click.
     *
     * @param modPlayer
     * @param blockClicked
     * @param itemInHand
     * @return false if you want the action to go through
     */
    @Override
    public boolean onBlockDestroy(Player modPlayer, Block blockClicked) {
        if (!canUseCommand(modPlayer, "//pos1")
                && !canUseCommand(modPlayer, "//")) { return false; }

        WorldEditPlayer player = new WorldEditPlayer(modPlayer);
        WorldEditSession session = getSession(player);

        if (player.getItemInHand() == wandItem) {
            if (session.isToolControlEnabled()) {
                Vector cur = Vector.toBlockPoint(blockClicked.getX(),
                                               blockClicked.getY(),
                                               blockClicked.getZ());

                // Bug workaround
                if (cur.getBlockX() == 0 && cur.getBlockY() == 0
                        && cur.getBlockZ() == 0) {
                    return false;
                }

                try {
                    if (session.getPos1().equals(cur)) {
                        return false;
                    }
                } catch (IncompleteRegionException e) {
                }

                session.setPos1(cur);
                try {
                    player.print("First position set to " + cur
                            + " (" + session.getRegion().getSize() + ").");
                } catch (IncompleteRegionException e) {
                    player.print("First position set to " + cur + ".");
                }

                return true;
            }
        } else if (player.isHoldingPickAxe()) {
            if (session.hasSuperPickAxe()) {
                boolean canBedrock = canUseCommand(modPlayer, "/worldeditbedrock");

                // Single block super pickaxe
                if (session.getSuperPickaxeMode() ==
                        WorldEditSession.SuperPickaxeMode.SINGLE) {
                    Vector pos = new Vector(blockClicked.getX(),
                            blockClicked.getY(), blockClicked.getZ());
                    if (ServerInterface.getBlockType(pos) == 7 && !canBedrock) {
                        return true;
                    } else if (ServerInterface.getBlockType(pos) == 46) {
                        return false;
                    }

                    ServerInterface.setBlockType(pos, 0);

                // Area super pickaxe
                } else if (session.getSuperPickaxeMode() ==
                        WorldEditSession.SuperPickaxeMode.SAME_TYPE_AREA) {
                    Vector origin = new Vector(blockClicked.getX(),
                            blockClicked.getY(), blockClicked.getZ());
                    int ox = blockClicked.getX();
                    int oy = blockClicked.getY();
                    int oz = blockClicked.getZ();
                    int size = session.getSuperPickaxeRange();
                    int initialType = ServerInterface.getBlockType(origin);

                    if (initialType == 7 && !canBedrock) {
                        return true;
                    }

                    for (int x = ox - size; x <= ox + size; x++) {
                        for (int y = oy - size; y <= oy + size; y++) {
                            for (int z = oz - size; z <= oz + size; z++) {
                                Vector pos = new Vector(x, y, z);
                                if (ServerInterface.getBlockType(pos) == initialType) {
                                    ServerInterface.setBlockType(pos, 0);
                                }
                            }
                        }
                    }

                    return true;

                // Area super pickaxe
                } else if (session.getSuperPickaxeMode() ==
                        WorldEditSession.SuperPickaxeMode.SAME_TYPE_RECURSIVE) {
                    Vector origin = new Vector(blockClicked.getX(),
                            blockClicked.getY(), blockClicked.getZ());
                    int ox = blockClicked.getX();
                    int oy = blockClicked.getY();
                    int oz = blockClicked.getZ();
                    int size = session.getSuperPickaxeRange();
                    int initialType = ServerInterface.getBlockType(origin);

                    if (initialType == 7 && !canBedrock) {
                        return true;
                    }

                    recursiveSuperPickaxe(origin.toBlockVector(), origin, size,
                            initialType, new HashSet<BlockVector>());

                    return true;
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Helper method for the recursive super pickaxe.
     * 
     * @param pos
     * @param canBedrock
     * @return
     */
    private void recursiveSuperPickaxe(BlockVector pos, Vector origin,
            int size, int initialType, Set<BlockVector> visited) {
        if (origin.distance(pos) > size || visited.contains(pos)) {
            return;
        }

        visited.add(pos);

        if (ServerInterface.getBlockType(pos) == initialType) {
            ServerInterface.setBlockType(pos, 0);
        } else {
            return;
        }

        recursiveSuperPickaxe(pos.add(1, 0, 0).toBlockVector(), origin, size,
                initialType, visited);
        recursiveSuperPickaxe(pos.add(-1, 0, 0).toBlockVector(), origin, size,
                initialType, visited);
        recursiveSuperPickaxe(pos.add(0, 0, 1).toBlockVector(), origin, size,
                initialType, visited);
        recursiveSuperPickaxe(pos.add(0, 0, -1).toBlockVector(), origin, size,
                initialType, visited);
        recursiveSuperPickaxe(pos.add(0, 1, 0).toBlockVector(), origin, size,
                initialType, visited);
        recursiveSuperPickaxe(pos.add(0, -1, 0).toBlockVector(), origin, size,
                initialType, visited);
    }

    /**
     *
     * @param ply
     * @param split
     * @return whether the command was processed
     */
    @Override
    public boolean onCommand(Player ply, String[] split) {
        try {
            // Legacy /, command
            if (split[0].equals("/,")) {
                split[0] = "//";
            }

            if (commands.containsKey(split[0].toLowerCase())) {
                if (canUseCommand(ply, split[0])) {
                    WorldEditPlayer player = new WorldEditPlayer(ply);
                    WorldEditSession session = getSession(player);
                    EditSession editSession =
                            new EditSession(session.getBlockChangeLimit());
                    editSession.enableQueue();

                    long start = System.currentTimeMillis();

                    try {
                        return performCommand(player, session, editSession, split);
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();

                        if (profile) {
                            long time = System.currentTimeMillis() - start;
                            ply.sendMessage(Colors.Yellow + (time / 1000.0) + "s elapsed");
                        }
                    }
                }
            }

            return false;
        } catch (NumberFormatException e) {
            ply.sendMessage(Colors.Rose + "Number expected; string given.");
        } catch (IncompleteRegionException e2) {
            ply.sendMessage(Colors.Rose + "The edit region has not been fully defined.");
        } catch (UnknownItemException e3) {
            ply.sendMessage(Colors.Rose + "Block name '" + e3.getID() + "' was not recognized.");
        } catch (InvalidItemException e4) {
            ply.sendMessage(Colors.Rose + e4.getMessage());
        } catch (DisallowedItemException e4) {
            ply.sendMessage(Colors.Rose + "Block '" + e4.getID() + "' not allowed (see WorldEdit configuration).");
        } catch (MaxChangedBlocksException e5) {
            ply.sendMessage(Colors.Rose + "The maximum number of blocks changed ("
                    + e5.getBlockLimit() + ") in an instance was reached.");
        } catch (MaxRadiusException e) {
            ply.sendMessage(Colors.Rose + "Maximum radius: " + maxRadius);
        } catch (UnknownDirectionException ue) {
            ply.sendMessage(Colors.Rose + "Unknown direction: " + ue.getDirection());
        } catch (InsufficientArgumentsException e6) {
            ply.sendMessage(Colors.Rose + e6.getMessage());
        } catch (EmptyClipboardException ec) {
            ply.sendMessage(Colors.Rose + "Your clipboard is empty.");
        } catch (WorldEditException e7) {
            ply.sendMessage(Colors.Rose + e7.getMessage());
        } catch (Throwable excp) {
            ply.sendMessage(Colors.Rose + "Please report this error: [See console]");
            ply.sendMessage(excp.getClass().getName() + ": " + excp.getMessage());
            excp.printStackTrace();
        }

        return true;
    }

    /**
     * Checks to see if the player can use a command or /worldedit.
     *
     * @param player
     * @param command
     * @return
     */
    private boolean canUseCommand(Player player, String command) {
        // Allow the /worldeditselect permission
        if (command.equalsIgnoreCase("//pos1")
                || command.equalsIgnoreCase("//pos2")
                || command.equalsIgnoreCase("//hpos1")
                || command.equalsIgnoreCase("//hpos2")) {
            return player.canUseCommand(command)
                    || player.canUseCommand("/worldeditselect")
                    || player.canUseCommand("/worldedit");
        }
        
        return player.canUseCommand(command.replace("air", ""))
                || player.canUseCommand("/worldedit");
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

        profile = properties.getBoolean("debug-profile", false);

        wandItem = properties.getInt("wand-item", 271);

        // Get allowed blocks
        allowedBlocks = new HashSet<Integer>();
        for (String b : properties.getString("allowed-blocks",
                WorldEditListener.getDefaultAllowedBlocks()).split(",")) {
            try {
                allowedBlocks.add(Integer.parseInt(b));
            } catch (NumberFormatException e) {
            }
        }

        defaultChangeLimit = Math.max(-1, properties.getInt("max-blocks-changed", -1));

        maxRadius = Math.max(-1, properties.getInt("max-radius", -1));

        maxSuperPickaxeSize = Math.max(1, properties.getInt("max-super-pickaxe-size", 5));

        String snapshotsDir = properties.getString("snapshots-dir", "");
        if (!snapshotsDir.trim().equals("")) {
            snapshotRepo = new SnapshotRepository(snapshotsDir);
        } else {
            snapshotRepo = null;
        }

        String type = properties.getString("shell-save-type", "").trim();
        shellSaveType = type.equals("") ? null : type;

        registerHelp = properties.getBoolean("register-help", true);

        logComands = properties.getBoolean("log-commands", false);

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

        try {
            restrictions.load("worldedit-restrictions.txt");
            logger.log(Level.INFO, "WorldEdit group restrictions loaded");
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load WorldEdit restrictions: "
                    + e.getMessage());
        }
    }

    /**
     * Register commands with help.
     */
    public void registerCommands() {
        if (registerHelp) {
            for (Map.Entry<String,String> entry : commands.entrySet()) {
                etc.getInstance().addCommand(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * De-register commands.
     */
    public void deregisterCommands() {
        for (String key : commands.keySet()) {
            etc.getInstance().removeCommand(key);
        }
    }

    /**
     * Gets the WorldEditLibrary session for a player. Used for the bridge.
     *
     * @param player
     * @return
     */
    public WorldEditSession _bridgeSession(Player pl) {
        WorldEditPlayer player = new WorldEditPlayer(pl);

        if (sessions.containsKey(player)) {
            return sessions.get(player);
        } else {
            WorldEditSession session = new WorldEditSession();
            session.setBlockChangeLimit(defaultChangeLimit);
            sessions.put(player, session);
            return session;
        }
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    private static String joinString(String[] str, String delimiter) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }
}
