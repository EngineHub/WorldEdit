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

import static com.sk89q.worldedit.event.platform.Interaction.HIT;
import static com.sk89q.worldedit.event.platform.Interaction.OPEN;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import com.sk89q.worldedit.event.platform.InputType;
import com.sk89q.worldedit.event.platform.PlayerInputEvent;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.factory.ItemFactory;
import com.sk89q.worldedit.extension.factory.MaskFactory;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.scripting.CraftScriptContext;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.RhinoCraftScriptEngine;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.io.file.FilenameResolutionException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;
import com.sk89q.worldedit.util.logging.WorldEditPrefixHandler;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.BundledItemData;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;

/**
 * The entry point and container for a working implementation of WorldEdit.
 *
 * <p>An instance handles event handling; block, mask, pattern, etc. registration;
 * the management of sessions; the creation of {@link EditSession}s; and more.
 * In order to use WorldEdit, at least one {@link Platform} must be registered
 * with WorldEdit using {@link PlatformManager#register(Platform)} on the
 * manager retrieved using {@link WorldEdit#getPlatformManager()}.</p>
 *
 * <p>An instance of WorldEdit can be retrieved using the static
 * method {@link WorldEdit#getInstance()}, which is shared among all
 * platforms within the same classloader hierarchy.</p>
 */
public class WorldEdit {

    public static final Logger logger = Logger.getLogger(WorldEdit.class.getCanonicalName());

    private final static WorldEdit instance = new WorldEdit();
    private static String version;

    private final EventBus eventBus = new EventBus();
    private final PlatformManager platformManager = new PlatformManager(this);
    private final EditSessionFactory editSessionFactory = new EditSessionFactory.EditSessionFactoryImpl(eventBus);
    private final SessionManager sessions = new SessionManager(this);

    private final BlockFactory blockFactory = new BlockFactory(this);
    private final ItemFactory itemFactory = new ItemFactory(this);
    private final MaskFactory maskFactory = new MaskFactory(this);
    private final PatternFactory patternFactory = new PatternFactory(this);

    static {
        WorldEditPrefixHandler.register("com.sk89q.worldedit");
        getVersion();
    }

    private WorldEdit() {
    }

    /**
     * Gets the current instance of this class.
     *
     * <p>An instance will always be available, but no platform may yet be
     * registered with WorldEdit, meaning that a number of operations
     * may fail. However, event handlers can be registered.</p>
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
     *
     * <p>Event handlers can be registered on the event bus.</p>
     *
     * @return the event bus
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Get the block factory from which new {@link BaseBlock}s can be
     * constructed.
     *
     * @return the block factory
     */
    public BlockFactory getBlockFactory() {
        return blockFactory;
    }

    /**
     * Get the item factory from which new {@link BaseItem}s can be
     * constructed.
     *
     * @return the item factory
     */
    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    /**
     * Get the mask factory from which new {@link com.sk89q.worldedit.function.mask.Mask}s
     * can be constructed.
     *
     * @return the mask factory
     */
    public MaskFactory getMaskFactory() {
        return maskFactory;
    }

    /**
     * Get the pattern factory from which new {@link com.sk89q.worldedit.function.pattern.Pattern}s
     * can be constructed.
     *
     * @return the pattern factory
     */
    public PatternFactory getPatternFactory() {
        return patternFactory;
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
    public File getSafeSaveFile(Player player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
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
    public File getSafeOpenFile(Player player, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
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
    private File getSafeFile(Player player, File dir, String filename, String defaultExt, String[] extensions, boolean isSave) throws FilenameException {
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
            List<String> exts = extensions == null ? ImmutableList.of(defaultExt) : Lists.asList(defaultExt, extensions);
            return getSafeFileWithExtensions(dir, filename,  exts, isSave);
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

    private File getSafeFileWithExtensions(File dir, String filename, List<String> exts, boolean isSave) throws InvalidFilenameException {
        if (isSave) {
            // First is default, only use that.
            if (exts.size() != 1) {
                exts = exts.subList(0, 1);
            }
        }
        File result = null;
        for (Iterator<String> iter = exts.iterator(); iter.hasNext() && (result == null || !result.exists());) {
            result = getSafeFileWithExtension(dir, filename, iter.next());
        }
        if (result == null) {
            throw new InvalidFilenameException(filename, "Invalid characters or extension missing");
        }
        return result;
    }

    private File getSafeFileWithExtension(File dir, String filename, String extension) {
        if (extension != null && filename.lastIndexOf('.') == -1) {
            filename += "." + extension;
        }

        if (!checkFilename(filename)) {
            return null;
        }

        return new File(dir, filename);
    }

    private boolean checkFilename(String filename) {
        return filename.matches("^[A-Za-z0-9_\\- \\./\\\\'\\$@~!%\\^\\*\\(\\)\\[\\]\\+\\{\\},\\?]+\\.[A-Za-z0-9]+$");
    }

    /**
     * Load the bundled mappings.
     */
    public void loadMappings() {
        BundledBlockData.getInstance(); // Load block registry
        BundledItemData.getInstance(); // Load item registry
        LegacyMapper.getInstance(); // Load item registry
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
    public Vector getDirection(Player player, String dirStr) throws UnknownDirectionException {
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
    private PlayerDirection getPlayerDirection(Player player, String dirStr) throws UnknownDirectionException {
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
     * Flush a block bag's changes to a player.
     *
     * @param actor the actor
     * @param editSession the edit session
     */
    public void flushBlockBag(Actor actor, EditSession editSession) {
        BlockBag blockBag = editSession.getBlockBag();

        if (blockBag != null) {
            blockBag.flushChanges();
        }

        Map<BlockType, Integer> missingBlocks = editSession.popMissingBlocks();

        if (!missingBlocks.isEmpty()) {
            StringBuilder str = new StringBuilder();
            str.append("Missing these blocks: ");
            int size = missingBlocks.size();
            int i = 0;

            for (BlockType id : missingBlocks.keySet()) {
                str.append(id.getName());

                str.append(" [Amt: ").append(missingBlocks.get(id)).append("]");

                ++i;

                if (i != size) {
                    str.append(", ");
                }
            }

            actor.printError(str.toString());
        }
    }

    /**
     * Called on arm swing.
     *
     * @param player the player
     * @return true if the swing was handled
     */
    public boolean handleArmSwing(Player player) {
        PlayerInputEvent event = new PlayerInputEvent(player, InputType.PRIMARY);
        getEventBus().post(event);
        return event.isCancelled();
    }

    /**
     * Called on right click (not on a block).
     *
     * @param player the player
     * @return true if the right click was handled
     */
    public boolean handleRightClick(Player player) {
        PlayerInputEvent event = new PlayerInputEvent(player, InputType.SECONDARY);
        getEventBus().post(event);
        return event.isCancelled();
    }

    /**
     * Called on right click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @return false if you want the action to go through
     */
    public boolean handleBlockRightClick(Player player, Location clicked) {
        BlockInteractEvent event = new BlockInteractEvent(player, clicked, OPEN);
        getEventBus().post(event);
        return event.isCancelled();
    }

    /**
     * Called on left click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @return false if you want the action to go through
     */
    public boolean handleBlockLeftClick(Player player, Location clicked) {
        BlockInteractEvent event = new BlockInteractEvent(player, clicked, HIT);
        getEventBus().post(event);
        return event.isCancelled();
    }

    /**
     * Executes a WorldEdit script.
     *
     * @param player the player
     * @param f the script file to execute
     * @param args arguments for the script
     * @throws WorldEditException
     */
    public void runScript(Player player, File f, String[] args) throws WorldEditException {
        Request.reset();

        String filename = f.getPath();
        int index = filename.lastIndexOf(".");
        String ext = filename.substring(index + 1);

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

        LocalSession session = getSessionManager().get(player);
        CraftScriptContext scriptContext = new CraftScriptContext(this, getPlatformManager().queryCapability(Capability.USER_COMMANDS),
                getConfiguration(), session, player, args);

        CraftScriptEngine engine;

        try {
            engine = new RhinoCraftScriptEngine();
        } catch (NoClassDefFoundError e) {
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
        }

        engine.setTimeLimit(getConfiguration().scriptTimeout);

        Map<String, Object> vars = new HashMap<>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);

        try {
            engine.evaluate(script, filename, vars);
        } catch (ScriptException e) {
            player.printError("Failed to execute:");
            player.printRaw(e.getMessage());
            logger.log(Level.WARNING, "Failed to execute script", e);
        } catch (NumberFormatException | WorldEditException e) {
            throw e;
        } catch (Throwable e) {
            player.printError("Failed to execute (see console):");
            player.printRaw(e.getClass().getCanonicalName());
            logger.log(Level.WARNING, "Failed to execute script", e);
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
     * Get a factory for {@link EditSession}s.
     */
    public EditSessionFactory getEditSessionFactory() {
        return editSessionFactory;
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

}
