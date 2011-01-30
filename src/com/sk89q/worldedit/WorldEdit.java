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
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.LocalSession.CompassMode;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.commands.*;
import com.sk89q.worldedit.scripting.*;
import com.sk89q.worldedit.patterns.*;

/**
 * This class is the main entry point for WorldEdit. All events are routed
 * to an instance of this controller for processing by WorldEdit. For
 * integrating WorldEdit in other platforms, an instance of this class
 * should be created and events should be redirected to it.
 *
 * @author sk89q
 */
public class WorldEdit {
    public static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    
    /**
     * Interface to the server.
     */
    private ServerInterface server;
    
    /**
     * Configuration. This is a subclass.
     */
    private LocalConfiguration config;
    
    /**
     * List of commands.
     */
    private CommandsManager commands;
    
    /**
     * Stores a list of WorldEdit sessions, keyed by players' names. Sessions
     * persist only for the user's session. On disconnect, the session will be
     * removed. Sessions are created only when they are needed and those
     * without any WorldEdit abilities or never use WorldEdit in a session will
     * not have a session object generated for them.
     */
    private HashMap<String,LocalSession> sessions =
            new HashMap<String,LocalSession>();
    
    /**
     * Construct an instance of the plugin
     * 
     * @param server
     * @param config
     */
    public WorldEdit(ServerInterface server, LocalConfiguration config) {
        this.server = server;
        this.config = config;
        
        commands = new CommandsManager();

        commands.register(ChunkCommands.class);
        commands.register(ClipboardCommands.class);
        commands.register(GeneralCommands.class);
        commands.register(GenerationCommands.class);
        commands.register(HistoryCommands.class);
        commands.register(NavigationCommands.class);
        commands.register(RegionCommands.class);
        commands.register(ScriptingCommands.class);
        commands.register(SelectionCommands.class);
        commands.register(SnapshotCommands.class);
        commands.register(SuperPickaxeCommands.class);
        commands.register(BrushShapeCommands.class);
        commands.register(UtilityCommands.class);
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    public LocalSession getSession(LocalPlayer player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        }
        
        LocalSession session = new LocalSession();
        
        // Set the limit on the number of blocks that an operation can
        // change at once, or don't if the player has an override or there
        // is no limit. There is also a default limit
        if (!player.hasPermission("worldedit.limit.unrestricted")
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
                        || !player.hasPermission("worldedit.inventory.unrestricted")));
        
        // Remember the session
        sessions.put(player.getName(), session);
        
        return session;
    }

    /**
     * Returns true if the player has a session.
     * 
     * @param player
     * @return
     */
    public boolean hasSession(LocalPlayer player) {
        return sessions.containsKey(player.getName());
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
        
        // Parse the block data (optional)
        try {
            data = args1.length > 1 ? Integer.parseInt(args1[1]) : 0;
            if (data > 15 || data < 0) {
                data = 0;
            }
        } catch (NumberFormatException e) {
            if (blockType == BlockType.CLOTH) {
                ClothColor col = ClothColor.lookup(args1[1]);
                
                if (col != null) {
                    data = col.getID();
                } else {
                    throw new InvalidItemException(arg, "Unknown cloth color '" + args1[1] + "'");
                }
            } else {
                throw new InvalidItemException(arg, "Unknown data value '" + args1[1] + "'");
            }
        }

        // Check if the item is allowed
        if (allAllowed || player.hasPermission("worldedit.anyblock")
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
            
            // Allow setting mob spawn type
            } else if (blockType == BlockType.MOB_SPAWNER) {
                if (args0.length > 1) {
                    if (!server.isValidMobType(args0[1])) {
                        throw new InvalidItemException(arg, "Unknown mob type '" + args0[1] + "'");
                    }
                    return new MobSpawnerBlock(data, args0[1]);
                } else {
                    return new MobSpawnerBlock(data, "Pig");
                }
            
            // Allow setting note
            } else if (blockType == BlockType.NOTE_BLOCK) {
                if (args0.length > 1) {
                    byte note = Byte.parseByte(args0[1]);
                    if (note < 0 || note > 24) {
                        throw new InvalidItemException(arg, "Out of range note value: '" + args0[1] + "'");
                    } else {
                        return new NoteBlock(data, note);
                    }
                } else {
                    return new NoteBlock(data, (byte)0);
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
     * Checks to see if the specified radius is within bounds.
     *
     * @param radius
     * @throws MaxRadiusException
     */
    public void checkMaxRadius(int radius) throws MaxRadiusException {
        if (config.maxRadius > 0 && radius > config.maxRadius) {
            throw new MaxRadiusException();
        }
    }
    
    /**
     * Get a file relative to the defined working directory. If the specified
     * path is absolute, then the working directory is not used.
     * 
     * @param path
     * @return
     */
    public File getWorkingDirectoryFile(String path) {
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        } else {
            return new File(config.getWorkingDirectory(), path);
        }
    }

    /**
     * Modulus, divisor-style.
     * 
     * @param a
     * @param n
     * @return
     */
    public static int divisorMod(int a, int n) {
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
        
        PlayerDirection dir = null;
        
        dirStr = dirStr.toLowerCase();
        boolean wasDetected = false;

        if (dirStr.equals("me")) {
            dir = player.getCardinalDirection();
            wasDetected = true;
        }

        if (dirStr.charAt(0) == 'w' || dir == PlayerDirection.WEST) {
            zm += 1;
        } else if (dirStr.charAt(0) == 'e' || dir == PlayerDirection.EAST) {
            zm -= 1;
        } else if (dirStr.charAt(0) == 's' || dir == PlayerDirection.SOUTH) {
            xm += 1;
        } else if (dirStr.charAt(0) == 'n' || dir == PlayerDirection.NORTH) {
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
        PlayerDirection dir = null;
        
        if (dirStr.equals("me")) {
            dir = player.getCardinalDirection();
        }

        if (dirStr.charAt(0) == 'w' || dir == PlayerDirection.EAST) {
            return CuboidClipboard.FlipDirection.WEST_EAST;
        } else if (dirStr.charAt(0) == 'e' || dir == PlayerDirection.EAST) {
            return CuboidClipboard.FlipDirection.WEST_EAST;
        } else if (dirStr.charAt(0) == 's' || dir == PlayerDirection.SOUTH) {
            return CuboidClipboard.FlipDirection.NORTH_SOUTH;
        } else if (dirStr.charAt(0) == 'n' || dir == PlayerDirection.SOUTH) {
            return CuboidClipboard.FlipDirection.NORTH_SOUTH;
        } else if (dirStr.charAt(0) == 'u') {
            return CuboidClipboard.FlipDirection.UP_DOWN;
        } else if (dirStr.charAt(0) == 'd') {
            return CuboidClipboard.FlipDirection.UP_DOWN;
        } else {
            throw new UnknownDirectionException(dir.name());
        }
    }

    /**
     * Remove a session.
     * 
     * @param player
     */
    public void removeSession(LocalPlayer player) {
        sessions.remove(player.getName());
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
    public void flushBlockBag(LocalPlayer player,
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
     * @return the commands
     */
    public Map<String, String> getCommands() {
        return commands.getCommands();
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
                && config.navigationWandMaxDistance > 0) {
            CompassMode mode = session.getCompassMode();
            
            if (player.hasPermission("worldedit.navigation.jumpto") && mode == CompassMode.JUMPTO) {
                WorldVector pos = player.getSolidBlockTrace(config.navigationWandMaxDistance);
                if (pos != null) {
                    player.findFreePosition(pos);
                } else {
                    player.printError("No block in sight (or too far)!");
                }
            } else if (mode == CompassMode.THRU) { // Permission is implied
                if (!player.passThroughForwardWall(40)) {
                    player.printError("Nothing to pass through!");
                }
            }
        }
        
        return false;
    }

    /**
     * Called on right click (not on a block).
     * 
     * @param player
     * @return 
     */
    public boolean handleRightClick(LocalPlayer player) {
        LocalSession session = getSession(player);
        
        if (player.isHoldingPickAxe()) {
            if (session.getArmSwingMode() != null) {
                session.getArmSwingMode().act(server, config,
                        player, session, null);
                return true;
            }
        } else if (player.getItemInHand() == config.navigationWand) {
            CompassMode mode = session.getCompassMode();
            
            if (mode == CompassMode.JUMPTO) {
                if (player.hasPermission("worldedit.navigation.thru")) {
                    session.setCompassMode(CompassMode.THRU);
                    player.print("Switched to /thru mode.");
                } else {
                    player.printError("You don't have permission for /thru.");
                }
            } else {
                if (player.hasPermission("worldedit.navigation.jumpto")) {
                    session.setCompassMode(CompassMode.JUMPTO);
                    player.print("Switched to /jumpto mode.");
                } else {
                    player.printError("You don't have permission for /jumpto.");
                }
            }
            
            return true;
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
                && player.hasPermission("worldedit.selection.pos")) {
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
        LocalSession session = getSession(player);

        if (player.getItemInHand() == config.wandItem) {
            if (session.isToolControlEnabled()
                    && player.hasPermission("worldedit.selection.pos")) {
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
            split[0] = split[0].substring(1);
            
            // Quick script shortcut
            if (split[0].matches("^[^/].*\\.js$")) {
                String[] newSplit = new String[split.length + 1];
                System.arraycopy(split, 0, newSplit, 1, split.length);
                newSplit[0] = "cs";
                newSplit[1] = newSplit[1];
                split = newSplit;
            }
            
            String searchCmd = split[0].toLowerCase();
            if (commands.hasCommand(searchCmd)
                    || (config.noDoubleSlash && commands.hasCommand("/" + searchCmd))
                    || (searchCmd.length() >= 2 && searchCmd.charAt(0) == '/'
                            && commands.hasCommand(searchCmd.substring(1)))) {
                if (config.noDoubleSlash && commands.hasCommand("/" + searchCmd)) {
                    split[0] = "/" + split[0];
                } else if (commands.hasCommand(searchCmd.substring(1))) {
                    split[0] = split[0].substring(1);
                }
            
                LocalSession session = getSession(player);
                BlockBag blockBag = session.getBlockBag(player);
                
                EditSession editSession =
                        new EditSession(server, player.getWorld(),
                                session.getBlockChangeLimit(), blockBag);
                editSession.enableQueue();

                long start = System.currentTimeMillis();

                try {
                    if (config.logCommands) {
                        logger.log(Level.INFO, "WorldEdit: " + player.getName() + ": "
                                + StringUtil.joinString(split, " "));
                    }
                    
                    return commands.execute(new CommandContext(split), this,
                            session, player, editSession);
                } finally {
                    session.remember(editSession);
                    editSession.flushQueue();

                    if (config.profile) {
                        long time = System.currentTimeMillis() - start;
                        player.print((time / 1000.0) + "s elapsed");
                    }
                    
                    flushBlockBag(player, editSession);
                }
            }

            return false;
        } catch (NumberFormatException e) {
            player.printError("Number expected; string given.");
        } catch (IncompleteRegionException e) {
            player.printError("Make a region selection first.");
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
            player.printError("Your clipboard is empty. Use //copy first.");
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
        File dir = getWorkingDirectoryFile(config.scriptsDir);
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
                file = WorldEdit.class.getResourceAsStream(
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
        
        engine.setTimeLimit(config.scriptTimeout);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);
        
        try {
            engine.evaluate(script, filename, vars);
        } catch (ScriptException e) {
            player.printError("Failed to execute:");;
            player.printRaw(e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            throw e;
        } catch (WorldEditException e) {
            throw e;
        } catch (Throwable e) {
            player.printError("Failed to execute (see console):");
            player.printRaw(e.getClass().getCanonicalName());
            e.printStackTrace();
        } finally {
            for (EditSession editSession : scriptContext.getEditSessions()) {
                session.remember(editSession);
            }
        }
    }
    
    /**
     * Get Worldedit's configuration.
     * 
     * @return
     */
    public LocalConfiguration getConfiguration() {
        return config;
    }
}
