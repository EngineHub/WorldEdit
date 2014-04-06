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

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.command.tool.*;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
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
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.logging.WorldEditPrefixHandler;

import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The entry point and container for a working implementation of WorldEdit.
 * </p>
 * An instance handles event handling; block, mask, pattern, etc. registration;
 * the management of sessions; the creation of {@link EditSession}s; and more.
 * In order to use WorldEdit, at least one {@link Platform} must be registered
 * with WorldEdit using {@link PlatformManager#register(Platform)} on the
 * manager retrieved using {@link WorldEdit#getPlatformManager()}.
 * </p>
 * An instance of WorldEdit can be retrieved using the static
 * method {@link WorldEdit#getInstance()}, which is shared among all
 * platforms within the same classloader hierarchy.
 */
public class WorldEdit {

    public static final Logger logger = Logger.getLogger(WorldEdit.class.getCanonicalName());

    private final static WorldEdit instance = new WorldEdit();
    private static String version;

    private final EventBus eventBus = new EventBus();
    private final PlatformManager platformManager = new PlatformManager(this);
    private final EditSessionFactory editSessionFactory = new EditSessionFactory.EditSessionFactoryImpl(eventBus);
    private final SessionManager sessions = new SessionManager(this);

    private final BlockRegistry blockRegistry = new BlockRegistry(this);
    private final MaskRegistry maskRegistry = new MaskRegistry(this);
    private final PatternRegistry patternRegistry = new PatternRegistry(this);

    static {
        WorldEditPrefixHandler.register("com.sk89q.worldedit");
        getVersion();
    }

    private WorldEdit() {
    }

    /**
     * Gets the current instance of this class.
     * </p>
     * An instance will always be available, but no platform may yet be
     * registered with WorldEdit, meaning that a number of operations
     * may fail. However, event handlers can be registered.
     *
     * @return an instance of WorldEdit.
     */
    public static WorldEdit getInstance() {
        return instance;
    }

    /**
     * Get the platform manager, where platforms (that implement WorldEdit)
     * can be registered and information about registered platforms can
     * be queried.
     *
     * @return the platform manager
     */
    public PlatformManager getPlatformManager() {
        return platformManager;
    }

    /**
     * Get the event bus for WorldEdit.
     * </p>
     * Event handlers can be registered on the event bus.
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
    @SuppressWarnings("deprecation")
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String arg, boolean allAllowed) throws WorldEditException {
        return getBlock(player, arg, allAllowed, false);
    }

    /**
     * @deprecated Use {@link #getBlockRegistry()} and {@link BlockRegistry#parseFromInput(String, ParserContext)}
     */
    @SuppressWarnings("deprecation")
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
    @SuppressWarnings("deprecation")
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
     * On success, a {@code java.io.File} object will be returned.
     *
     * @param player the player
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
     */
    public File getSafeSaveFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions)
            throws FilenameException {
        return getSafeFile(player, dir, filename, defaultExt, extensions, true);
    }

    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a {@code java.io.File} object will be returned.
     *
     * @param player the player
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
     */
    public File getSafeOpenFile(LocalPlayer player, File dir, String filename, String defaultExt, String... extensions)
            throws FilenameException {
        return getSafeFile(player, dir, filename, defaultExt, extensions, false);
    }

    /**
     * Get a safe path to a file.
     *
     * @param player the player
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @param isSave true if the purpose is for saving
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
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

            if (!filePath.substring(0, dirPath.length()).equals(dirPath) && !getConfiguration().allowSymlinks) {
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
        if (player.hasPermission("worldedit.limit.unrestricted") || getConfiguration().maxPolygonalPoints < 0) {
            return getConfiguration().defaultMaxPolygonalPoints;
        }

        if (getConfiguration().defaultMaxPolygonalPoints < 0) {
            return getConfiguration().maxPolygonalPoints;
        }

        return Math.min(getConfiguration().defaultMaxPolygonalPoints, getConfiguration().maxPolygonalPoints);
    }

    public int getMaximumPolyhedronPoints(LocalPlayer player) {
        if (player.hasPermission("worldedit.limit.unrestricted") || getConfiguration().maxPolyhedronPoints < 0) {
            return getConfiguration().defaultMaxPolyhedronPoints;
        }

        if (getConfiguration().defaultMaxPolyhedronPoints < 0) {
            return getConfiguration().maxPolyhedronPoints;
        }

        return Math.min(getConfiguration().defaultMaxPolyhedronPoints, getConfiguration().maxPolyhedronPoints);
    }

    /**
     * Checks to see if the specified radius is within bounds.
     *
     * @param radius the radius
     * @throws MaxRadiusException
     */
    public void checkMaxRadius(double radius) throws MaxRadiusException {
        if (getConfiguration().maxRadius > 0 && radius > getConfiguration().maxRadius) {
            throw new MaxRadiusException();
        }
    }

    /**
     * Checks to see if the specified brush radius is within bounds.
     *
     * @param radius the radius
     * @throws MaxBrushRadiusException
     */
    public void checkMaxBrushRadius(double radius) throws MaxBrushRadiusException {
        if (getConfiguration().maxBrushRadius > 0 && radius > getConfiguration().maxBrushRadius) {
            throw new MaxBrushRadiusException();
        }
    }

    /**
     * Get a file relative to the defined working directory. If the specified
     * path is absolute, then the working directory is not used.
     *
     * @param path the subpath under the working directory
     * @return a working directory
     */
    public File getWorkingDirectoryFile(String path) {
        File f = new File(path);
        if (f.isAbsolute()) {
            return f;
        }

        return new File(getConfiguration().getWorkingDirectory(), path);
    }

    /**
     * Get the direction vector for a player's direction. May return
     * null if a direction could not be found.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction vector
     * @throws UnknownDirectionException thrown if the direction is not known
     */
    public Vector getDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {
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

    /**
     * Get the direction vector for a player's direction. May return
     * null if a direction could not be found.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction enum value
     * @throws UnknownDirectionException thrown if the direction is not known
     */
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
     * @param player the player
     * @param dirStr the direction string
     * @return a direction vector
     * @throws UnknownDirectionException thrown if the direction is not known
     */
    public Vector getDiagonalDirection(LocalPlayer player, String dirStr)
            throws UnknownDirectionException {

        return getPlayerDirection(player, dirStr.toLowerCase()).vector();
    }

    /**
     * Get the flip direction for a player's direction.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction vector
     * @throws UnknownDirectionException thrown if the direction is not known
     */
    public FlipDirection getFlipDirection(LocalPlayer player, String dirStr) throws UnknownDirectionException {

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
     * @param player the player
     * @param editSession the edit session
     */
    public void flushBlockBag(LocalPlayer player, EditSession editSession) {
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

                str.append(" [Amt: ").append(missingBlocks.get(id)).append("]");

                ++i;

                if (i != size) {
                    str.append(", ");
                }
            }

            player.printError(str.toString());
        }
    }

    /**
     * Get the map of commands (internal usage only).
     *
     * @return the commands
     */
    public Map<String, String> getCommands() {
        return getCommandsManager().getCommands();
    }

    /**
     * Get the commands manager (internal usage only).
     *
     * @return the commands
     */
    public CommandsManager<LocalPlayer> getCommandsManager() {
        return getPlatformManager().getCommandManager().getCommands();
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
     * @param player the player
     * @return true if the swing was handled
     */
    public boolean handleArmSwing(LocalPlayer player) {
        if (player.getItemInHand() == getConfiguration().navigationWand) {
            if (getConfiguration().navigationWandMaxDistance <= 0) {
                return false;
            }

            if (!player.hasPermission("worldedit.navigation.jumpto.tool")) {
                return false;
            }

            WorldVector pos = player.getSolidBlockTrace(getConfiguration().navigationWandMaxDistance);
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
                ((DoubleActionTraceTool) tool).actSecondary(getServer(), getConfiguration(), player, session);
                return true;
            }
        }

        return false;
    }

    /**
     * Called on right click (not on a block).
     *
     * @param player the player
     * @return true if the right click was handled
     */
    public boolean handleRightClick(LocalPlayer player) {
        if (player.getItemInHand() == getConfiguration().navigationWand) {
            if (getConfiguration().navigationWandMaxDistance <= 0) {
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
                ((TraceTool) tool).actPrimary(getServer(), getConfiguration(), player, session);
                return true;
            }
        }

        return false;
    }

    /**
     * Called on right click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @return false if you want the action to go through
     */
    public boolean handleBlockRightClick(LocalPlayer player, WorldVector clicked) {
        LocalSession session = getSession(player);

        if (player.getItemInHand() == getConfiguration().wandItem) {
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
                ((BlockTool) tool).actPrimary(getServer(), getConfiguration(), player, session, clicked);
                return true;
            }
        }

        return false;
    }

    /**
     * Called on left click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @return false if you want the action to go through
     */
    public boolean handleBlockLeftClick(LocalPlayer player, WorldVector clicked) {
        LocalSession session = getSession(player);

        if (player.getItemInHand() == getConfiguration().wandItem) {
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
                return superPickaxe.actPrimary(getServer(), getConfiguration(), player, session, clicked);
            }
        }

        Tool tool = session.getTool(player.getItemInHand());
        if (tool != null && tool instanceof DoubleActionBlockTool) {
            if (tool.canUse(player)) {
                ((DoubleActionBlockTool) tool).actSecondary(getServer(), getConfiguration(), player, session, clicked);
                return true;
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
        CommandEvent event = new CommandEvent(player, split);
        getEventBus().post(event);
        return event.isCancelled();
    }

    public String[] commandDetection(String[] split) {
        return getPlatformManager().getCommandManager().commandDetection(split);
    }

    /**
     * Executes a WorldEdit script.
     *
     * @param player the player
     * @param f the script file to execute
     * @param args arguments for the script
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
                file = WorldEdit.class.getResourceAsStream("craftscripts/" + filename);

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
                new CraftScriptContext(this, getServer(), getConfiguration(), session, player, args);

        CraftScriptEngine engine = null;

        try {
            engine = new RhinoCraftScriptEngine();
        } catch (NoClassDefFoundError e) {
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
        }

        engine.setTimeLimit(getConfiguration().scriptTimeout);

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
     * @return a configuration
     */
    public LocalConfiguration getConfiguration() {
        return getPlatformManager().getConfiguration();
    }

    /**
     * Get the server interface.
     *
     * @return the server interface
     */
    public ServerInterface getServer() {
        return getPlatformManager().getServerInterface();
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
     * @return the version of WorldEdit
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

    /**
     * @deprecated Declare your platform version with {@link Platform#getPlatformVersion()}
     */
    @Deprecated
    public static void setVersion(String version) {
    }

}
