// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.script.ScriptException;

import com.sk89q.minecraft.util.commands.*;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.commands.*;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.scripting.*;
import com.sk89q.worldedit.tools.*;
import com.sk89q.worldedit.masks.*;
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
    /**
     * Logger for debugging.
     */
    public static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");

    /**
     * Holds WorldEdit's version.
     */
    private static String version;

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
    private CommandsManager<LocalPlayer> commands;

    /**
     * Stores a list of WorldEdit sessions, keyed by players' names. Sessions
     * persist only for the user's session. On disconnect, the session will be
     * removed. Sessions are created only when they are needed and those
     * without any WorldEdit abilities or never use WorldEdit in a session will
     * not have a session object generated for them.
     */
    private HashMap<String, LocalSession> sessions = new HashMap<String, LocalSession>();

    /**
     * Initialize statically.
     */
    static {
        getVersion();
    }

    /**
     * Construct an instance of the plugin
     * 
     * @param server
     * @param config
     */
    public WorldEdit(ServerInterface server, final LocalConfiguration config) {
        this.server = server;
        this.config = config;

        commands = new CommandsManager<LocalPlayer>() {
            @Override
            protected void checkPermission(LocalPlayer player, Method method) throws CommandException {
                if (!player.isPlayer() && !method.isAnnotationPresent(Console.class)) {
                    throw new UnhandledCommandException();
                }

                super.checkPermission(player, method);
            }

            @Override
            public boolean hasPermission(LocalPlayer player, String perm) {
                return player.hasPermission(perm);
            }

            @Override
            public void invokeMethod(Method parent, String[] args,
                    LocalPlayer player, Method method, Object instance,
                    Object[] methodArgs, int level) throws CommandException {
                if (config.logCommands) {
                    final Logging loggingAnnotation = method.getAnnotation(Logging.class);

                    final Logging.LogMode logMode;
                    if (loggingAnnotation == null) {
                        logMode = null;
                    } else {
                        logMode = loggingAnnotation.value();
                    }

                    String msg = "WorldEdit: " + player.getName();
                    if (player.isPlayer()) {
                        msg += " (in \"" + player.getWorld().getName() + "\")";
                    }
                    msg += ": " + StringUtil.joinString(args, " ");
                    if (logMode != null && player.isPlayer()) {
                        Vector position = player.getPosition();
                        final LocalSession session = getSession(player);
                        switch (logMode) {
                        case PLACEMENT:
                            try {
                                position = session.getPlacementPosition(player);
                            } catch (IncompleteRegionException e) {
                                break;
                            }
                            /* FALL-THROUGH */

                        case POSITION:
                            msg += " - Position: " + position;
                            break;

                        case ALL:
                            msg += " - Position: " + position;
                            /* FALL-THROUGH */

                        case ORIENTATION_REGION:
                            msg += " - Orientation: " + player.getCardinalDirection().name();
                            /* FALL-THROUGH */

                        case REGION:
                            try {
                                msg += " - Region: " + session.getSelection(player.getWorld());
                            } catch (IncompleteRegionException e) {
                                break;
                            }
                            break;
                        }
                    }
                    logger.info(msg);
                }
                super.invokeMethod(parent, args, player, method, instance, methodArgs, level);
            }
        };
        
        commands.setInjector(new SimpleInjector(this));

        server.onCommandRegistration(commands.registerAndReturn(ChunkCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(ClipboardCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(GeneralCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(GenerationCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(HistoryCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(NavigationCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(RegionCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(ScriptingCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(SelectionCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(SnapshotUtilCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(ToolUtilCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(ToolCommands.class));
        server.onCommandRegistration(commands.registerAndReturn(UtilityCommands.class));
    }

    /**
     * Gets the LocalSession for a player name if it exists
     *
     * @param player
     * @return The session for the player, if it exists
     */
    public LocalSession getSession(String player) {
        return sessions.get(player);
    }

    /**
     * Gets the WorldEdit session for a player.
     *
     * @param player
     * @return
     */
    public LocalSession getSession(LocalPlayer player) {
        LocalSession session;

        synchronized (sessions) {
            if (sessions.containsKey(player.getName())) {
                return sessions.get(player.getName());
            }

            session = new LocalSession(config);

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
        }

        return session;
    }

    /**
     * Returns true if the player has a session.
     * 
     * @param player
     * @return
     */
    public boolean hasSession(LocalPlayer player) {
        // TODO: If this is indeed used in multiple threads, we should use Collections.synchronizedMap here to simplify things and exclude sources of error.
        synchronized (sessions) {
            return sessions.containsKey(player.getName());
        }
    }

    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed)
            throws UnknownItemException, DisallowedItemException {
        return getBlock(player, arg, allAllowed, false);
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
    public BaseBlock getBlock(LocalPlayer player, String arg,
                              boolean allAllowed, boolean allowNoData)
            throws UnknownItemException, DisallowedItemException {
        BlockType blockType;
        arg = arg.replace("_", " ");
        arg = arg.replace(";", "|");
        String[] blockAndExtraData = arg.split("\\|");
        String[] typeAndData = blockAndExtraData[0].split(":", 2);
        String testID = typeAndData[0];
        int blockId = -1;

        int data = -1;

        // Attempt to parse the item ID or otherwise resolve an item/block
        // name to its numeric ID
        try {
            blockId = Integer.parseInt(testID);
            blockType = BlockType.fromID(blockId);
        } catch (NumberFormatException e) {
            blockType = BlockType.lookup(testID);
            if (blockType == null) {
                int t = server.resolveItem(testID);
                if (t > 0 && t < 256) {
                    blockType = BlockType.fromID(t);
                }
            }
        }

        if (blockId == -1 && blockType == null) {
            // Maybe it's a cloth
            ClothColor col = ClothColor.lookup(testID);

            if (col != null) {
                blockType = BlockType.CLOTH;
                data = col.getID();
            } else {
                throw new UnknownItemException(arg);
            }
        }

        // Read block ID
        if (blockId == -1) {
            blockId = blockType.getID();
        }

        if (!player.getWorld().isValidBlockType(blockId)) {
            throw new UnknownItemException(arg);
        }

        if (data == -1) { // Block data not yet detected
            // Parse the block data (optional)
            try {
                data = typeAndData.length > 1 ? Integer.parseInt(typeAndData[1]) : (allowNoData ? -1 : 0);
                if (data > 15 || (data < 0 && !(allAllowed && data == -1))) {
                    data = 0;
                }
            } catch (NumberFormatException e) {
                if (blockType != null) {
                    switch (blockType) {
                        case CLOTH:
                            ClothColor col = ClothColor.lookup(typeAndData[1]);

                            if (col != null) {
                                data = col.getID();
                            } else {
                                throw new InvalidItemException(arg, "Unknown cloth color '" + typeAndData[1] + "'");
                            }
                            break;

                        case STEP:
                        case DOUBLE_STEP:
                            BlockType dataType = BlockType.lookup(typeAndData[1]);

                            if (dataType != null) {
                                switch (dataType) {
                                    case STONE:
                                        data = 0;
                                        break;

                                    case SANDSTONE:
                                        data = 1;
                                        break;

                                    case WOOD:
                                        data = 2;
                                        break;

                                    case COBBLESTONE:
                                        data = 3;
                                        break;
                                    case BRICK:
                                        data = 4;
                                        break;
                                    case STONE_BRICK:
                                        data = 5;

                                    default:
                                        throw new InvalidItemException(arg, "Invalid step type '" + typeAndData[1] + "'");
                                }
                            } else {
                                throw new InvalidItemException(arg, "Unknown step type '" + typeAndData[1] + "'");
                            }
                            break;

                        default:
                            throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
                    }
                } else {
                    throw new InvalidItemException(arg, "Unknown data value '" + typeAndData[1] + "'");
                }
            }
        }

        // Check if the item is allowed
        if (allAllowed || player.hasPermission("worldedit.anyblock") || !config.disallowedBlocks.contains(blockId)) {
            if (blockType != null) {
                switch (blockType) {
                    case SIGN_POST:
                    case WALL_SIGN:
                        // Allow special sign text syntax
                        String[] text = new String[4];
                        text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
                        text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
                        text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
                        text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
                        return new SignBlock(blockType.getID(), data, text);

                    case MOB_SPAWNER:
                        // Allow setting mob spawn type
                        if (blockAndExtraData.length > 1) {
                            String mobName = blockAndExtraData[1];
                            for (MobType mobType : MobType.values()) {
                                if (mobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
                                    mobName = mobType.getName();
                                    break;
                                }
                            }
                            if (!server.isValidMobType(mobName)) {
                                throw new InvalidItemException(arg, "Unknown mob type '" + mobName + "'");
                            }
                            return new MobSpawnerBlock(data, mobName);
                        } else {
                            return new MobSpawnerBlock(data, MobType.PIG.getName());
                        }

                    case NOTE_BLOCK:
                        // Allow setting note
                        if (blockAndExtraData.length > 1) {
                            byte note = Byte.parseByte(blockAndExtraData[1]);
                            if (note < 0 || note > 24) {
                                throw new InvalidItemException(arg, "Out of range note value: '" + blockAndExtraData[1] + "'");
                            } else {
                                return new NoteBlock(data, note);
                            }
                        } else {
                            return new NoteBlock(data, (byte) 0);
                        }

                    default:
                        return new BaseBlock(blockId, data);
                }
            } else {
                return new BaseBlock(blockId, data);
            }
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

    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed, boolean allowNoData)
            throws DisallowedItemException, UnknownItemException {
        String[] items = list.split(",");
        Set<BaseBlock> blocks = new HashSet<BaseBlock>();
        for (String id : items) {
            blocks.add(getBlock(player, id, allAllowed, allowNoData));
        }
        return blocks;
    }

    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed)
            throws DisallowedItemException, UnknownItemException {
        return getBlocks(player, list, allAllowed, false);
    }

    public Set<BaseBlock> getBlocks(LocalPlayer player, String list)
            throws DisallowedItemException, UnknownItemException {
        return getBlocks(player, list, false);
    }

    /**
     * Returns a Pattern corresponding to the specified pattern string,
     * as given by the player on the command line.
     *
     * @param player
     * @param patternString
     * @return pattern
     * @throws UnknownItemException 
     * @throws DisallowedItemException 
     */
    public Pattern getBlockPattern(LocalPlayer player, String patternString)
            throws UnknownItemException, DisallowedItemException {

        String[] items = patternString.split(",");

        // Handle special block pattern types
        if (patternString.charAt(0) == '#') {
            if (patternString.equals("#clipboard") || patternString.equals("#copy")) {
                LocalSession session = getSession(player);
                CuboidClipboard clipboard;

                try {
                    clipboard = session.getClipboard();
                } catch (EmptyClipboardException e) {
                    player.printError("Copy a selection first with //copy.");
                    throw new UnknownItemException("#clipboard");
                }

                return new ClipboardPattern(clipboard);
            } else {
                throw new UnknownItemException(patternString);
            }
        }

        // If it's only one block, then just return that single one
        if (items.length == 1) {
            return new SingleBlockPattern(getBlock(player, items[0]));
        }

        List<BlockChance> blockChances = new ArrayList<BlockChance>();

        for (String s : items) {
            BaseBlock block;

            double chance;

            // Parse special percentage syntax
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
     * Get a block mask. Block masks are used to determine which
     * blocks to include when replacing.
     *
     * @param player
     * @param session
     * @param maskString
     * @return
     * @throws WorldEditException
     */
    public Mask getBlockMask(LocalPlayer player, LocalSession session,
            String maskString) throws WorldEditException {
        List<Mask> masks = new ArrayList<Mask>();

        for (String component : maskString.split(" ")) {
            if (component.length() == 0) {
                continue;
            }

            Mask current = getBlockMaskComponent(player, session, masks, component);

            masks.add(current);
        }

        switch (masks.size()) {
        case 0:
            return null;

        case 1:
            return masks.get(0);

        default:
            return new CombinedMask(masks);
        }
    }

    private Mask getBlockMaskComponent(LocalPlayer player, LocalSession session, List<Mask> masks, String component) throws IncompleteRegionException, UnknownItemException, DisallowedItemException {
        final char firstChar = component.charAt(0);
        switch (firstChar) {
        case '#':
            if (component.equalsIgnoreCase("#existing")) {
                return new ExistingBlockMask();
            } else if (component.equalsIgnoreCase("#selection")
                    || component.equalsIgnoreCase("#region")
                    || component.equalsIgnoreCase("#sel")) {
                return new RegionMask(session.getSelection(player.getWorld()));
            } else {
                throw new UnknownItemException(component);
            }

        case '>':
        case '<':
            final LocalWorld world = player.getWorld();
            final boolean over = firstChar == '>';
            final String idString = component.substring(1);
            final Set<Integer> ids = new HashSet<Integer>();

            if (!(idString.equals("*") || idString.equals(""))) {
                for (String sid : idString.split(",")) {
                    try {
                        final int pid = Integer.parseInt(sid);
                        if (!world.isValidBlockType(pid)) {
                            throw new UnknownItemException(sid);
                        }
                        ids.add(pid);
                    } catch (NumberFormatException e) {
                        final BlockType type = BlockType.lookup(sid);
                        final int id = type.getID();
                        if (!world.isValidBlockType(id)) {
                            throw new UnknownItemException(sid);
                        }
                        ids.add(id);
                    }
                }
            }

            return new UnderOverlayMask(ids, over);

        case '!':
            if (component.length() > 1) {
                return new InvertedBlockTypeMask(getBlockIDs(player, component.substring(1), true));
            }

        default:
            return new BlockTypeMask(getBlockIDs(player, component, true));
        }
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param player
     * @param list
     * @param allBlocksAllowed
     * @return set
     * @throws UnknownItemException 
     * @throws DisallowedItemException 
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
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a <code>java.io.File</code> object will be returned.
     * 
     * @param player 
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return
     * @throws FilenameException
     */
    public File getSafeSaveFile(LocalPlayer player, File dir, String filename,
            String defaultExt, String... extensions)
            throws FilenameException {
        return getSafeFile(player, dir, filename, defaultExt, extensions, true);
    }

    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a <code>java.io.File</code> object will be returned.
     * 
     * @param player 
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return
     * @throws FilenameException
     */
    public File getSafeOpenFile(LocalPlayer player, File dir, String filename,
            String defaultExt, String... extensions)
            throws FilenameException {
        return getSafeFile(player, dir, filename, defaultExt, extensions, false);
    }

    /**
     * Get a safe path to a file.
     * 
     * @param player
     * @param dir
     * @param filename
     * @param defaultExt
     * @param extensions
     * @param isSave
     * @return
     * @throws FilenameException
     */
    private File getSafeFile(LocalPlayer player, File dir, String filename,
            String defaultExt, String[] extensions, boolean isSave)
            throws FilenameException {
        if (extensions != null && (extensions.length == 1 && extensions[0] == null)) extensions = null;

        File f;

        if (filename.equals("#")) {
            if (isSave) {
                f = player.openFileSaveDialog(extensions);
            } else {
                f = player.openFileOpenDialog(extensions);
            }

            if (f == null) {
                throw new FileSelectionAbortedException("No file selected");
            }
        } else {
            if (defaultExt != null && filename.lastIndexOf('.') == -1) {
                filename += "." + defaultExt;
            }

            if (!filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$")) {
                throw new InvalidFilenameException(filename, "Invalid characters or extension missing");
            }

            f = new File(dir, filename);
        }

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                throw new FilenameResolutionException(filename,
                        "Path is outside allowable root");
            }

            return f;
        } catch (IOException e) {
            throw new FilenameResolutionException(filename,
                    "Failed to resolve path");
        }
    }

    /**
     * Checks to see if the specified radius is within bounds.
     *
     * @param radius
     * @throws MaxRadiusException
     */
    public void checkMaxRadius(double radius) throws MaxRadiusException {
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
        return (int) (a - n * Math.floor(Math.floor(a) / (double) n));
    }

    /**
     * Get the direction vector for a player's direction. May return
     * null if a direction could not be found.
     * 
     * @param player
     * @param dirStr 
     * @return
     * @throws UnknownDirectionException 
     */
    public Vector getDirection(LocalPlayer player, String dirStr)
            throws UnknownDirectionException {

        dirStr = dirStr.toLowerCase();

        final PlayerDirection dir = getPlayerDirection(player, dirStr);

        switch (dir) {
        case WEST:
        case EAST:
        case SOUTH:
        case NORTH:
        case UP:
        case DOWN:
            return dir.vector();

        default:
            throw new UnknownDirectionException(dir.name());
        }
    }

    private PlayerDirection getPlayerDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
        final PlayerDirection dir;

        switch (dirStr.charAt(0)) {
        case 'w':
            dir = PlayerDirection.WEST;
            break;

        case 'e':
            dir = PlayerDirection.EAST;
            break;

        case 's':
            if (dirStr.indexOf('w') > 0) {
                return PlayerDirection.SOUTH_WEST;
            }

            if (dirStr.indexOf('e') > 0) {
                return PlayerDirection.SOUTH_EAST;
            }
            dir = PlayerDirection.SOUTH;
            break;

        case 'n':
            if (dirStr.indexOf('w') > 0) {
                return PlayerDirection.NORTH_WEST;
            }

            if (dirStr.indexOf('e') > 0) {
                return PlayerDirection.NORTH_EAST;
            }
            dir = PlayerDirection.NORTH;
            break;

        case 'u':
            dir = PlayerDirection.UP;
            break;

        case 'd':
            dir = PlayerDirection.DOWN;
            break;

        case 'm': // me
        case 'f': // forward
            dir = player.getCardinalDirection(0);
            break;

        case 'b': // back
            dir = player.getCardinalDirection(180);
            break;

        case 'l': // left
            dir = player.getCardinalDirection(-90);
            break;

        case 'r': // right
            dir = player.getCardinalDirection(90);
            break;

        default:
            throw new UnknownDirectionException(dirStr);
        }
        return dir;
    }

    /**
     * Get diagonal direction vector for a player's direction. May return
     * null if a direction could not be found.
     * 
     * @param player
     * @param dirStr 
     * @return
     * @throws UnknownDirectionException 
     */
    public Vector getDiagonalDirection(LocalPlayer player, String dirStr)
            throws UnknownDirectionException {

        return getPlayerDirection(player, dirStr.toLowerCase()).vector();
    }

    /**
     * Get the flip direction for a player's direction.
     *
     * @param player
     * @param dirStr 
     * @return
     * @throws UnknownDirectionException 
     */
    public FlipDirection getFlipDirection(LocalPlayer player, String dirStr)
            throws UnknownDirectionException {

        final PlayerDirection dir = getPlayerDirection(player, dirStr);
        switch (dir) {
        case WEST:
        case EAST:
            return FlipDirection.WEST_EAST;

        case NORTH:
        case SOUTH:
            return FlipDirection.NORTH_SOUTH;

        case UP:
        case DOWN:
            return FlipDirection.UP_DOWN;

        default:
            throw new UnknownDirectionException(dir.name());
        }
    }

    /**
     * Remove a session.
     * 
     * @param player
     */
    public void removeSession(LocalPlayer player) {
        synchronized (sessions) {
            sessions.remove(player.getName());
        }
    }

    /**
     * Remove all sessions.
     */
    public void clearSessions() {
        synchronized (sessions) {
            sessions.clear();
        }
    }

    /**
     * Flush a block bag's changes to a player.
     * 
     * @param player
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

                ++i;

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
     * @return the commands
     */
    public CommandsManager<LocalPlayer> getCommandsManager() {
        return commands;
    }

    /**
     *
     * @param player
     */
    @Deprecated
    public void handleDisconnect(LocalPlayer player) {
        forgetPlayer(player);
    }

    /**
     *
     * @param player
     */
    public void markExpire(LocalPlayer player) {
        synchronized (sessions) {
            LocalSession session = sessions.get(player.getName());
            if (session != null) {
                session.update();
            }
        }
    }

    /**
     * Forget a player.
     *
     * @param player
     */
    public void forgetPlayer(LocalPlayer player) {
        removeSession(player);
    }

    /*
     * Flush expired sessions.
     */
    public void flushExpiredSessions(SessionCheck checker) {
        synchronized (sessions) {
            Iterator<Map.Entry<String, LocalSession>> it = sessions.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, LocalSession> entry = it.next();
                if (entry.getValue().hasExpired()
                        && !checker.isOnlinePlayer(entry.getKey())) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Called on arm swing.
     * 
     * @param player
     * @return 
     */
    public boolean handleArmSwing(LocalPlayer player) {
        if (player.getItemInHand() == config.navigationWand) {
            if (config.navigationWandMaxDistance <= 0) {
                return false;
            }

            if (!player.hasPermission("worldedit.navigation.jumpto.tool")
                    && !player.hasPermission("worldedit.navigation.jumpto")) { // TODO: Remove old permission
                return false;
            }

            WorldVector pos = player.getSolidBlockTrace(config.navigationWandMaxDistance);
            if (pos != null) {
                player.findFreePosition(pos);
            } else {
                player.printError("No block in sight (or too far)!");
            }
            return true;
        }

        LocalSession session = getSession(player);

        Tool tool = session.getTool(player.getItemInHand());
        if (tool != null && tool instanceof DoubleActionTraceTool) {
            if (tool.canUse(player)) {
                ((DoubleActionTraceTool) tool).actSecondary(server, config, player, session);
                return true;
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
        if (player.getItemInHand() == config.navigationWand) {
            if (config.navigationWandMaxDistance <= 0) {
                return false;
            }

            if (!player.hasPermission("worldedit.navigation.thru.tool")
                    && !player.hasPermission("worldedit.navigation.thru")) { // TODO: Remove old permission
                return false;
            }

            if (!player.passThroughForwardWall(40)) {
                player.printError("Nothing to pass through!");
            }

            return true;
        }

        LocalSession session = getSession(player);

        Tool tool = session.getTool(player.getItemInHand());
        if (tool != null && tool instanceof TraceTool) {
            if (tool.canUse(player)) {
                ((TraceTool) tool).actPrimary(server, config, player, session);
                return true;
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
        LocalSession session = getSession(player);

        if (player.getItemInHand() == config.wandItem) {
            if (!session.isToolControlEnabled()) {
                return false;
            }

            if (!player.hasPermission("worldedit.selection.pos")) {
                return false;
            }

            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectSecondary(clicked)) {
                selector.explainSecondarySelection(player, session, clicked);
            }

            return true;
        }

        Tool tool = session.getTool(player.getItemInHand());
        if (tool != null && tool instanceof BlockTool) {
            if (tool.canUse(player)) {
                ((BlockTool) tool).actPrimary(server, config, player, session, clicked);
                return true;
            }
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
            if (!session.isToolControlEnabled()) {
                return false;
            }

            if (!player.hasPermission("worldedit.selection.pos")) {
                return false;
            }

            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectPrimary(clicked)) {
                selector.explainPrimarySelection(player, session, clicked);
            }

            return true;
        }

        if (player.isHoldingPickAxe() && session.hasSuperPickAxe()) {
            final BlockTool superPickaxe = session.getSuperPickaxe();
            if (superPickaxe != null && superPickaxe.canUse(player)) {
                return superPickaxe.actPrimary(server, config, player, session, clicked);
            }
        }

        Tool tool = session.getTool(player.getItemInHand());
        if (tool != null && tool instanceof DoubleActionBlockTool) {
            if (tool.canUse(player)) {
                ((DoubleActionBlockTool) tool).actSecondary(server, config, player, session, clicked);
                return true;
            }
        }

        return false;
    }

    private static final java.util.regex.Pattern numberFormatExceptionPattern = java.util.regex.Pattern.compile("^For input string: \"(.*)\"$");

    /**
     *
     * @param player
     * @param split
     * @return whether the command was processed
     */
    public boolean handleCommand(LocalPlayer player, String[] split) {
        try {
            split = commandDetection(split);

            // No command found!
            if (!commands.hasCommand(split[0])) {
                return false;
            }

            LocalSession session = getSession(player);
            EditSession editSession = session.createEditSession(player);
            editSession.enableQueue();

            session.tellVersion(player);

            long start = System.currentTimeMillis();

            try {
                commands.execute(split, player, session, player, editSession);
            } catch (CommandPermissionsException e) {
                player.printError("You don't have permission to do this.");
            } catch (MissingNestedCommandException e) {
                player.printError(e.getUsage());
            } catch (CommandUsageException e) {
                player.printError(e.getMessage());
                player.printError(e.getUsage());
            } catch (WrappedCommandException e) {
                throw e.getCause();
            } catch (UnhandledCommandException e) {
                player.printError("Command could not be handled; invalid sender!");
                return false;
            } finally {
                session.remember(editSession);
                editSession.flushQueue();

                if (config.profile) {
                    long time = System.currentTimeMillis() - start;
                    int changed = editSession.getBlockChangeCount();
                    if (time > 0) {
                        double throughput = changed / (time / 1000.0);
                        player.printDebug((time / 1000.0) + "s elapsed (history: "
                                + changed + " changed; "
                                + Math.round(throughput) + " blocks/sec).");
                    } else {
                        player.printDebug((time / 1000.0) + "s elapsed.");
                    }
                }

                flushBlockBag(player, editSession);
            }
        } catch (NumberFormatException e) {
            final Matcher matcher = numberFormatExceptionPattern.matcher(e.getMessage());

            if (matcher.matches()) {
                player.printError("Number expected; string \"" + matcher.group(1) + "\" given.");
            } else {
                player.printError("Number expected; string given.");
            }
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
        } catch (InvalidFilenameException e) {
            player.printError("Filename '" + e.getFilename() + "' invalid: "
                    + e.getMessage());
        } catch (FilenameResolutionException e) {
            player.printError("File '" + e.getFilename() + "' resolution error: "
                    + e.getMessage());
        } catch (InvalidToolBindException e) {
            player.printError("Can't bind tool to "
                    + ItemType.toHeldName(e.getItemId()) + ": " + e.getMessage());
        } catch (FileSelectionAbortedException e) {
            player.printError("File selection aborted.");
        } catch (WorldEditException e) {
            player.printError(e.getMessage());
        } catch (Throwable excp) {
            player.printError("Please report this error: [See console]");
            player.printRaw(excp.getClass().getName() + ": " + excp.getMessage());
            excp.printStackTrace();
        }

        return true;
    }
    
    public String[] commandDetection(String[] split) {
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

        // Try to detect the command
        if (commands.hasCommand(searchCmd)) {
        } else if (config.noDoubleSlash && commands.hasCommand("/" + searchCmd)) {
            split[0] = "/" + split[0];
        } else if (split[0].length() >= 2 && split[0].charAt(0) == '/'
                && commands.hasCommand(searchCmd.substring(1))) {
            split[0] = split[0].substring(1);
        }
        return split;
    }

    /**
     * Executes a WorldEdit script.
     * 
     * @param player
     * @param f
     * @param args
     * @throws WorldEditException 
     */
    public void runScript(LocalPlayer player, File f, String[] args)
            throws WorldEditException {
        String filename = f.getPath();
        int index = filename.lastIndexOf(".");
        String ext = filename.substring(index + 1, filename.length());

        if (!ext.equalsIgnoreCase("js")) {
            player.printError("Only .js scripts are currently supported");
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
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
        }

        engine.setTimeLimit(config.scriptTimeout);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);

        try {
            engine.evaluate(script, filename, vars);
        } catch (ScriptException e) {
            player.printError("Failed to execute:");
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
                editSession.flushQueue();
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

    /**
     * Get the server interface.
     * 
     * @return
     */
    public ServerInterface getServer() {
        return server;
    }

    /**
     * Get the version.
     *
     * @return
     */
    public static String getVersion() {
        if (version != null) {
            return version;
        }

        Package p = WorldEdit.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.sk89q.worldedit");
        }

        if (p == null) {
            version = "(unknown)";
        } else {
            version = p.getImplementationVersion();

            if (version == null) {
                version = "(unknown)";
            }
        }

        return version;
    }

    public static void setVersion(String version) {
        WorldEdit.version = version;
    }
}
