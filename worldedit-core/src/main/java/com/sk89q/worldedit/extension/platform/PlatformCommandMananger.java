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

import com.google.common.collect.ImmutableList;
import com.google.inject.Key;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.BiomeCommands;
import com.sk89q.worldedit.command.BiomeCommandsRegistration;
import com.sk89q.worldedit.command.BrushCommands;
import com.sk89q.worldedit.command.BrushCommandsRegistration;
import com.sk89q.worldedit.command.ChunkCommands;
import com.sk89q.worldedit.command.ChunkCommandsRegistration;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.command.SchematicCommandsRegistration;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.argument.EditSessionHolder;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.command.ActorAuthorizer;
import com.sk89q.worldedit.internal.command.CommandLoggingHandler;
import com.sk89q.worldedit.internal.command.UserCommandCompleter;
import com.sk89q.worldedit.internal.command.WorldEditBinding;
import com.sk89q.worldedit.internal.command.WorldEditExceptionConverter;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverter;
import com.sk89q.worldedit.util.command.parametric.LegacyCommandsHandler;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;
import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.DefaultCommandManagerService;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.MapBackedValueStore;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the registration and invocation of commands.
 *
 * <p>This class is primarily for internal usage.</p>
 */
public final class PlatformCommandMananger {

    public static final Pattern COMMAND_CLEAN_PATTERN = Pattern.compile("^[/]+");
    private static final Logger log = LoggerFactory.getLogger(PlatformCommandMananger.class);
    private static final java.util.logging.Logger commandLog =
        java.util.logging.Logger.getLogger(PlatformCommandMananger.class.getCanonicalName() + ".CommandLog");
    private static final Pattern numberFormatExceptionPattern = Pattern.compile("^For input string: \"(.*)\"$");
    private static final CommandPermissionsConditionGenerator PERM_GEN = new CommandPermissionsConditionGenerator();

    private final WorldEdit worldEdit;
    private final PlatformManager platformManager;
    private final CommandManager commandManager;
    private final DynamicStreamHandler dynamicHandler = new DynamicStreamHandler();
    private final ExceptionConverter exceptionConverter;
    private final List<CommandCallListener> callListeners;

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    PlatformCommandMananger(final WorldEdit worldEdit, PlatformManager platformManager) {
        checkNotNull(worldEdit);
        checkNotNull(platformManager);
        this.worldEdit = worldEdit;
        this.platformManager = platformManager;
        this.exceptionConverter = new WorldEditExceptionConverter(worldEdit);
        this.commandManager = DefaultCommandManagerService.getInstance()
            .newCommandManager();
        this.callListeners = Collections.singletonList(
            new CommandLoggingHandler(worldEdit, commandLog)
        );
        // setup separate from main constructor
        // ensures that everything is definitely assigned
        initialize();
    }

    private <CI> void register(CommandManager manager, CommandRegistration<CI> registration, CI instance) {
        registration.containerInstance(instance)
            .commandManager(manager)
            .listeners(callListeners);
        if (registration instanceof CommandPermissionsConditionGenerator.Registration) {
            ((CommandPermissionsConditionGenerator.Registration) registration).commandPermissionsConditionGenerator(
                PERM_GEN
            );
        }
        registration.build();
    }

    private void initialize() {
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

        commandManager.register("schematic", cmd -> {
            cmd.aliases(ImmutableList.of("schem", "/schematic", "/schem"));
            cmd.description("Schematic commands for saving/loading areas");
            cmd.action(Command.Action.NULL_ACTION);

            CommandManager manager = DefaultCommandManagerService.getInstance()
                .newCommandManager();
            register(
                manager,
                SchematicCommandsRegistration.builder(),
                new SchematicCommands(worldEdit)
            );

            cmd.addPart(SubCommandPart.builder("action", "Sub-command to run.")
                .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                .required()
                .build());
        });
        commandManager.register("brush", cmd -> {
            cmd.aliases(ImmutableList.of("br"));
            cmd.description("Brushing commands");
            cmd.action(Command.Action.NULL_ACTION);

            CommandManager manager = DefaultCommandManagerService.getInstance()
                .newCommandManager();
            register(
                manager,
                BrushCommandsRegistration.builder(),
                new BrushCommands(worldEdit)
            );

            cmd.addPart(SubCommandPart.builder("action", "Sub-command to run.")
                .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                .required()
                .build());
        });
        register(
            commandManager,
            BiomeCommandsRegistration.builder(),
            new BiomeCommands()
        );
        register(
            commandManager,
            ChunkCommandsRegistration.builder(),
            new ChunkCommands(worldEdit)
        );

        // Unported commands are below. Delete once they're added to the main manager above.
        /*
        dispatcher = new CommandGraph()
                .builder(builder)
                    .commands()
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
                        .group("snapshot", "snap")
                            .describeAs("Schematic commands for saving/loading areas")
                            .registerMethods(new SnapshotCommands(worldEdit))
                            .parent()
                        .group("brush", "br")
                            .describeAs("Brushing commands")
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
         */
    }

    public ExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    void register(Platform platform) {
        log.info("Registering commands with " + platform.getClass().getCanonicalName());

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

            log.info("Logging WorldEdit commands to " + file.getAbsolutePath());

            try {
                dynamicHandler.setHandler(new FileHandler(file.getAbsolutePath(), true));
            } catch (IOException e) {
                log.warn("Could not use command log file " + path + ": " + e.getMessage());
            }

            dynamicHandler.setFormatter(new LogFormat(config.logFormat));
        }

        platform.registerCommands(commandManager);
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
        if (!commandManager.containsCommand(searchCmd)) {
            if (worldEdit.getConfiguration().noDoubleSlash && commandManager.containsCommand("/" + searchCmd)) {
                split[0] = "/" + split[0];
            } else if (searchCmd.length() >= 2 && searchCmd.charAt(0) == '/' && commandManager.containsCommand(searchCmd.substring(1))) {
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
        if (!commandManager.containsCommand(split[0])) {
            return;
        }

        LocalSession session = worldEdit.getSessionManager().get(actor);
        Request.request().setSession(session);
        if (actor instanceof Entity) {
            Extent extent = ((Entity) actor).getExtent();
            if (extent instanceof World) {
                Request.request().setWorld(((World) extent));
            }
        }
        LocalConfiguration config = worldEdit.getConfiguration();

        InjectedValueStore store = MapBackedValueStore.create();
        store.injectValue(Key.get(Actor.class), ValueProvider.constant(actor));
        if (actor instanceof Player) {
            store.injectValue(Key.get(Player.class), ValueProvider.constant((Player) actor));
        }
        store.injectValue(Key.get(Arguments.class), ValueProvider.constant(event::getArguments));
        store.injectValue(Key.get(LocalSession.class),
            context -> {
                LocalSession localSession = worldEdit.getSessionManager().get(actor);
                localSession.tellVersion(actor);
                return Optional.of(localSession);
            });
        store.injectValue(Key.get(EditSession.class),
            context -> {
                LocalSession localSession = context.injectedValue(Key.get(LocalSession.class))
                    .orElseThrow(() -> new IllegalStateException("No LocalSession"));
                return context.injectedValue(Key.get(Player.class))
                    .map(player -> {
                        EditSession editSession = localSession.createEditSession(player);
                        editSession.enableStandardMode();
                        return editSession;
                    });
            });

        MemoizingValueAccess context = MemoizingValueAccess.wrap(store);

        long start = System.currentTimeMillis();

        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                commandManager.execute(context, ImmutableList.copyOf(split));
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
        } catch (ConditionFailedException e) {
            if (e.getCondition() instanceof PermissionCondition) {
                actor.printError("You are not permitted to do that. Are you in the right mode?");
            }
        } catch (UsageException e) {
            String message = e.getMessage();
            actor.printError(message != null ? message : "The command was not used properly (no more help available).");
        } catch (CommandExecutionException e) {
            Throwable t = e.getCause();
            actor.printError("Please report this error: [See console]");
            actor.printRaw(t.getClass().getName() + ": " + t.getMessage());
            log.error("An unexpected error while handling a WorldEdit command", t);
        } catch (CommandException e) {
            String message = e.getMessage();
            if (message != null) {
                actor.printError(e.getMessage());
            } else {
                actor.printError("An unknown error has occurred! Please see console.");
                log.error("An unknown error occurred", e);
            }
        } finally {
            Optional<EditSession> editSessionOpt =
                context.injectedValueIfMemoized(Key.get(EditSession.class));

            if (editSessionOpt.isPresent()) {
                EditSession editSession = editSessionOpt.get();
                session.remember(editSession);
                editSession.flushSession();

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
            Request.reset();
        }

        event.setCancelled(true);
    }

    @Subscribe
    public void handleCommandSuggestion(CommandSuggestionEvent event) {
        try {
            commandManager.injectValue(Key.get(Actor.class), ValueProvider.constant(event.getActor()));
            commandManager.injectValue(Key.get(Arguments.class), ValueProvider.constant(event::getArguments));
            // TODO suggestions
        } catch (CommandException e) {
            event.getActor().printError(e.getMessage());
        }
    }

    /**
     * Get the command manager instance.
     *
     * @return the command manager
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public static java.util.logging.Logger getLogger() {
        return commandLog;
    }

}
