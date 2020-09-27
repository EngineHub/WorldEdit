/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.SchematicsEventListener;
import com.sk89q.worldedit.internal.expression.invoke.ReturnException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.scripting.CraftScriptContext;
import com.sk89q.worldedit.scripting.CraftScriptEngine;
import com.sk89q.worldedit.scripting.RhinoCraftScriptEngine;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.collection.SetWithDefault;
import com.sk89q.worldedit.util.concurrency.EvenMoreExecutors;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.io.file.FileType;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.io.file.InvalidFilenameException;
import com.sk89q.worldedit.util.io.file.PathRequestType;
import com.sk89q.worldedit.util.io.file.SafeFiles;
import com.sk89q.worldedit.util.task.SimpleSupervisor;
import com.sk89q.worldedit.util.task.Supervisor;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.BundledItemData;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import static com.sk89q.worldedit.event.platform.Interaction.HIT;
import static com.sk89q.worldedit.event.platform.Interaction.OPEN;

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
public final class WorldEdit {

    public static final Logger logger = LoggerFactory.getLogger(WorldEdit.class);

    private static final WorldEdit instance = new WorldEdit();
    private static String version;

    private final EventBus eventBus = new EventBus();
    private final PlatformManager platformManager = new PlatformManager(this);
    @Deprecated
    private final EditSessionFactory editSessionFactory = new EditSessionFactory.EditSessionFactoryImpl();
    private final SessionManager sessions = new SessionManager(this);
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(
            EvenMoreExecutors.newBoundedCachedThreadPool(0, 1, 20, "WorldEdit Task Executor - %s"));
    private final Supervisor supervisor = new SimpleSupervisor();
    private final LazyReference<TranslationManager> translationManager =
            LazyReference.from(() -> new TranslationManager(
                    WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader()
            ));

    private final BlockFactory blockFactory = new BlockFactory(this);
    private final ItemFactory itemFactory = new ItemFactory(this);
    private final MaskFactory maskFactory = new MaskFactory(this);
    private final PatternFactory patternFactory = new PatternFactory(this);

    static {
        getVersion();
    }

    private WorldEdit() {
        eventBus.register(new SchematicsEventListener());
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
     * Get the supervisor. Internal, not for API use.
     *
     * @return the supervisor
     */
    public Supervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Get the executor service. Internal, not for API use.
     *
     * @return the executor service
     */
    public ListeningExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Get the block factory from which new {@link BlockStateHolder}s can be
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
     * Get the mask factory from which new {@link Mask}s
     * can be constructed.
     *
     * @return the mask factory
     */
    public MaskFactory getMaskFactory() {
        return maskFactory;
    }

    /**
     * Get the pattern factory from which new {@link Pattern}s
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
     * Return the translation manager.
     *
     * @return the translation manager
     */
    public TranslationManager getTranslationManager() {
        return translationManager.getValue();
    }

    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a {@code java.io.File} object will be returned.
     *
     * @param actor the actor
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
     * @deprecated use {@link #resolveSafePath(Actor, Path, String, SetWithDefault, PathRequestType)}
     *      instead with {@link PathRequestType#SAVE}
     */
    @Deprecated
    public File getSafeSaveFile(Actor actor, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
        return legacyAdapter(actor, dir, filename, defaultExt, extensions, PathRequestType.SAVE);
    }

    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a {@code java.io.File} object will be returned.
     *
     * @param actor the actor
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
     * @deprecated use {@link #resolveSafePath(Actor, Path, String, SetWithDefault, PathRequestType)}
     *      instead with {@link PathRequestType#LOAD}
     */
    @Deprecated
    public File getSafeOpenFile(Actor actor, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
        return legacyAdapter(actor, dir, filename, defaultExt, extensions, PathRequestType.LOAD);
    }

    /**
     * Gets the path to a file. This method will check to see if the filename
     * has valid characters and has an extension. It also prevents directory
     * traversal exploits by checking the root directory and the file directory.
     * On success, a {@link Path} object will be returned.
     *
     * @param actor the actor
     * @param dir sub-directory to look in
     * @param filename filename (user-submitted)
     * @param defaultExt append an extension if missing one, null to not use
     * @param extensions list of extensions, null for any
     * @return a file
     * @throws FilenameException thrown if the filename is invalid
     * @deprecated use {@link #resolveSafePath(Actor, Path, String, SetWithDefault, PathRequestType)}
     *      instead with {@link PathRequestType#LOAD}
     */
    @Deprecated
    public File get(Actor actor, File dir, String filename, String defaultExt, String... extensions) throws FilenameException {
        return legacyAdapter(actor, dir, filename, defaultExt, extensions, PathRequestType.LOAD);
    }

    @Deprecated
    private File legacyAdapter(Actor actor, File dir, String filename, String defaultExt,
                               String[] extensions, PathRequestType type) throws FilenameException {
        SetWithDefault<FileType> fileTypes = FileType.adaptLegacyExtensions(defaultExt, extensions);
        try {
            return resolveSafePath(
                actor, dir.toPath(), filename, fileTypes, type
            ).join().toFile();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            Throwables.propagateIfPossible(cause, FilenameException.class);
            throw e;
        }
    }

    /**
     * Resolve a path to a file.
     *
     * <p>Details on how this is <em>safe</em> can be found at
     * {@link SafeFiles#resolveSafePathWithFileType(Path, String, SetWithDefault)}.</p>
     *
     * @param actor the actor
     * @param dir directory to resolve against
     * @param path user-submitted path
     * @param fileTypes file types to accept, empty for any
     * @return a future that completes with the path if successful, otherwise an error
     */
    public CompletableFuture<Path> resolveSafePath(@Nullable Actor actor,
                                                   Path dir,
                                                   String path,
                                                   SetWithDefault<FileType> fileTypes,
                                                   PathRequestType type) {
        CompletableFuture<Path> ftr;
        if (path.equals("#") && actor != null) {
            ftr = actor.requestPath(type, fileTypes);
        } else {
            ftr = new CompletableFuture<>();
            try {
                ftr.complete(SafeFiles.resolveSafePathWithFileType(
                    dir, path, fileTypes
                ));
            } catch (InvalidFilenameException e) {
                ftr.completeExceptionally(e);
            }
        }

        return ftr;
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
     * @throws MaxRadiusException if the radius is bigger than the configured radius
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
     * @throws MaxBrushRadiusException if the radius is bigger than the configured radius
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
     * @deprecated Use {@link WorldEdit#getWorkingDirectoryPath(String)} instead
     */
    @Deprecated
    public File getWorkingDirectoryFile(String path) {
        return getWorkingDirectoryPath(path).toFile();
    }

    /**
     * Get a file relative to the defined working directory. If the specified
     * path is absolute, then the working directory is not used.
     *
     * @param path the subpath under the working directory
     * @return a working directory
     */
    public Path getWorkingDirectoryPath(String path) {
        Path p = Paths.get(path);
        if (p.isAbsolute()) {
            return p;
        }

        return getConfiguration().getWorkingDirectoryPath().resolve(path);
    }

    /**
     * Get the direction vector for a player's direction.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction vector
     * @throws UnknownDirectionException thrown if the direction is not known, or a relative direction is used with null player
     */
    public BlockVector3 getDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
        dirStr = dirStr.toLowerCase(Locale.ROOT);

        final Direction dir = getPlayerDirection(player, dirStr);

        if (dir.isUpright() || dir.isCardinal()) {
            return dir.toBlockVector();
        } else {
            throw new UnknownDirectionException(dir.name());
        }
    }

    /**
     * Get the direction vector for a player's direction.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction vector
     * @throws UnknownDirectionException thrown if the direction is not known, or a relative direction is used with null player
     */
    public BlockVector3 getDiagonalDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
        dirStr = dirStr.toLowerCase(Locale.ROOT);

        final Direction dir = getPlayerDirection(player, dirStr);

        if (dir.isCardinal() || dir.isOrdinal() || dir.isUpright()) {
            return dir.toBlockVector();
        }

        throw new UnknownDirectionException(dir.name());
    }

    private static final Map<String, Direction> NAME_TO_DIRECTION_MAP;

    static {
        SetMultimap<Direction, String> directionNames = HashMultimap.create();
        for (Direction direction : Direction.valuesOf(
            Direction.Flag.CARDINAL | Direction.Flag.UPRIGHT
        )) {
            String name = direction.name().toLowerCase(Locale.ROOT);
            for (int i = 1; i <= name.length(); i++) {
                directionNames.put(direction, name.substring(0, i));
            }
        }
        ImmutableMap.Builder<String, Direction> nameToDirectionMap = ImmutableMap.builder();
        for (Direction direction : directionNames.keySet()) {
            directionNames.get(direction).forEach(name ->
                nameToDirectionMap.put(name, direction)
            );
        }
        for (Direction direction : ImmutableList.of(Direction.NORTH, Direction.SOUTH)) {
            for (Direction diagonal : ImmutableList.of(Direction.WEST, Direction.EAST)) {
                for (String dirName : directionNames.get(direction)) {
                    for (String diagName : directionNames.get(diagonal)) {
                        nameToDirectionMap.put(dirName + diagName, Direction.valueOf(
                            direction.name() + diagonal.name()
                        ));
                    }
                }
            }
        }
        NAME_TO_DIRECTION_MAP = nameToDirectionMap.build();
    }

    /**
     * Get the direction vector for a player's direction.
     *
     * @param player the player
     * @param dirStr the direction string
     * @return a direction enum value
     * @throws UnknownDirectionException thrown if the direction is not known, or a relative direction is used with null player
     */
    private Direction getPlayerDirection(@Nullable Player player, String dirStr) throws UnknownDirectionException {
        Direction byName = NAME_TO_DIRECTION_MAP.get(dirStr);
        if (byName != null) {
            return byName;
        }
        switch (dirStr) {
            case "m":
            case "me":
            case "f":
            case "forward":
                return getDirectionRelative(player, 0);

            case "b":
            case "back":
                return getDirectionRelative(player, 180);

            case "l":
            case "left":
                return getDirectionRelative(player, -90);

            case "r":
            case "right":
                return getDirectionRelative(player, 90);

            default:
                throw new UnknownDirectionException(dirStr);
        }
    }

    private Direction getDirectionRelative(Player player, int yawOffset) throws UnknownDirectionException {
        if (player != null) {
            return player.getCardinalDirection(yawOffset);
        }
        throw new UnknownDirectionException("Only a player can use relative directions");
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
            TextComponent.Builder str = TextComponent.builder();
            str.append("Missing these blocks: ");
            int size = missingBlocks.size();
            int i = 0;

            for (Map.Entry<BlockType, Integer> blockTypeIntegerEntry : missingBlocks.entrySet()) {
                str.append((blockTypeIntegerEntry.getKey()).getRichName());

                str.append(" [Amt: ")
                    .append(String.valueOf(blockTypeIntegerEntry.getValue()))
                    .append("]");

                ++i;

                if (i != size) {
                    str.append(", ");
                }
            }

            actor.printError(str.build());
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
    @Deprecated
    public boolean handleBlockRightClick(Player player, Location clicked) {
        return handleBlockRightClick(player, clicked, null);
    }

    /**
     * Called on right click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @param face The clicked face
     * @return false if you want the action to go through
     */
    public boolean handleBlockRightClick(Player player, Location clicked, @Nullable Direction face) {
        BlockInteractEvent event = new BlockInteractEvent(player, clicked, face, OPEN);
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
    @Deprecated
    public boolean handleBlockLeftClick(Player player, Location clicked) {
        return handleBlockLeftClick(player, clicked, null);
    }

    /**
     * Called on left click.
     *
     * @param player the player
     * @param clicked the clicked block
     * @param face The clicked face
     * @return false if you want the action to go through
     */
    public boolean handleBlockLeftClick(Player player, Location clicked, @Nullable Direction face) {
        BlockInteractEvent event = new BlockInteractEvent(player, clicked, face, HIT);
        getEventBus().post(event);
        return event.isCancelled();
    }

    /**
     * Executes a WorldEdit script.
     *
     * @param player the player
     * @param f the script file to execute
     * @param args arguments for the script
     * @throws WorldEditException if something goes wrong
     * @deprecated Use {@link #runScript(Player, Path, List)} instead
     */
    @Deprecated
    public void runScript(Player player, File f, String[] args) throws WorldEditException {
        runScript(player, f.toPath(), Arrays.asList(args));
    }

    /**
     * Executes a WorldEdit script.
     *
     * @param player the player
     * @param path the script file to execute
     * @param args arguments for the script
     * @throws WorldEditException if something goes wrong
     */
    public void runScript(Player player, Path path, List<String> args) throws WorldEditException {
        String filename = path.toString();
        int index = filename.lastIndexOf('.');
        String ext = filename.substring(index + 1);

        if (!ext.equalsIgnoreCase("js")) {
            player.printError(TranslatableComponent.of("worldedit.script.unsupported"));
            return;
        }

        String script;

        try {
            if (Files.exists(path)) {
                script = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Files.readAllBytes(path)))
                    .toString();
            } else {
                try (InputStream stream = WorldEdit.class.getResourceAsStream("craftscripts/" + filename)) {
                    if (stream == null) {
                        player.printError(TranslatableComponent.of("worldedit.script.file-not-found", TextComponent.of(filename)));
                        return;
                    }
                    script = CharStreams.toString(new InputStreamReader(stream));
                }
            }
        } catch (IOException e) {
            player.printError(TranslatableComponent.of("worldedit.script.read-error", TextComponent.of(e.getMessage())));
            return;
        }

        LocalSession session = getSessionManager().get(player);
        CraftScriptContext scriptContext = new CraftScriptContext(this, getPlatformManager().queryCapability(Capability.USER_COMMANDS),
                getConfiguration(), session, player, args);

        CraftScriptEngine engine;

        try {
            engine = new RhinoCraftScriptEngine();
        } catch (NoClassDefFoundError ignored) {
            player.printError(TranslatableComponent.of("worldedit.script.no-script-engine"));
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
            // non-exceptional return check
            if (!(Throwables.getRootCause(e) instanceof ReturnException)) {
                player.printError(TranslatableComponent.of("worldedit.script.failed", TextComponent.of(e.getMessage(), TextColor.WHITE)));
                logger.warn("Failed to execute script", e);
            }
        } catch (NumberFormatException | WorldEditException e) {
            throw e;
        } catch (Throwable e) {
            player.printError(TranslatableComponent.of("worldedit.script.failed-console", TextComponent.of(e.getClass().getCanonicalName(),
                    TextColor.WHITE)));
            logger.warn("Failed to execute script", e);
        } finally {
            for (EditSession editSession : scriptContext.getEditSessions()) {
                editSession.close();
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
     *
     * @deprecated Use {@link #newEditSessionBuilder()} instead. See {@link EditSessionFactory} for details.
     */
    @Deprecated
    public EditSessionFactory getEditSessionFactory() {
        return editSessionFactory;
    }

    /**
     * Create a builder for {@link EditSession}s.
     */
    public EditSessionBuilder newEditSessionBuilder() {
        return new EditSessionBuilder(eventBus);
    }

    /**
     * Shorthand for {@code newEditSessionBuilder().world(world).build()}.
     *
     * @param world the world
     * @return the new {@link EditSession}
     */
    public EditSession newEditSession(@Nullable World world) {
        return newEditSessionBuilder().world(world).build();
    }

    /**
     * Shorthand for {@code newEditSessionBuilder().locatableActor(locatableActor).build()}.
     *
     * @param locatableActor the actor
     * @return the new {@link EditSession}
     */
    public <A extends Actor & Locatable> EditSession newEditSession(A locatableActor) {
        return newEditSessionBuilder().locatableActor(locatableActor).build();
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

        WorldEditManifest manifest = WorldEditManifest.load();

        return version = manifest.getWorldEditVersion();
    }

}
