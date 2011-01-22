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

package com.sk89q.worldedit;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import javax.script.ScriptException;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.data.*;
import com.sk89q.worldedit.filters.*;
import com.sk89q.worldedit.scripting.*;
import com.sk89q.worldedit.snapshots.*;
import com.sk89q.worldedit.superpickaxe.*;
import com.sk89q.worldedit.regions.*;
import com.sk89q.worldedit.patterns.*;

/**
 * This class is the main entry point for WorldEdit. All events are routed
 * to an instance of this controller for processing by WorldEdit. For
 * integrating WorldEdit in other platforms, an instance of this class
 * should be created and events should be redirected to it.
 *
 * @author sk89q
 */
public class WorldEditController {
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    private ServerInterface server;
    private LocalConfiguration config;
    
    /**
     * Stores a list of WorldEdit sessions, keyed by players' names. Sessions
     * persist only for the user's session. On disconnect, the session will be
     * removed. Sessions are created only when they are needed and those
     * without any WorldEdit abilities or never use WorldEdit in a session will
     * not have a session object generated for them.
     */
    private HashMap<LocalPlayer,LocalSession> sessions =
            new HashMap<LocalPlayer,LocalSession>();
    
    /**
     * List of commands. These are checked when the command event is called, so
     * the list must know about every command.
     */
    private HashMap<String,String> commands = new HashMap<String,String>();
    
    /**
     * Construct an instance of the plugin
     * 
     * @param server
     * @param config
     */
    public WorldEditController(ServerInterface server, LocalConfiguration config) {
        this.server = server;
        this.config = config;
        
        populateCommands();
    }
    
    /**
     * Builds the list of commands.
     */
    private void populateCommands() {
        commands.put("//limit", "[Num] - See documentation");
        commands.put("/toggleplace", "Toggle placing at pos #1");

        commands.put("//undo", "Undo");
        commands.put("//redo", "Redo");
        commands.put("/clearhistory", "Clear history");
        
        commands.put("//pos1", "Set editing position #1");
        commands.put("//pos2", "Set editing position #2");
        commands.put("//hpos1", "Trace editing position #1");
        commands.put("//hpos2", "Trace editing position #2");
        commands.put("//chunk", "Select the chunk that you are in");
        commands.put("//wand", "Gives you the \"edit wand\"");
        commands.put("/toggleeditwand", "Toggles edit wand selection");
        commands.put("//expand", "[Num] <Dir> - Expands the selection");
        commands.put("//contract", "[Num] <Dir> - Contracts the selection");
        commands.put("//shift", "[Num] <Dir> - Shift the selection");
        commands.put("//size", "Get size of selected region");
        commands.put("//count", "[BlockIDs] - Count the number of blocks in the region");
        commands.put("//distr", "Get the top block distribution");

        commands.put("//set", "[ID] - Set all blocks inside region");
        commands.put("//replace", "<FromID> [ToID] - Replace all existing blocks inside region");
        commands.put("//overlay", "[ID] - Overlay the area one layer");
        commands.put("//walls", "[ID] - Build walls");
        commands.put("//outline", "[ID] - Outline the region with blocks");
        commands.put("//move", "<Count> <Dir> <LeaveID> - Move the selection");
        commands.put("//stack", "<Count> <Dir> - Stacks the selection");
        commands.put("//smooth", "<Iterations> - Smooth an area's heightmap");

        commands.put("//copy", "Copies the currently selected region");
        commands.put("//cut", "Cuts the currently selected region");
        commands.put("//paste", "<AtOrigin?> - Pastes the clipboard");
        commands.put("//rotate", "[Angle] - Rotate the clipboard");
        commands.put("//flip", "<Dir> - Flip the clipboard");
        commands.put("//load", "[Filename] - Load .schematic into clipboard");
        commands.put("//save", "[Filename] - Save clipboard to .schematic");
        commands.put("/clearclipboard", "Clear clipboard");

        commands.put("//hcyl", "[ID] [Radius] <Height> - Create a vertical hollow cylinder");
        commands.put("//cyl", "[ID] [Radius] <Height> - Create a vertical cylinder");
        commands.put("//sphere", "[ID] [Radius] <Raised?> - Create a sphere");
        commands.put("//hsphere", "[ID] [Radius] <Raised?> - Create a hollow sphere");
        commands.put("/forestgen", "<Size> <Density> - Make Notch tree forest");
        commands.put("/pinegen", "<Size> <Density> - Make an ugly pine tree forest");
        commands.put("/pumpkins", "<Size> - Make a pumpkin forest");

        commands.put("//fill", "[ID] [Radius] <Depth> - Fill a hole");
        commands.put("//fillr", "[ID] [Radius] - Fill a hole fully recursively");
        commands.put("/fixwater", "[Radius] - Level nearby pools of water");
        commands.put("/fixlava", "[Radius] - Level nearby pools of lava");
        commands.put("//drain", "[Radius] - Drain nearby water/lava pools");
        commands.put("/removeabove", "<Size> <Height> - Remove blocks above head");
        commands.put("/removebelow", "<Size> <Height> - Remove blocks below position");
        commands.put("/removenear", "<ID> <Size> - Remove blocks near you");
        commands.put("/replacenear", "<Size> <FromID> [ToID] - Replace all existing blocks nearby");
        commands.put("/snow", "<Radius> - Simulate snow cover");
        commands.put("/thaw", "<Radius> - Unthaw/remove snow");
        commands.put("/butcher", "<Radius> - Kill nearby mobs");
        commands.put("/ex", "[Size] - Extinguish fires");

        commands.put("/chunkinfo", "Get the filename of the chunk that you are in");
        commands.put("/listchunks", "Print a list of used chunks");
        commands.put("/delchunks", "Generate a shell script to delete chunks");

        commands.put("/unstuck", "Go up to the first free spot");
        commands.put("/ascend", "Go up one level");
        commands.put("/descend", "Go down one level");
        commands.put("/jumpto", "Jump to the block that you are looking at");
        commands.put("/thru", "Go through the wall that you are looking at");
        commands.put("/ceil", "<Clearance> - Get to the ceiling");
        commands.put("/up", "<Distance> - Go up some distance");

        commands.put("/listsnapshots", "<Num> - List the 5 newest snapshots");
        commands.put("//use", "[SnapshotID] - Use a particular snapshot");
        commands.put("//restore", "<SnapshotID> - Restore a particular snapshot");

        commands.put("//", "Toggles super pick axe.");
        commands.put("/single", "Switch to single block super pickaxe mode");
        commands.put("/area", "[Range] - Switch to area super pickaxe mode");
        commands.put("/recur", "[Range] - Switch to recursive super pickaxe mode");
        commands.put("/none", "Switch to no tool");
        commands.put("/info", "Switch to the info tool");
        commands.put("/tree", "Switch to the tree tool");
        commands.put("/pinetree", "Switch to the pine tree tool");
        commands.put("/bigtree", "Switch to the big tree tool");
        commands.put("/repl", "[ID] - Switch to the block replacer tool");
        commands.put("/brush", "[ID] <Radius> <NoReplace?> - Switch to the sphere brush tool");
        commands.put("/rbrush", "[ID] <Radius> - Switch to the replacing sphere brush tool");

        commands.put("/cs", "[Filename] <args...> - Execute a CraftScript");
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    public LocalSession getSession(LocalPlayer player) {
        if (sessions.containsKey(player)) {
            return sessions.get(player);
        }
        
        LocalSession session = new LocalSession();
        
        // Set the limit on the number of blocks that an operation can
        // change at once, or don't if the player has an override or there
        // is no limit. There is also a default limit
        if (!player.hasPermission("worldeditnomax")
                && config.maxChangeLimit > -1) {
            
            // If the default limit is infinite but there is a maximum
            // limit, make sure to not have it be overridden
            if (config.defaultChangeLimit < 0) {
                session.setBlockChangeLimit(config.maxChangeLimit);
            } else {
                // Bound the change limit
                int limit = Math.min(config.defaultChangeLimit,
                        config.maxChangeLimit);
                session.setBlockChangeLimit(limit);
            }
        } else {
            // No change limit or override
            session.setBlockChangeLimit(config.defaultChangeLimit);
        }
        
        // Have the session use inventory if it's enabled and the player
        // doesn't have an override
        session.setUseInventory(config.useInventory
                && (!config.useInventoryOverride
                        || !player.hasPermission("worldeditunlimited")));
        
        // Remember the session
        sessions.put(player, session);
        
        return session;
    }

    /**
     * Returns true if the player has a session.
     * 
     * @param player
     * @return
     */
    public boolean hasSession(LocalPlayer player) {
        return sessions.containsKey(player);
    }

    /**
     * Get an item ID from an item name or an item ID number.
     *
     * @param player
     * @param arg
     * @param allAllowed true to ignore blacklists
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        BlockType blockType;
        arg = arg.replace("_", " ");
        arg = arg.replace(";", "|");
        String[] args0 = arg.split("\\|");
        String[] args1 = args0[0].split(":", 2);
        String testID = args1[0];
        
        int data;
        
        // Parse the block data (optional)
        try {
            data = args1.length > 1 ? Integer.parseInt(args1[1]) : 0;
            if (data > 15 || data < 0) {
                data = 0;
            }
        } catch (NumberFormatException e) {
            data = 0;
        }

        // Attempt to parse the item ID or otherwise resolve an item/block
        // name to its numeric ID
        try {
            blockType = BlockType.fromID(Integer.parseInt(testID));
        } catch (NumberFormatException e) {
            blockType = BlockType.lookup(testID);
            if (blockType == null) {
                int t = server.resolveItem(testID);
                if (t > 0 && t < 256) {
                    blockType = BlockType.fromID(t);
                }
            }
        }

        if (blockType == null) {
            throw new UnknownItemException(arg);
        }

        // Check if the item is allowed
        if (allAllowed || player.hasPermission("worldeditanyblock")
                || !config.disallowedBlocks.contains(blockType.getID())) {
            
            // Allow special sign text syntax
            if (blockType == BlockType.SIGN_POST
                    || blockType == BlockType.WALL_SIGN) {
                String[] text = new String[4];
                text[0] = args0.length > 1 ? args0[1] : "";
                text[1] = args0.length > 2 ? args0[2] : "";
                text[2] = args0.length > 3 ? args0[3] : "";
                text[3] = args0.length > 4 ? args0[4] : "";
                return new SignBlock(blockType.getID(), data, text);
            
            // Alow setting mob spawn type
            } else if (blockType == BlockType.MOB_SPAWNER) {
                if (args0.length > 1) {
                    if (!server.isValidMobType(args0[1])) {
                        throw new InvalidItemException(arg, "Unknown mob type '" + args0[1] + "'");
                    }
                    return new MobSpawnerBlock(data, args0[1]);
                } else {
                    return new MobSpawnerBlock(data, "Pig");
                }
            }

            return new BaseBlock(blockType.getID(), data);
        }

        throw new DisallowedItemException(arg);
    }

    /**
     * Get a block.
     *
     * @param player
     * @param id
     * @return
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public BaseBlock getBlock(LocalPlayer player, String id)
            throws UnknownItemException, DisallowedItemException {
        return getBlock(player, id, false);
    }

    /**
     * Get a list of blocks as a set. This returns a Pattern.
     *
     * @param player
     * @param list
     * @return pattern
     */
    public Pattern getBlockPattern(LocalPlayer player, String list)
            throws UnknownItemException, DisallowedItemException {

        String[] items = list.split(",");

        if (items.length == 1) {
            return new SingleBlockPattern(getBlock(player, items[0]));
        }

        List<BlockChance> blockChances = new ArrayList<BlockChance>();

        for (String s : items) {
            BaseBlock block;
            
            double chance;
            if (s.matches("[0-9]+(?:\\.(?:[0-9]+)?)?%.*")) {
                String[] p = s.split("%");
                chance = Double.parseDouble(p[0]);
                block = getBlock(player, p[1]);
            } else {
                chance = 1;
                block = getBlock(player, s);
            }
            
            blockChances.add(new BlockChance(block, chance));
        }

        return new RandomFillPattern(blockChances);
    }

    /**
     * Get a list of blocks as a set.
     *
     *@param player
     * @param list
     * @param allBlocksAllowed
     * @return set
     */
    public Set<Integer> getBlockIDs(LocalPlayer player,
            String list, boolean allBlocksAllowed)
            throws UnknownItemException, DisallowedItemException {
        
        String[] items = list.split(",");
        Set<Integer> blocks = new HashSet<Integer>();
        for (String s : items) {
            blocks.add(getBlock(player, s, allBlocksAllowed).getType());
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
        if (config.maxRadius > 0 && radius > config.maxRadius) {
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
    private boolean performCommand(LocalPlayer player,
            LocalSession session, EditSession editSession, String[] split)
            throws WorldEditException
    {
        if (config.logComands) {
            logger.log(Level.INFO, "WorldEdit: " + player.getName() + ": "
                    + StringUtil.joinString(split, " "));
        }
        
        LocalWorld world = player.getPosition().getWorld();

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
            WorldVector pos = player.getSolidBlockTrace(300);
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
            player.giveItem(config.wandItem, 1);
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
            if (!player.hasPermission("worldeditnomax")
                    && config.maxChangeLimit > -1) {
                if (limit > config.maxChangeLimit) {
                    player.printError("Your maximum allowable limit is "
                            + config.maxChangeLimit + ".");
                    return true;
                }
            }
            
            session.setBlockChangeLimit(limit);
            player.print("Block change limit set to " + limit + ".");
            
            return true;

        // Single super pickaxe mode
        } else if (split[0].equalsIgnoreCase("/single")) {
            if (!canUseCommand(player, "/")) {
                player.printError("You don't have permission for super pickaxe usage.");
                return true;
            }
            
            checkArgs(split, 0, 0, split[0]);
            session.setLeftClickMode(new SinglePickaxe());
            session.enableSuperPickAxe();
            player.print("Mode changed. Left click with a pickaxe. // to disable.");
            return true;

        // Area/recursive super pickaxe mode
        } else if (split[0].equalsIgnoreCase("/area")
                || split[0].equalsIgnoreCase("/recur")) {
            
            if (!canUseCommand(player, "/")) {
                player.printError("You don't have permission for super pickaxe usage.");
                return true;
            }
            
            checkArgs(split, 1, 1, split[0]);
            
            boolean recur = split[0].equalsIgnoreCase("/recur");
            int range = Integer.parseInt(split[1]);
            
            if (range > config.maxSuperPickaxeSize) {
                player.printError("Maximum range: " + config.maxSuperPickaxeSize);
                return true;
            }
            
            session.setLeftClickMode(
                    recur ? new RecursivePickaxe(range) : new AreaPickaxe(range));
            session.enableSuperPickAxe();
            player.print("Mode changed. Left click with a pickaxe. // to disable.");
            return true;

        // Tree tool
        } else if (split[0].equalsIgnoreCase("/tree")) {
            checkArgs(split, 0, 0, split[0]);
            session.setArmSwingMode(null);
            session.setRightClickMode(new TreePlanter());
            player.print("Tree tool equipped. Right click with a pickaxe.");
            return true;

        // Big tree tool
        } else if (split[0].equalsIgnoreCase("/bigtree")) {
            checkArgs(split, 0, 0, split[0]);
            session.setArmSwingMode(null);
            session.setRightClickMode(new BigTreePlanter());
            player.print("Big tree tool equipped. Right click with a pickaxe.");
            return true;

        // Pine tree tool
        } else if (split[0].equalsIgnoreCase("/pinetree")) {
            checkArgs(split, 0, 0, split[0]);
            session.setArmSwingMode(null);
            session.setRightClickMode(new PineTreePlanter());
            player.print("Pine tree tree tool equipped. Right click with a pickaxe.");
            return true;

        // Info tool
        } else if (split[0].equalsIgnoreCase("/info")) {
            checkArgs(split, 0, 0, split[0]);
            session.setArmSwingMode(null);
            session.setRightClickMode(new QueryTool());
            player.print("Info tool equipped. Right click with a pickaxe.");
            return true;

        // Replace block tool
        } else if (split[0].equalsIgnoreCase("/repl")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock targetBlock = getBlock(player, split[1]);
            session.setArmSwingMode(null);
            session.setRightClickMode(new BlockReplacer(targetBlock));
            player.print("Block replacer tool equipped. Right click with a pickaxe.");
            return true;

        // Sphere brush tool
        } else if (split[0].equalsIgnoreCase("/brush")) {
            checkArgs(split, 1, 3, split[0]);
            int radius = split.length > 2 ? Integer.parseInt(split[2]) : 2;
            boolean nonReplacing = split.length > 3
                    ? (split[3].equalsIgnoreCase("true")
                            || split[3].equalsIgnoreCase("yes")) : false;
            if (radius > config.maxBrushRadius) {
                player.printError("Maximum allowed brush radius: "
                        + config.maxBrushRadius);
                return true;
            }
            BaseBlock targetBlock = getBlock(player, split[1]);
            session.setRightClickMode(null);
            session.setArmSwingMode(new SphereBrush(targetBlock, radius, nonReplacing));
            if (nonReplacing) {
                player.print("Non-replacing sphere brush tool equipped.");
            } else {
                player.print("Sphere brush tool equipped. Swing with a pickaxe.");
            }
            return true;

        // Sphere brush tool
        } else if (split[0].equalsIgnoreCase("/rbrush")) {
            checkArgs(split, 1, 3, split[0]);
            int radius = split.length > 2 ? Integer.parseInt(split[2]) : 2;
            if (radius > config.maxBrushRadius) {
                player.printError("Maximum allowed brush radius: "
                        + config.maxBrushRadius);
                return true;
            }
            BaseBlock targetBlock = getBlock(player, split[1]);
            session.setRightClickMode(null);
            session.setArmSwingMode(new ReplacingSphereBrush(targetBlock, radius));
            player.print("Replacing sphere brush tool equipped. Swing with a pickaxe.");
            return true;

        // No tool
        } else if (split[0].equalsIgnoreCase("/none")) {
            checkArgs(split, 0, 0, split[0]);
            session.setArmSwingMode(null);
            session.setRightClickMode(null);
            player.print("Now no longer equipping a tool.");
            return true;

        // Undo
        } else if (split[0].equalsIgnoreCase("//undo")) {
            checkArgs(split, 0, 0, split[0]);
            EditSession undone = session.undo(session.getBlockBag(player));
            if (undone != null) {
                player.print("Undo successful.");
                flushBlockBag(player, undone);
            } else {
                player.printError("Nothing to undo.");
            }
            return true;

        // Redo
        } else if (split[0].equalsIgnoreCase("//redo")) {
            checkArgs(split, 0, 0, split[0]);
            EditSession redone = session.redo(session.getBlockBag(player));
            if (redone != null) {
                player.print("Redo successful.");
                flushBlockBag(player, redone);
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
        } else if (split[0].equalsIgnoreCase("//paste")) {
            checkArgs(split, 0, 1, split[0]);
            boolean atOrigin = split.length > 1
                    ? (split[1].equalsIgnoreCase("true")
                            || split[1].equalsIgnoreCase("yes"))
                    : false;
            if (atOrigin) {
                Vector pos = session.getClipboard().getOrigin();
                session.getClipboard().place(editSession, pos, false);
                player.findFreePosition();
                player.print("Pasted to copy origin. Undo with //undo");
            } else {
                Vector pos = session.getPlacementPosition(player);
                session.getClipboard().paste(editSession, pos, false);
                player.findFreePosition();
                player.print("Pasted relative to you. Undo with //undo");
            }

            return true;

        // Draw a hollow cylinder
        } else if (split[0].equalsIgnoreCase("//hcyl")
                || split[0].equalsIgnoreCase("//cyl")) {
            checkArgs(split, 2, 3, split[0]);
            BaseBlock block = getBlock(player, split[1]);
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
            BaseBlock block = getBlock(player, split[1]);
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
            Pattern pattern = getBlockPattern(player, split[1]);
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
            BaseBlock block = getBlock(player, split[1], true);
            int size = Math.max(1, Integer.parseInt(split[2]));
            checkMaxRadius(size);

            int affected = editSession.removeNear(
                    session.getPlacementPosition(player), block.getType(), size);
            player.print(affected + " block(s) have been removed.");

            return true;

        // Extinguish
        } else if (split[0].equalsIgnoreCase("/ex")) {
            checkArgs(split, 0, 1, split[0]);
            int defaultRadius = config.maxRadius != -1 ? Math.min(40, config.maxRadius) : 40;
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
            Set<Integer> searchIDs = getBlockIDs(player, split[1], true);
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
            Pattern pattern = getBlockPattern(player, split[1]);
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
            BaseBlock block = getBlock(player, split[1]);
            int affected = editSession.makeCuboidFaces(session.getRegion(), block);
            player.print(affected + " block(s) have been changed.");

            return true;

        // Set the walls of a region
        } else if(split[0].equalsIgnoreCase("//walls")) {
            checkArgs(split, 1, 1, split[0]);
            BaseBlock block = getBlock(player, split[1]);
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
                to = getBlockPattern(player, split[1]);
            } else {
                from = getBlockIDs(player, split[1], true);
                to = getBlockPattern(player, split[2]);
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
                to = getBlock(player, split[2]);
            } else {
                from = getBlockIDs(player, split[2], true);
                to = getBlock(player, split[3]);
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
            BaseBlock block = getBlock(player, split[1]);

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
                    getBlock(player, split[1]);
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

        // Thaw
        } else if (split[0].equalsIgnoreCase("/thaw")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;

            int affected = editSession.thaw(player.getBlockIn(), size);
            player.print(affected + " surfaces thawed.");

            return true;

        // Make pumpkin patches
        } else if (split[0].equalsIgnoreCase("/pumpkins")) {
            checkArgs(split, 0, 1, split[0]);
            int size = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 10;

            int affected = editSession.makePumpkinPatches(player.getPosition(), size);
            player.print(affected + " pumpkin patches created.");

            return true;

        // Move
        } else if (split[0].equalsIgnoreCase("//move")) {
            checkArgs(split, 0, 3, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            Vector dir = getDirection(player,
                    split.length > 2 ? split[2].toLowerCase() : "me");
            BaseBlock replace;

            // Replacement block argument
            if (split.length > 3) {
                replace = getBlock(player, split[3]);
            } else {
                replace = new BaseBlock(0);
            }

            int affected = editSession.moveCuboidRegion(session.getRegion(),
                    dir, count, true, replace);
            player.print(affected + " blocks moved.");

            return true;

        // Stack
        } else if (split[0].equalsIgnoreCase("//stack")) {
            checkArgs(split, 0, 2, split[0]);
            int count = split.length > 1 ? Math.max(1, Integer.parseInt(split[1])) : 1;
            Vector dir = getDirection(player,
                    split.length > 2 ? split[2].toLowerCase() : "me");

            int affected = editSession.stackCuboidRegion(session.getRegion(),
                    dir, count, true);
            player.print(affected + " blocks changed. Undo with //undo");

            return true;

        // Expand
        } else if (split[0].equalsIgnoreCase("//expand")) {
            checkArgs(split, 1, 3, split[0]);
            Vector dir;
            
            if (split[1].equals("vert") || split[1].equals("vertical")) {
                Region region = session.getRegion();
                int oldSize = region.getSize();
                region.expand(new Vector(0, 128, 0));
                region.expand(new Vector(0, -128, 0));
                session.learnRegionChanges();
                int newSize = region.getSize();
                player.print("Region expanded " + (newSize - oldSize) + " blocks [top-to-bottom].");
                return true;
            }
            
            int change = Integer.parseInt(split[1]);
            int reverseChange = 0;
            
            if (split.length == 3) {
                try {
                    reverseChange = Integer.parseInt(split[2]) * -1;
                    dir = getDirection(player, "me");
                } catch (NumberFormatException e) {
                    dir = getDirection(player, split[2].toLowerCase());
                }
            } else if (split.length == 4) {
                reverseChange = Integer.parseInt(split[2]) * -1;
                dir = getDirection(player, split[3].toLowerCase());
            } else {
                dir = getDirection(player, "me");
            }

            Region region = session.getRegion();
            int oldSize = region.getSize();
            region.expand(dir.multiply(change));
            if (reverseChange != 0) {
                region.expand(dir.multiply(reverseChange));
            }
            session.learnRegionChanges();
            int newSize = region.getSize();
            player.print("Region expanded " + (newSize - oldSize) + " blocks.");

            return true;

        // Contract
        } else if (split[0].equalsIgnoreCase("//contract")) {
            checkArgs(split, 1, 3, split[0]);
            Vector dir;
            int change = Integer.parseInt(split[1]);
            int reverseChange = 0;
            if (split.length == 3) {
                try {
                    reverseChange = Integer.parseInt(split[2]) * -1;
                    dir = getDirection(player, "me");
                } catch (NumberFormatException e) {
                    dir = getDirection(player, split[2].toLowerCase());
                }
            } else if (split.length == 4) {
                reverseChange = Integer.parseInt(split[2]) * -1;
                dir = getDirection(player, split[3].toLowerCase());
            } else {
                dir = getDirection(player, "me");
            }

            Region region = session.getRegion();
            int oldSize = region.getSize();
            region.contract(dir.multiply(change));
            if (reverseChange != 0) {
                region.contract(dir.multiply(reverseChange));
            }
            session.learnRegionChanges();
            int newSize = region.getSize();
            player.print("Region contracted " + (oldSize - newSize) + " blocks.");

            return true;

        // Shift
        } else if (split[0].equalsIgnoreCase("//shift")) {
            checkArgs(split, 1, 2, split[0]);
            Vector dir;
            int change = Integer.parseInt(split[1]);
            if (split.length == 3) {
                dir = getDirection(player, split[2].toLowerCase());
            } else {
                dir = getDirection(player, "me");
            }

            Region region = session.getRegion();
            region.expand(dir.multiply(change));
            region.contract(dir.multiply(change));
            session.learnRegionChanges();
            player.print("Region shifted.");

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

        // Flip
        } else if (split[0].equalsIgnoreCase("//flip")) {
            checkArgs(split, 0, 1, split[0]);
            CuboidClipboard.FlipDirection dir = getFlipDirection(player,
                    split.length > 1 ? split[1].toLowerCase() : "me");

            CuboidClipboard clipboard = session.getClipboard();
            clipboard.flip(dir);
            player.print("Clipboard flipped.");

            return true;

        // Kill mobs
        } else if (split[0].equalsIgnoreCase("/butcher")) {
            checkArgs(split, 0, 1, split[0]);

            int radius = split.length > 1 ?
                Math.max(1, Integer.parseInt(split[1])) : -1;

            Vector origin = session.getPlacementPosition(player);
            int killed = world.killMobs(origin, radius);
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

            if (config.shellSaveType == null) {
                player.printError("shell-save-type has to be configured in worldedit.properties");
            } else if (config.shellSaveType.equalsIgnoreCase("bat")) {
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
            } else if (config.shellSaveType.equalsIgnoreCase("bash")) {
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

            if (config.snapshotRepo != null) {
                Snapshot[] snapshots = config.snapshotRepo.getSnapshots();

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

            if (config.snapshotRepo == null) {
                player.printError("Snapshot/backup restore is not configured.");
                return true;
            }

            String name = split[1];

            // Want the latest snapshot?
            if (name.equalsIgnoreCase("latest")) {
                Snapshot snapshot = config.snapshotRepo.getDefaultSnapshot();

                if (snapshot != null) {
                    session.setSnapshot(null);
                    player.print("Now using newest snapshot.");
                } else {
                    player.printError("No snapshots were found.");
                }
            } else {
                try {
                    session.setSnapshot(config.snapshotRepo.getSnapshot(name));
                    player.print("Snapshot set to: " + name);
                } catch (InvalidSnapshotException e) {
                    player.printError("That snapshot does not exist or is not available.");
                }
            }

            return true;

        // Restore
        } else if (split[0].equalsIgnoreCase("//restore")) {
            checkArgs(split, 0, 1, split[0]);

            if (config.snapshotRepo == null) {
                player.printError("Snapshot/backup restore is not configured.");
                return true;
            }

            Region region = session.getRegion();
            Snapshot snapshot;

            if (split.length > 1) {
                try {
                    snapshot = config.snapshotRepo.getSnapshot(split[1]);
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
                snapshot = config.snapshotRepo.getDefaultSnapshot();

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

        // CraftScript
        } else if (split[0].equalsIgnoreCase("/cs")) {
            checkArgs(split, 1, -1, split[0]);
            
            String[] args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, split.length - 1);
            
            runScript(player, split[1], args);
            
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
    public Vector getDirection(LocalPlayer player, String dirStr)
            throws UnknownDirectionException {
        int xm = 0;
        int ym = 0;
        int zm = 0;
        
        LocalPlayer.DIRECTION dir = null;
        
        dirStr = dirStr.toLowerCase();
        boolean wasDetected = false;

        if (dirStr.equals("me")) {
            dir = player.getCardinalDirection();
            wasDetected = true;
        }

        if (dirStr.charAt(0) == 'w' || dir == LocalPlayer.DIRECTION.WEST) {
            zm += 1;
        } else if (dirStr.charAt(0) == 'e' || dir == LocalPlayer.DIRECTION.EAST) {
            zm -= 1;
        } else if (dirStr.charAt(0) == 's' || dir == LocalPlayer.DIRECTION.SOUTH) {
            xm += 1;
        } else if (dirStr.charAt(0) == 'n' || dir == LocalPlayer.DIRECTION.NORTH) {
            xm -= 1;
        } else if (dirStr.charAt(0) == 'u') {
            ym += 1;
        } else if (dirStr.charAt(0) == 'd') {
            ym -= 1;
        } else {
            if (wasDetected) {
                throw new UnknownDirectionException(dir.name());
            } else {
                throw new UnknownDirectionException(dirStr);
            }
        }

        return new Vector(xm, ym, zm);
    }

    /**
     * Get the flip direction for a player's direction. May return
     * null if a direction could not be found.
     *
     * @param player
     * @param dir
     * @return
     */
    public CuboidClipboard.FlipDirection getFlipDirection(
            LocalPlayer player, String dirStr)
            throws UnknownDirectionException {
        LocalPlayer.DIRECTION dir = null;
        
        if (dirStr.equals("me")) {
            dir = player.getCardinalDirection();
        }

        if (dirStr.charAt(0) == 'w' || dir == LocalPlayer.DIRECTION.EAST) {
            return CuboidClipboard.FlipDirection.WEST_EAST;
        } else if (dirStr.charAt(0) == 'e' || dir == LocalPlayer.DIRECTION.EAST) {
            return CuboidClipboard.FlipDirection.WEST_EAST;
        } else if (dirStr.charAt(0) == 's' || dir == LocalPlayer.DIRECTION.SOUTH) {
            return CuboidClipboard.FlipDirection.NORTH_SOUTH;
        } else if (dirStr.charAt(0) == 'n' || dir == LocalPlayer.DIRECTION.SOUTH) {
            return CuboidClipboard.FlipDirection.NORTH_SOUTH;
        } else if (dirStr.charAt(0) == 'u') {
            return CuboidClipboard.FlipDirection.UP_DOWN;
        } else if (dirStr.charAt(0) == 'd') {
            return CuboidClipboard.FlipDirection.UP_DOWN;
        } else {
            throw new UnknownDirectionException(dirStr);
        }
    }

    /**
     * Remove a session.
     * 
     * @param player
     */
    public void removeSession(LocalPlayer player) {
        sessions.remove(player);
    }

    /**
     * Remove all sessions.
     */
    public void clearSessions() {
        sessions.clear();
    }
    
    /**
     * Flush a block bag's changes to a player.
     * 
     * @param player
     * @param blockBag
     * @param editSession
     */
    private static void flushBlockBag(LocalPlayer player,
            EditSession editSession) {
        
        BlockBag blockBag = editSession.getBlockBag();
        
        if (blockBag != null) {
            blockBag.flushChanges();
        }
        
        Set<Integer> missingBlocks = editSession.popMissingBlocks();
        
        if (missingBlocks.size() > 0) {
            StringBuilder str = new StringBuilder();
            str.append("Missing these blocks: ");
            int size = missingBlocks.size();
            int i = 0;
            
            for (Integer id : missingBlocks) {
                BlockType type = BlockType.fromID(id);
                
                str.append(type != null
                        ? type.getName() + " (" + id + ")"
                        : id.toString());
                
                i++;
                
                if (i != size) {
                    str.append(", ");
                }
            }
            
            player.printError(str.toString());
        }
    }

    /**
     * Checks to see if the player can use a command or /worldedit.
     *
     * @param player
     * @param command
     * @return
     */
    private boolean canUseCommand(LocalPlayer player, String command) {
        // Allow the /worldeditselect permission
        if (command.equalsIgnoreCase("/pos1")
                || command.equalsIgnoreCase("/pos2")
                || command.equalsIgnoreCase("/hpos1")
                || command.equalsIgnoreCase("/hpos2")
                || command.equalsIgnoreCase("/chunk")
                || command.equalsIgnoreCase("/expand")
                || command.equalsIgnoreCase("/contract")
                || command.equalsIgnoreCase("/shift")
                || command.equalsIgnoreCase("toggleeditwand")) {
            return player.hasPermission(command)
                    || player.hasPermission("worldeditselect")
                    || player.hasPermission("worldedit");
        }
        
        return player.hasPermission(command)
                || player.hasPermission("worldedit");
    }

    /**
     * @return the commands
     */
    public HashMap<String, String> getCommands() {
        return commands;
    }
    
    /**
     *
     * @param player
     */
    public void handleDisconnect(LocalPlayer player) {
        removeSession(player);
    }

    /**
     * Called on arm swing.
     * 
     * @param player
     * @return 
     */
    public boolean handleArmSwing(LocalPlayer player) {
        LocalSession session = getSession(player);
        
        if (player.isHoldingPickAxe()) {
            if (session.getArmSwingMode() != null) {
                session.getArmSwingMode().act(server, config,
                        player, session, null);
                return true;
            }
        } else if (player.getItemInHand() == config.navigationWand
                && config.navigationWandMaxDistance > 0
                && player.hasPermission("jumpto")) {
            WorldVector pos = player.getSolidBlockTrace(config.navigationWandMaxDistance);
            if (pos != null) {
                player.findFreePosition(pos);
            } else {
                player.printError("No block in sight (or too far)!");
            }
        }
        
        return false;
    }

    /**
     * Called on right click.
     *
     * @param player
     * @param clicked
     * @return false if you want the action to go through
     */
    public boolean handleBlockRightClick(LocalPlayer player, WorldVector clicked) {
        int itemInHand = player.getItemInHand();
        
        LocalSession session = getSession(player);

        if (itemInHand == config.wandItem && session.isToolControlEnabled()
                && canUseCommand(player, "/pos2")) {
            session.setPos2(clicked);
            try {
                player.print("Second position set to " + clicked
                        + " (" + session.getRegion().getSize() + ").");
            } catch (IncompleteRegionException e) {
                player.print("Second position set to " + clicked + ".");
            }

            return true;
        } else if (player.isHoldingPickAxe() && session.getRightClickMode() != null) {
            return session.getRightClickMode().act(server, config, player, session, clicked);
        }

        return false;
    }

    /**
     * Called on left click.
     *
     * @param player
     * @param clicked
     * @return false if you want the action to go through
     */
    public boolean handleBlockLeftClick(LocalPlayer player, WorldVector clicked) {
        if (!canUseCommand(player, "/pos1")
                && !canUseCommand(player, "/")) { return false; }
        
        LocalSession session = getSession(player);

        if (player.getItemInHand() == config.wandItem) {
            if (session.isToolControlEnabled()) {
                // Bug workaround
                if (clicked.getBlockX() == 0 && clicked.getBlockY() == 0
                        && clicked.getBlockZ() == 0) {
                    return false;
                }

                try {
                    if (session.getPos1().equals(clicked)) {
                        return false;
                    }
                } catch (IncompleteRegionException e) {
                }

                session.setPos1(clicked);
                try {
                    player.print("First position set to " + clicked
                            + " (" + session.getRegion().getSize() + ").");
                } catch (IncompleteRegionException e) {
                    player.print("First position set to " + clicked + ".");
                }

                return true;
            }
        } else if (player.isHoldingPickAxe() && session.hasSuperPickAxe()) {
            if (session.getLeftClickMode() != null) {
                return session.getLeftClickMode().act(server, config,
                        player, session, clicked);
            }
        }

        return false;
    }

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    public boolean handleCommand(LocalPlayer player, String[] split) {
        try {
            // Legacy /, command
            if (split[0].equals("/,")) {
                split[0] = "//";
            }   
            
            String searchCmd = split[0].toLowerCase();

            if (commands.containsKey(searchCmd)
                    || (config.noDoubleSlash && commands.containsKey("/" + searchCmd))
                    || ((searchCmd.length() < 3 || searchCmd.charAt(2) != '/')
                            && commands.containsKey(searchCmd.substring(1)))) {
                if (config.noDoubleSlash && commands.containsKey("/" + searchCmd)) {
                    split[0] = "/" + split[0];
                } else if (commands.containsKey(searchCmd.substring(1))) {
                    split[0] = split[0].substring(1);
                }
                
                if (canUseCommand(player, split[0].substring(1))) {
                    LocalSession session = getSession(player);
                    BlockBag blockBag = session.getBlockBag(player);
                    
                    EditSession editSession =
                            new EditSession(server, player.getWorld(),
                                    session.getBlockChangeLimit(), blockBag);
                    editSession.enableQueue();

                    long start = System.currentTimeMillis();

                    try {
                        return performCommand(player, session, editSession, split);
                    } finally {
                        session.remember(editSession);
                        editSession.flushQueue();

                        if (config.profile) {
                            long time = System.currentTimeMillis() - start;
                            player.print((time / 1000.0) + "s elapsed");
                        }
                        
                        flushBlockBag(player, editSession);
                    }
                } else {
                    player.printError("You don't have permission for this command.");
                }
            }

            return false;
        } catch (NumberFormatException e) {
            player.printError("Number expected; string given.");
        } catch (IncompleteRegionException e) {
            player.printError("The edit region has not been fully defined.");
        } catch (UnknownItemException e) {
            player.printError("Block name '" + e.getID() + "' was not recognized.");
        } catch (InvalidItemException e) {
            player.printError(e.getMessage());
        } catch (DisallowedItemException e) {
            player.printError("Block '" + e.getID() + "' not allowed (see WorldEdit configuration).");
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks changed in an operation reached ("
                    + e.getBlockLimit() + ").");
        } catch (MaxRadiusException e) {
            player.printError("Maximum radius: " + config.maxRadius);
        } catch (UnknownDirectionException e) {
            player.printError("Unknown direction: " + e.getDirection());
        } catch (InsufficientArgumentsException e) {
            player.printError(e.getMessage());
        } catch (EmptyClipboardException e) {
            player.printError("Your clipboard is empty.");
        } catch (WorldEditException e) {
            player.printError(e.getMessage());
        } catch (Throwable excp) {
            player.printError("Please report this error: [See console]");
            player.printRaw(excp.getClass().getName() + ": " + excp.getMessage());
            excp.printStackTrace();
        }

        return true;
    }
    
    /**
     * Executes a WorldEdit script.
     * 
     * @param player
     * @param filename
     * @param args
     * @throws WorldEditException 
     */
    public void runScript(LocalPlayer player, String filename, String[] args)
            throws WorldEditException {
        File dir = new File("craftscripts");
        File f = new File(dir, filename);

        if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$")) {
            player.printError("Invalid filename. Don't forget the extension.");
            return;
        }
        
        int index = filename.lastIndexOf(".");
        String ext = filename.substring(index + 1, filename.length());
        
        if (!ext.equalsIgnoreCase("js")) {
            player.printError("Only .js scripts are currently supported");
            return;
        }

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Script could not read or it does not exist.");
                return;
            }
        } catch (IOException e) {
            player.printError("Script could not read or it does not exist: " + e.getMessage());
            return;
        }
        
        String script;
        
        try {
            InputStream file;
            
            if (!f.exists()) {
                file = WorldEditController.class.getResourceAsStream(
                        "craftscripts/" + filename);
                
                if (file == null) {
                    player.printError("Script does not exist: " + filename);
                    return;
                }
            } else {
                file = new FileInputStream(f);
            }
            
            DataInputStream in = new DataInputStream(file);
            byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            script = new String(data, 0, data.length, "utf-8");
        } catch (IOException e) {
            player.printError("Script read error: " + e.getMessage());
            return;
        }
        
        LocalSession session = getSession(player);
        CraftScriptContext scriptContext =
                new CraftScriptContext(this, server, config, session, player, args);
        
        CraftScriptEngine engine = null;
        
        try {
            engine = new RhinoCraftScriptEngine();
        } catch (NoClassDefFoundError e) {
            try {
                engine = new SunRhinoCraftScriptEngine();
            } catch (NoClassDefFoundError e2) {
                player.printError("Failed to find an installed script engine.");
                return;
            }
        }
        
        engine.setTimeLimit(3000);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);
        
        try {
            engine.evaluate(script, filename, vars);
        } catch (ScriptException e) {
            player.printError("Failed to execute:");;
            player.printRaw(e.getMessage());
        } catch (NumberFormatException e) {
            throw e;
        } catch (WorldEditException e) {
            throw e;
        } catch (Throwable e) {
            player.printError("Failed to execute (exception):");
            player.printRaw(e.getClass().getCanonicalName());
        } finally {
            for (EditSession editSession : scriptContext.getEditSessions()) {
                session.remember(editSession);
            }
        }
    }
}
