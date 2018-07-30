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

package com.sk89q.worldedit.extension.platform;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.command.composition.LegacyCommandAdapter.adapt;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.BiomeCommands;
import com.sk89q.worldedit.command.BrushCommands;
import com.sk89q.worldedit.command.ChunkCommands;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.GeneralCommands;
import com.sk89q.worldedit.command.GenerationCommands;
import com.sk89q.worldedit.command.HistoryCommands;
import com.sk89q.worldedit.command.NavigationCommands;
import com.sk89q.worldedit.command.RegionCommands;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.command.ScriptingCommands;
import com.sk89q.worldedit.command.SelectionCommands;
import com.sk89q.worldedit.command.SnapshotCommands;
import com.sk89q.worldedit.command.SnapshotUtilCommands;
import com.sk89q.worldedit.command.SuperPickaxeCommands;
import com.sk89q.worldedit.command.ToolCommands;
import com.sk89q.worldedit.command.ToolUtilCommands;
import com.sk89q.worldedit.command.UtilityCommands;
import com.sk89q.worldedit.command.WorldEditCommands;
import com.sk89q.worldedit.command.argument.ReplaceParser;
import com.sk89q.worldedit.command.argument.TreeGeneratorParser;
import com.sk89q.worldedit.command.composition.ApplyCommand;
import com.sk89q.worldedit.command.composition.DeformCommand;
import com.sk89q.worldedit.command.composition.PaintCommand;
import com.sk89q.worldedit.command.composition.SelectionCommand;
import com.sk89q.worldedit.command.composition.ShapedBrushCommand;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.function.factory.Deform;
import com.sk89q.worldedit.function.factory.Deform.Mode;
import com.sk89q.worldedit.internal.command.ActorAuthorizer;
import com.sk89q.worldedit.internal.command.CommandLoggingHandler;
import com.sk89q.worldedit.internal.command.UserCommandCompleter;
import com.sk89q.worldedit.internal.command.WorldEditBinding;
import com.sk89q.worldedit.internal.command.WorldEditExceptionConverter;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.InvalidUsageException;
import com.sk89q.worldedit.util.command.composition.ProvidedValue;
import com.sk89q.worldedit.util.command.fluent.CommandGraph;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverter;
import com.sk89q.worldedit.util.command.parametric.LegacyCommandsHandler;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.component.CommandUsageBox;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;
import com.sk89q.worldedit.util.logging.LogFormat;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Handles the registration and invocation of commands.
 *
 * <p>This class is primarily for internal usage.</p>
 */
public final class CommandManager {

    public static final Pattern COMMAND_CLEAN_PATTERN = Pattern.compile("^[/]+");
    private static final Logger log = Logger.getLogger(CommandManager.class.getCanonicalName());
    private static final Logger commandLog = Logger.getLogger(CommandManager.class.getCanonicalName() + ".CommandLog");
    private static final Pattern numberFormatExceptionPattern = Pattern.compile("^For input string: \"(.*)\"$");

    private final WorldEdit worldEdit;
    private final PlatformManager platformManager;
    private final Dispatcher dispatcher;
    private final DynamicStreamHandler dynamicHandler = new DynamicStreamHandler();
    private final ExceptionConverter exceptionConverter;

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    CommandManager(final WorldEdit worldEdit, PlatformManager platformManager) {
        checkNotNull(worldEdit);
        checkNotNull(platformManager);
        this.worldEdit = worldEdit;
        this.platformManager = platformManager;
        this.exceptionConverter = new WorldEditExceptionConverter(worldEdit);

        // Register this instance for command events
        worldEdit.getEventBus().register(this);

        // Setup the logger
        commandLog.addHandler(dynamicHandler);

        // Set up the commands manager
        ParametricBuilder builder = new ParametricBuilder();
        builder.setAuthorizer(new ActorAuthorizer());
        builder.setDefaultCompleter(new UserCommandCompleter(platformManager));
        builder.addBinding(new WorldEditBinding(worldEdit));
        builder.addInvokeListener(new LegacyCommandsHandler());
        builder.addInvokeListener(new CommandLoggingHandler(worldEdit, commandLog));

        dispatcher = new CommandGraph()
                .builder(builder)
                    .commands()
                        .registerMethods(new BiomeCommands(worldEdit))
                        .registerMethods(new ChunkCommands(worldEdit))
                        .registerMethods(new ClipboardCommands(worldEdit))
                        .registerMethods(new GeneralCommands(worldEdit))
                        .registerMethods(new GenerationCommands(worldEdit))
                        .registerMethods(new HistoryCommands(worldEdit))
                        .registerMethods(new NavigationCommands(worldEdit))
                        .registerMethods(new RegionCommands(worldEdit))
                        .registerMethods(new ScriptingCommands(worldEdit))
                        .registerMethods(new SelectionCommands(worldEdit))
                        .registerMethods(new SnapshotUtilCommands(worldEdit))
                        .registerMethods(new ToolUtilCommands(worldEdit))
                        .registerMethods(new ToolCommands(worldEdit))
                        .registerMethods(new UtilityCommands(worldEdit))
                        .register(adapt(new SelectionCommand(new ApplyCommand(new ReplaceParser(), "Set all blocks within selection"), "worldedit.region.set")), "/set")
                        .group("worldedit", "we")
                            .describeAs("WorldEdit commands")
                            .registerMethods(new WorldEditCommands(worldEdit))
                            .parent()
                        .group("schematic", "schem", "/schematic", "/schem")
                            .describeAs("Schematic commands for saving/loading areas")
                            .registerMethods(new SchematicCommands(worldEdit))
                            .parent()
                        .group("snapshot", "snap")
                            .describeAs("Schematic commands for saving/loading areas")
                            .registerMethods(new SnapshotCommands(worldEdit))
                            .parent()
                        .group("brush", "br")
                            .describeAs("Brushing commands")
                            .registerMethods(new BrushCommands(worldEdit))
                            .register(adapt(new ShapedBrushCommand(new DeformCommand(), "worldedit.brush.deform")), "deform")
                            .register(adapt(new ShapedBrushCommand(new ApplyCommand(new ReplaceParser(), "Set all blocks within region"), "worldedit.brush.set")), "set")
                            .register(adapt(new ShapedBrushCommand(new PaintCommand(), "worldedit.brush.paint")), "paint")
                            .register(adapt(new ShapedBrushCommand(new ApplyCommand(), "worldedit.brush.apply")), "apply")
                            .register(adapt(new ShapedBrushCommand(new PaintCommand(new TreeGeneratorParser("treeType")), "worldedit.brush.forest")), "forest")
                            .register(adapt(new ShapedBrushCommand(ProvidedValue.create(new Deform("y-=1", Mode.RAW_COORD), "Raise one block"), "worldedit.brush.raise")), "raise")
                            .register(adapt(new ShapedBrushCommand(ProvidedValue.create(new Deform("y+=1", Mode.RAW_COORD), "Lower one block"), "worldedit.brush.lower")), "lower")
                        .parent()
                        .group("superpickaxe", "pickaxe", "sp")
                            .describeAs("Super-pickaxe commands")
                            .registerMethods(new SuperPickaxeCommands(worldEdit))
                            .parent()
                        .group("tool")
                            .describeAs("Bind functions to held items")
                            .registerMethods(new ToolCommands(worldEdit))
                            .parent()
                        .graph()
                .getDispatcher();
    }

    public ExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    void register(Platform platform) {
        log.log(Level.FINE, "Registering commands with " + platform.getClass().getCanonicalName());

        LocalConfiguration config = platform.getConfiguration();
        boolean logging = config.logCommands;
        String path = config.logFile;

        // Register log
        if (!logging || path.isEmpty()) {
            dynamicHandler.setHandler(null);
            commandLog.setLevel(Level.OFF);
        } else {
            File file = new File(config.getWorkingDirectory(), path);
            commandLog.setLevel(Level.ALL);

            log.log(Level.INFO, "Logging WorldEdit commands to " + file.getAbsolutePath());

            try {
                dynamicHandler.setHandler(new FileHandler(file.getAbsolutePath(), true));
            } catch (IOException e) {
                log.log(Level.WARNING, "Could not use command log file " + path + ": " + e.getMessage());
            }

            dynamicHandler.setFormatter(new LogFormat(config.logFormat));
        }

        platform.registerCommands(dispatcher);
    }

    void unregister() {
        dynamicHandler.setHandler(null);
    }

    public String[] commandDetection(String[] split) {
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
        if (!dispatcher.contains(searchCmd)) {
            if (worldEdit.getConfiguration().noDoubleSlash && dispatcher.contains("/" + searchCmd)) {
                split[0] = "/" + split[0];
            } else if (searchCmd.length() >= 2 && searchCmd.charAt(0) == '/' && dispatcher.contains(searchCmd.substring(1))) {
                split[0] = split[0].substring(1);
            }
        }

        return split;
    }

    @Subscribe
    public void handleCommand(CommandEvent event) {
        Request.reset();

        Actor actor = platformManager.createProxyActor(event.getActor());
        String[] split = commandDetection(event.getArguments().split(" "));

        // No command found!
        if (!dispatcher.contains(split[0])) {
            return;
        }

        LocalSession session = worldEdit.getSessionManager().get(actor);
        LocalConfiguration config = worldEdit.getConfiguration();

        CommandLocals locals = new CommandLocals();
        locals.put(Actor.class, actor);
        locals.put("arguments", event.getArguments());

        long start = System.currentTimeMillis();

        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                dispatcher.call(Joiner.on(" ").join(split), locals, new String[0]);
            } catch (Throwable t) {
                // Use the exception converter to convert the exception if any of its causes
                // can be converted, otherwise throw the original exception
                Throwable next = t;
                do {
                    exceptionConverter.convert(next);
                    next = next.getCause();
                } while (next != null);

                throw t;
            }
        } catch (CommandPermissionsException e) {
            actor.printError("You are not permitted to do that. Are you in the right mode?");
        } catch (InvalidUsageException e) {
            if (e.isFullHelpSuggested()) {
                actor.printRaw(ColorCodeBuilder.asColorCodes(new CommandUsageBox(e.getCommand(), e.getCommandUsed("/", ""), locals)));
                String message = e.getMessage();
                if (message != null) {
                    actor.printError(message);
                }
            } else {
                String message = e.getMessage();
                actor.printError(message != null ? message : "The command was not used properly (no more help available).");
                actor.printError("Usage: " + e.getSimpleUsageString("/"));
            }
        } catch (WrappedCommandException e) {
            Throwable t = e.getCause();
            actor.printError("Please report this error: [See console]");
            actor.printRaw(t.getClass().getName() + ": " + t.getMessage());
            log.log(Level.SEVERE, "An unexpected error while handling a WorldEdit command", t);
        } catch (CommandException e) {
            String message = e.getMessage();
            if (message != null) {
                actor.printError(e.getMessage());
            } else {
                actor.printError("An unknown error has occurred! Please see console.");
                log.log(Level.SEVERE, "An unknown error occurred", e);
            }
        } finally {
            EditSession editSession = locals.get(EditSession.class);

            if (editSession != null) {
                session.remember(editSession);
                editSession.flushQueue();

                if (config.profile) {
                    long time = System.currentTimeMillis() - start;
                    int changed = editSession.getBlockChangeCount();
                    if (time > 0) {
                        double throughput = changed / (time / 1000.0);
                        actor.printDebug((time / 1000.0) + "s elapsed (history: "
                                + changed + " changed; "
                                + Math.round(throughput) + " blocks/sec).");
                    } else {
                        actor.printDebug((time / 1000.0) + "s elapsed.");
                    }
                }

                worldEdit.flushBlockBag(actor, editSession);
            }
        }

        event.setCancelled(true);
    }

    @Subscribe
    public void handleCommandSuggestion(CommandSuggestionEvent event) {
        try {
            CommandLocals locals = new CommandLocals();
            locals.put(Actor.class, event.getActor());
            locals.put("arguments", event.getArguments());
            event.setSuggestions(dispatcher.getSuggestions(event.getArguments(), locals));
        } catch (CommandException e) {
            event.getActor().printError(e.getMessage());
        }
    }

    /**
     * Get the command dispatcher instance.
     *
     * @return the command dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static Logger getLogger() {
        return commandLog;
    }

}
