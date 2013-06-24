// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.rebar.command.Dispatcher;
import com.sk89q.rebar.command.InvalidUsageException;
import com.sk89q.rebar.command.fluent.CommandGraph;
import com.sk89q.rebar.command.parametric.ExceptionConverter;
import com.sk89q.rebar.command.parametric.LegacyCommandsHandler;
import com.sk89q.rebar.command.parametric.ParametricBuilder;
import com.sk89q.rebar.formatting.MessageBuilder;
import com.sk89q.rebar.formatting.Style;
import com.sk89q.worldedit.CuboidClipboard.FlipDirection;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.commands.BiomeCommands;
import com.sk89q.worldedit.commands.BrushCommands;
import com.sk89q.worldedit.commands.ChunkCommands;
import com.sk89q.worldedit.commands.ClipboardCommands;
import com.sk89q.worldedit.commands.GeneralCommands;
import com.sk89q.worldedit.commands.GenerationCommands;
import com.sk89q.worldedit.commands.HistoryCommands;
import com.sk89q.worldedit.commands.NavigationCommands;
import com.sk89q.worldedit.commands.OperationCommands;
import com.sk89q.worldedit.commands.RegionCommands;
import com.sk89q.worldedit.commands.SchematicCommands;
import com.sk89q.worldedit.commands.ScriptingCommands;
import com.sk89q.worldedit.commands.SelectionCommands;
import com.sk89q.worldedit.commands.SnapshotCommands;
import com.sk89q.worldedit.commands.SnapshotUtilCommands;
import com.sk89q.worldedit.commands.SuperPickaxeCommands;
import com.sk89q.worldedit.commands.ToolCommands;
import com.sk89q.worldedit.commands.ToolUtilCommands;
import com.sk89q.worldedit.commands.UtilityCommands;
import com.sk89q.worldedit.commands.WorldEditCommands;
import com.sk89q.worldedit.factory.FilterFactory;
import com.sk89q.worldedit.factory.MaterialFactory;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.operation.EditSessionFlusher;
import com.sk89q.worldedit.operation.ImmediateExecutor;
import com.sk89q.worldedit.operation.Operation;
import com.sk89q.worldedit.operation.OperationExecutor;
import com.sk89q.worldedit.operation.OperationResponse;
import com.sk89q.worldedit.operation.PlayerIssuedOperation;
import com.sk89q.worldedit.operation.QueuedOperation;
import com.sk89q.worldedit.operation.RejectedOperationException;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.scripting.CraftScriptHost;
import com.sk89q.worldedit.tools.BlockTool;
import com.sk89q.worldedit.tools.DoubleActionBlockTool;
import com.sk89q.worldedit.tools.DoubleActionTraceTool;
import com.sk89q.worldedit.tools.Tool;
import com.sk89q.worldedit.tools.TraceTool;
import com.sk89q.worldedit.util.CommandLoggingHandler;
import com.sk89q.worldedit.util.CommandPermissionsHandler;
import com.sk89q.worldedit.util.SessionMap;
import com.sk89q.worldedit.util.WorldEditBinding;
import com.sk89q.worldedit.util.WorldEditData;
import com.sk89q.worldedit.util.WorldEditExceptionConverter;

/**
 * The main entry point for WorldEdit.
 *
 * <p>Implementations should create one instance of WorldEdit.</p>
 */
public class WorldEdit {
    
    public static final Logger logger = Logger.getLogger(
            WorldEdit.class.getCanonicalName());

    private static WorldEdit instance;
    private static String version;
    
    private final ServerInterface server;
    private final LocalConfiguration config;
    private final Dispatcher dispatcher;
    private final SessionMap sessions;
    private final OperationExecutor executor;
    private final CommandLoggingHandler commandLogger;
    private final WorldEditExceptionConverter exceptionConverter;
    private final MaterialFactory materials;
    private final FilterFactory filters;
    private final WorldEditData data;
    private final CraftScriptHost scriptingHost;
    private EditSessionFactory editSessionFactory = new EditSessionFactory();

    /**
     * Construct an instance of the class.
     * 
     * <p>This constructor will use an {@link ImmediateExecutor} as the
     * {@link OperationExecutor}.</p>
     *
     * @param server the server interface
     * @param config the configuration
     */
    public WorldEdit(ServerInterface server, LocalConfiguration config) {
        this(server, config, new ImmediateExecutor());
    }

    /**
     * Construct an instance of the class.
     *
     * @param server the server interface
     * @param config the configuration
     * @param executor an operation executor
     */
    public WorldEdit(ServerInterface server, LocalConfiguration config,
                     OperationExecutor executor) {
        WorldEdit.instance = this;
        this.server = server;
        this.config = config;
        this.executor = executor;

        materials = new MaterialFactory(this);
        filters = new FilterFactory(this, materials);
        sessions = new SessionMap(config);
        data = new WorldEditData(this);
        scriptingHost = new CraftScriptHost(this);

        ParametricBuilder builder = new ParametricBuilder();
        builder.addBinding(new WorldEditBinding(this));
        builder.attach(new CommandPermissionsHandler());
        builder.attach(exceptionConverter = new WorldEditExceptionConverter(config));
        builder.attach(new LegacyCommandsHandler());
        builder.attach(commandLogger = new CommandLoggingHandler(this, config));

        dispatcher = new CommandGraph()
                .builder(builder)
                .commands()
                    .build(new BiomeCommands(this))
                    .build(new ChunkCommands(this))
                    .build(new ClipboardCommands(this))
                    .build(new GeneralCommands(this))
                    .build(new GenerationCommands(this))
                    .build(new HistoryCommands(this))
                    .build(new NavigationCommands(this))
                    .build(new OperationCommands(this))
                    .build(new RegionCommands(this))
                    .build(new ScriptingCommands(this))
                    .build(new SelectionCommands(this))
                    .build(new SnapshotUtilCommands(this))
                    .build(new ToolUtilCommands(this))
                    .build(new ToolCommands(this))
                    .build(new UtilityCommands(this))
                    .group("worldedit", "we")
                        .describe("WorldEdit commands")
                        .build(new WorldEditCommands(this))
                        .parent()
                    .group("schematic", "schem", "/schematic", "/schem")
                        .describe("Schematic commands for saving/loading areas")
                        .build(new SchematicCommands(this))
                        .parent()
                    .group("snapshot", "snap")
                        .describe("Schematic commands for saving/loading areas")
                        .build(new SnapshotCommands(this))
                        .parent()
                    .group("brush", "br")
                        .describe("Brushing commands")
                        .build(new BrushCommands(this))
                        .parent()
                    .group("superpickaxe", "pickaxe", "sp")
                        .describe("Super-pickaxe commands")
                        .build(new SuperPickaxeCommands(this))
                        .parent()
                    .group("tool")
                        .describe("Bind functions to held items")
                        .build(new ToolCommands(this))
                        .parent()
                .graph()
                .getDispatcher();

        server.registerCommands(dispatcher);
    }

    /**
     * Get the server interface.
     *
     * @return the server interface
     */
    public ServerInterface getServer() {
        return server;
    }
    
    /**
     * Get the operation executor.
     * 
     * @return the executor
     */
    public OperationExecutor getExecutor() {
        return executor;
    }
    
    /**
     * Get the exception converter.
     * 
     * <p>This is used to convert arbitrary exceptions to {@link CommandException}s
     * when a matching exception is found.</p>
     * 
     * @return the converter
     */
    public ExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    /**
     * Get the map of sessions.
     *
     * @return the session map
     */
    public SessionMap getSessions() {
        return sessions;
    }

    /**
     * Get the material factory.
     *
     * <p>This is used to create {@link BaseBlock}s.</p>
     *
     * @return the material factory
     */
    public MaterialFactory getMaterials() {
        return materials;
    }

    /**
     * Get the filter factory.
     *
     * <p>This is used to create {@link Pattern}s and {@link Mask}s.</p>
     *
     * @return the filter factory
     */
    public FilterFactory getFilters() {
        return filters;
    }

    /**
     * Get the scripting host.
     *
     * @return the scriptign host
     */
    public CraftScriptHost getScripting() {
        return scriptingHost;
    }

    /**
     * Get the command dispatcher.
     *
     * @return the command dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Get Worldedit's configuration.
     *
     * @return the configuration
     */
    public LocalConfiguration getConfiguration() {
        return config;
    }

    /**
     * Get the command logger.
     *
     * @return the logger
     */
    public CommandLoggingHandler getCommandLogger() {
        return commandLogger;
    }

    /**
     * Get the object that represents the writable directory for WorldEdit data.
     *
     * @return the data
     */
    public WorldEditData getApplicationData() {
        return data;
    }

    /**
     * Get the edit session factory
     *
     * @return the edit session factory
     */
    public EditSessionFactory getEditSessionFactory() {
        return this.editSessionFactory;
    }

    /**
     * Set the edit session factory
     *
     * @param factory the edit session factory
     */
    public void setEditSessionFactory(EditSessionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("New EditSessionFactory may not be null");
        }
        logger.info("Accepted EditSessionFactory of type " +
                factory.getClass().getName() + " from " +
                factory.getClass().getPackage().getName());
        this.editSessionFactory = factory;
    }

    /**
     * Add an {@link Operation} to the current {@link OperationExecutor} received
     * from {@link #getExecutor()} and add callbacks to the returned {@link Future}
     * to inform the given player about the return status of the operation.
     *
     * @param player the player
     * @param operation the operation
     * @param editSession the edit session
     * @param label a label describing the operation
     * @throws RejectedOperationException if the operation was rejected
     * @see OperationExecutor#offer(Operation)
     */
    public void execute(final LocalPlayer player, Operation operation,
            EditSession editSession, String label) throws RejectedOperationException {
        QueuedOperation queued = getExecutor().offer(operation);
        OperationResponse response = new OperationResponse(this, player, queued);
        response.schedule();
        PlayerIssuedOperation metadata = new PlayerIssuedOperation(label, player);
        queued.setMetadata(metadata);
        ListenableFuture<Operation> future = queued.getFuture();
        Futures.addCallback(future, new EditSessionFlusher(this, editSession, player));
        Futures.addCallback(future, response);
    }

    /**
     * Return a new message builder for WorldEdit messages.
     * 
     * @return a message builder
     */
    public MessageBuilder createMessage() {
        return new MessageBuilder(Style.PURPLE);
    }

    /**
     * Handle a disconnect of a player.
     *
     * @param player the player
     */
    @Deprecated
    public void handleDisconnect(LocalPlayer player) {
        getSessions().remove(player);
    }

    /**
     * Called on arm swing of a player.
     *
     * @param player the player
     * @return if the event has been handled
     */
    public boolean handleArmSwing(LocalPlayer player) {
        if (player.getItemInHand() == config.navigationWand) {
            if (config.navigationWandMaxDistance <= 0) {
                return false;
            }

            if (!player.hasPermission("worldedit.navigation.jumpto.tool") ){
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
     * @param player the player
     * @return if the event was handled
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
     * Called on right click of a block by a player.
     *
     * @param player the player
     * @param clicked the position of the block clicked
     * @return if the event was handled
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
     * Called on left click of a block by a player.
     *
     * @param player the player
     * @param clicked the position of the block clicked
     * @return if the event was handled
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

    /**
     * Execute a command.
     * 
     * @param player the local player
     * @param split the list of arguments
     * @return whether the command was processed
     */
    public boolean handleCommand(LocalPlayer player, String[] split) {
        split = commandDetection(split);

        // No command found!
        if (!dispatcher.contains(split[0])) {
            return false;
        }
        
        CommandLocals locals = new CommandLocals();
        locals.put(LocalPlayer.class, player);

        long start = System.currentTimeMillis();

        try {
            dispatcher.call(split, locals);
        } catch (CommandPermissionsException e) {
            player.printError("You don't have permission to do this.");
        } catch (InvalidUsageException e) {
            player.printError(e.getMessage() + "\nUsage: " + e.getUsage("/"));
        } catch (WrappedCommandException e) {
            Throwable t = e.getCause();
            player.printError("Please report this error: [See console]");
            player.printRaw(t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace();
        } catch (CommandException e) {
            player.printError(e.getMessage());
        } finally {
            EditSession editSession = locals.get(EditSession.class);
            
            if (editSession != null) {
                LocalSession session = getSession(player);
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
        if (dispatcher.contains(searchCmd)) {
        } else if (config.noDoubleSlash && dispatcher.contains("/" + searchCmd)) {
            split[0] = "/" + split[0];
        } else if (split[0].length() >= 2 && split[0].charAt(0) == '/'
                && dispatcher.contains(searchCmd.substring(1))) {
            split[0] = split[0].substring(1);
        }
        return split;
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
     * Gets the LocalSession for a player name if it exists.
     *
     * @param player the player
     * @return the session for the player, if it exists, otherwise null
     * @deprecated see #getSessions()
     */
    @Deprecated
    public LocalSession getSession(String player) {
        return getSessions().getIfExists(player);
    }

    /**
     * Gets the WorldEdit session for a player.
     * 
     * <p>If the session does not yet exist, create a new one.</p>
     *
     * @param player the player
     * @return the session
     * @deprecated see #getSessions()
     */
    @Deprecated
    public LocalSession getSession(LocalPlayer player) {
        return getSessions().get(player);
    }

    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @return a block
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String input, boolean ignoreBlacklist)
            throws UnknownItemException, DisallowedItemException {
        return getMaterials().matchBlock(player, input, ignoreBlacklist);
    }

    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @param allowDataWildcard true to allow wildcard data match
     * @return a block
     * @throws UnknownItemException if an item cannot be matched
     * @throws DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String input,
                              boolean ignoreBlacklist, boolean allowDataWildcard)
            throws UnknownItemException, DisallowedItemException {
        return getMaterials().matchBlock(player, input, ignoreBlacklist, allowDataWildcard);
    }

    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a block
     * @throws UnknownItemException if an item cannot be matched
     * @throws DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public BaseBlock getBlock(LocalPlayer player, String input)
            throws UnknownItemException, DisallowedItemException {
        return getMaterials().matchBlock(player, input);
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @param allowDataWildcard true to allow wildcard data match
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public Set<BaseBlock> getBlocks(LocalPlayer player, String input,
                                    boolean ignoreBlacklist, boolean allowDataWildcard)
            throws DisallowedItemException, UnknownItemException {
        return getMaterials().matchBlocks(player, input, ignoreBlacklist, allowDataWildcard);
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public Set<BaseBlock> getBlocks(LocalPlayer player, String input, boolean ignoreBlacklist)
            throws DisallowedItemException, UnknownItemException {
        return getMaterials().matchBlocks(player, input, ignoreBlacklist);
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public Set<BaseBlock> getBlocks(LocalPlayer player, String input)
            throws DisallowedItemException, UnknownItemException {
        return getMaterials().matchBlocks(player, input);
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param player the player
     * @param input the user input
     * @param ignoreBlacklist true to ignore blacklists
     * @return the list of block IDs
     * @throws UnknownItemException
     * @throws DisallowedItemException
     * @deprecated see #getMaterials()
     */
    @Deprecated
    public Set<Integer> getBlockIDs(LocalPlayer player,
                                    String input, boolean ignoreBlacklist)
            throws UnknownItemException, DisallowedItemException {
        return getMaterials().matchBlockIds(player, input, ignoreBlacklist);
    }

    /**
     * Match a {@link com.sk89q.worldedit.patterns.Pattern} from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a pattern
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     * @deprecated see #getFilters()
     */
    @Deprecated
    public Pattern getBlockPattern(LocalPlayer player, String input)
            throws UnknownItemException, DisallowedItemException {
        return getFilters().matchPattern(player, input);
    }

    /**
     * Match a {@link Mask} from user input.
     *
     * @param player the player
     * @param session the session
     * @param input the user input
     * @return a mask
     * @throws WorldEditException on an error
     * @deprecated see #getFilters()
     */
    @Deprecated
    public Mask getBlockMask(LocalPlayer player, LocalSession session,
                             String input) throws WorldEditException {
        return getFilters().matchMask(player, input);
    }

    /**
     * Parse a string for a direction, using the player's information if needed.
     * 
     * <p>Only orthogonal directions are accepted.</p>
     *
     * @param player the player
     * @param input the string to parse
     * @return a unit vector pointing in the direction
     * @throws UnknownDirectionException
     * @deprecated see LocalPlayer#matchDirection(String)
     */
    @Deprecated
    public Vector getDirection(LocalPlayer player, String input)
            throws UnknownDirectionException {
        return player.matchDirection(input, false);
    }

    /**
     * Parse a string for a direction, using the player's information if needed.
     * 
     * <p>The returned direction may be diagonal.</p>
     *
     * @param player the player
     * @param input the string to parse
     * @return a unit vector pointing in the direction
     * @throws UnknownDirectionException
     * @deprecated see LocalPlayer#matchDirection(String)
     */
    @Deprecated
    public Vector getDiagonalDirection(LocalPlayer player, String input)
            throws UnknownDirectionException {
        return player.matchDirection(input, true);
    }

    /**
     * Get the flip direction for a player's direction.
     *
     * @param player the player
     * @param input the input
     * @return a flip direction
     * @throws UnknownDirectionException
     * @deprecated see LocalPlayer#matchFlipDirection(String)
     */
    @Deprecated
    public FlipDirection getFlipDirection(LocalPlayer player, String input)
            throws UnknownDirectionException {
        return player.matchFlipDirection(input);
    }

    static {
        getVersion();
    }

    /**
     * Gets the current instance of this class.
     *
     * <p>An instance is available once a plugin/host has initialized WorldEdit.</p>
     *
     * @return an instance
     */
    public static WorldEdit getInstance() {
        return instance;
    }

    /**
     * Get the version.
     *
     * @return the WorldEdit version
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
     * Override the version detected by WorldEdit.
     *
     * @param version the version to override width
     */
    public static void setVersion(String version) {
        WorldEdit.version = version;
    }
}
