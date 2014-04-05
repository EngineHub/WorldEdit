/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import com.sk89q.minecraft.util.commands.*;
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.command.*;
import com.sk89q.worldedit.command.tool.*;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.registry.BlockRegistry;
import com.sk89q.worldedit.extension.registry.MaskRegistry;
import com.sk89q.worldedit.extension.registry.PatternRegistry;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.scripting.CraftScriptContext;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.RhinoCraftScriptEngine;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.LogFormat;
import com.sk89q.worldedit.util.eventbus.EventBus;

import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The current instance of WorldEdit.
 */
public class WorldEdit {

    public static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    public final Logger commandLogger = Logger.getLogger("Minecraft.WorldEdit.CommandLogger");

    private static WorldEdit instance;
    private static String version;

    private final ServerInterface server;
    private final LocalConfiguration config;
    private final CommandsManager<LocalPlayer> commands;
    private final EventBus eventBus = new EventBus();
    private final EditSessionFactory editSessionFactory = new EditSessionFactory.EditSessionFactoryImpl(eventBus);
    private final SessionManager sessions = new SessionManager(this);

    private final BlockRegistry blockRegistry = new BlockRegistry(this);
    private final MaskRegistry maskRegistry = new MaskRegistry(this);
    private final PatternRegistry patternRegistry = new PatternRegistry(this);

    static {
        getVersion();
    }

    /**
     * Construct an instance of WorldEdit.
     *
     * @param server
     * @param config
     */
    public WorldEdit(ServerInterface server, final LocalConfiguration config) {
        instance = this;
        this.server = server;
        this.config = config;

        if (!config.logFile.equals("")) {
            try {
                FileHandler logFileHandler;
                logFileHandler = new FileHandler(new File(config.getWorkingDirectory(),
                        config.logFile).getAbsolutePath(), true);
                logFileHandler.setFormatter(new LogFormat());
                commandLogger.addHandler(logFileHandler);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not use command log file " + config.logFile + ": "
                        + e.getMessage());
            }
        }

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
                    commandLogger.info(msg);
                }
                super.invokeMethod(parent, args, player, method, instance, methodArgs, level);
            }
        };

        commands.setInjector(new SimpleInjector(this));

        reg(BiomeCommands.class);
        reg(ChunkCommands.class);
        reg(ClipboardCommands.class);
        reg(GeneralCommands.class);
        reg(GenerationCommands.class);
        reg(HistoryCommands.class);
        reg(NavigationCommands.class);
        reg(RegionCommands.class);
        reg(ScriptingCommands.class);
        reg(SelectionCommands.class);
        reg(SnapshotUtilCommands.class);
        reg(ToolUtilCommands.class);
        reg(ToolCommands.class);
        reg(UtilityCommands.class);
    }

    private void reg(Class<?> clazz) {
        server.onCommandRegistration(commands.registerAndReturn(clazz), commands);
    }

    /**
     * Gets the current instance of this class
     * 
     * @return
     */
    public static WorldEdit getInstance() {
        return instance;
    }

    /**
     * Get the event bus for WorldEdit.
     *
     * @return the event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get the block registry from which new {@link BaseBlock}s can be
     * constructed.
     *
     * @return the block registry
     */
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    /**
     * Get the mask registry from which new {@link com.sk89q.worldedit.function.mask.Mask}s
     * can be constructed.
     *
     * @return the mask registry
     */
    public MaskRegistry getMaskRegistry() {
        return maskRegistry;
    }

    /**
     * Get the pattern registry from which new {@link com.sk89q.worldedit.function.pattern.Pattern}s
     * can be constructed.
     *
     * @return the pattern registry
     */
    public PatternRegistry getPatternRegistry() {
        return patternRegistry;
    }

    /**
     * Return the session manager.
     *
     * @return the session manager
     */
    public SessionManager getSessionManager() {
        return sessions;
    }

    /**
     * @deprecated Use {@link #getSessionManager()}
     */
    @Deprecated
    public LocalSession getSession(String player) {
        return sessions.findByName(player);
    }

    /**
     * @deprecated use {@link #getSessionManager()}
     */
    @Deprecated
    public LocalSession getSession(LocalPlayer player) {
        return sessions.get(player);
    }

    /**
     * @deprecated use {@link #getSessionManager()}
     */
    @Deprecated
    public void removeSession(LocalPlayer player) {
        sessions.remove(player);
    }

    /**
     * @deprecated use {@link #getSessionManager()}
     */
    @Deprecated
    public void clearSessions() {
        sessions.clear();
    }

    /**
     * @deprecated use {@link #getSessionManager()}
     */
    @Deprecated
    public boolean hasSession(LocalPlayer player) {
        return sessions.contains(player);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed) throws WorldEditException {
        return getBlock(player, arg, allAllowed, false);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed, boolean allowNoData) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(getSession(player));
        context.setRestricted(!allAllowed);
        context.setPreferringWildcard(allowNoData);
        return getBlockRegistry().parseFromInput(arg, context);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String id) throws WorldEditException {
        return getBlock(player, id, false);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromListInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed, boolean allowNoData) throws WorldEditException {
        String[] items = list.split(",");
        Set<BaseBlock> blocks = new HashSet<BaseBlock>();
        for (String id : items) {
            blocks.add(getBlock(player, id, allAllowed, allowNoData));
        }
        return blocks;
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list, boolean allAllowed) throws WorldEditException {
        return getBlocks(player, list, allAllowed, false);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromListInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Set<BaseBlock> getBlocks(LocalPlayer player, String list) throws WorldEditException {
        return getBlocks(player, list, false);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromListInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Set<Integer> getBlockIDs(LocalPlayer player, String list, boolean allBlocksAllowed) throws WorldEditException {
        String[] items = list.split(",");
        Set<Integer> blocks = new HashSet<Integer>();
        for (String s : items) {
            blocks.add(getBlock(player, s, allBlocksAllowed).getType());
        }
        return blocks;
    }

    /**
     * @deprecated Use {@link #getPatternRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Pattern getBlockPattern(LocalPlayer player, String input) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(getSession(player));
        return Patterns.wrap(getPatternRegistry().parseFromInput(input, context));
    }

    /**
     * @deprecated Use {@link #getMaskRegistry()} ()} and {@link MaskRegistry#parseFromInput(String, ParserContext)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public Mask getBlockMask(LocalPlayer player, LocalSession session, String input) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        return Masks.wrap(getMaskRegistry().parseFromInput(input, context));
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

            if (!filePath.substring(0, dirPath.length()).equals(dirPath) && !config.allowSymlinks) {
                throw new FilenameResolutionException(filename,
                        "Path is outside allowable root");
            }

            return f;
        } catch (IOException e) {
            throw new FilenameResolutionException(filename,
                    "Failed to resolve path");
        }
    }

    public int getMaximumPolygonalPoints(LocalPlayer player) {
        if (player.hasPermission("worldedit.limit.unrestricted") || config.maxPolygonalPoints < 0) {
            return config.defaultMaxPolygonalPoints;
        }

        if (config.defaultMaxPolygonalPoints < 0) {
            return config.maxPolygonalPoints;
        }

        return Math.min(config.defaultMaxPolygonalPoints, config.maxPolygonalPoints);
    }

    public int getMaximumPolyhedronPoints(LocalPlayer player) {
        if (player.hasPermission("worldedit.limit.unrestricted") || config.maxPolyhedronPoints < 0) {
            return config.defaultMaxPolyhedronPoints;
        }

        if (config.defaultMaxPolyhedronPoints < 0) {
            return config.maxPolyhedronPoints;
        }

        return Math.min(config.defaultMaxPolyhedronPoints, config.maxPolyhedronPoints);
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
     * Checks to see if the specified brush radius is within bounds.
     *
     * @param radius
     * @throws MaxBrushRadiusException
     */
    public void checkMaxBrushRadius(double radius) throws MaxBrushRadiusException {
        if (config.maxBrushRadius > 0 && radius > config.maxBrushRadius) {
            throw new MaxBrushRadiusException();
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
        }

        return new File(config.getWorkingDirectory(), path);
    }

    /**
     * Modulus, divisor-style.
     *
     * @param a
     * @param n
     * @return
     */
    public static int divisorMod(int a, int n) {
        return (int) (a - n * Math.floor(Math.floor(a) / n));
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

        Map<Integer, Integer> missingBlocks = editSession.popMissingBlocks();

        if (missingBlocks.size() > 0) {
            StringBuilder str = new StringBuilder();
            str.append("Missing these blocks: ");
            int size = missingBlocks.size();
            int i = 0;

            for (Integer id : missingBlocks.keySet()) {
                BlockType type = BlockType.fromID(id);

                str.append(type != null
                        ? type.getName() + " (" + id + ")"
                        : id.toString());

                str.append(" [Amt: " + missingBlocks.get(id) + "]");

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
     * Handle a disconnection.
     *
     * @param player the player
     */
    @Deprecated
    public void handleDisconnect(LocalPlayer player) {
        forgetPlayer(player);
    }

    /**
     * Mark for expiration of the session.
     *
     * @param player the player
     */
    public void markExpire(LocalPlayer player) {
        sessions.markforExpiration(player);
    }

    /**
     * Forget a player.
     *
     * @param player the player
     */
    public void forgetPlayer(LocalPlayer player) {
        sessions.remove(player);
    }

    /*
     * Flush expired sessions.
     */
    public void flushExpiredSessions(SessionCheck checker) {
        sessions.removeExpired(checker);
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

            if (!player.hasPermission("worldedit.navigation.jumpto.tool")) {
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

            if (!player.hasPermission("worldedit.navigation.thru.tool")) {
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
        Request.reset();

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
            } catch (PlayerNeededException e) {
                player.printError(e.getMessage());
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
        } catch (MaxBrushRadiusException e) {
            player.printError("Maximum allowed brush size: " + config.maxBrushRadius);
        } catch (MaxRadiusException e) {
            player.printError("Maximum allowed size: " + config.maxRadius);
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
        Request.reset();

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
    public void runScript(LocalPlayer player, File f, String[] args) throws WorldEditException {
        Request.reset();

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
     * Get a factory for {@link EditSession}s.
     */
    public EditSessionFactory getEditSessionFactory() {
        return editSessionFactory;
    }

    /**
     * @deprecated EditSessionFactories are no longer used. Please register an {@link EditSessionEvent} event
     *             with the event bus in order to override or catch changes to the world
     */
    @Deprecated
    public void setEditSessionFactory(EditSessionFactory factory) {
        checkNotNull(factory);
        logger.severe("Got request to set EditSessionFactory of type " +
                factory.getClass().getName() + " from " + factory.getClass().getPackage().getName() +
                " but EditSessionFactories have been removed in favor of extending EditSession's extents.\n\n" +
                "This may mean that any block logger / intercepters addons/plugins/mods that you have installed will not " +
                "intercept WorldEdit's changes! Please notify the maintainer of the other addon about this.");
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
