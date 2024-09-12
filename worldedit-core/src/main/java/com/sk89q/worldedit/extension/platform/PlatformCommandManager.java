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

package com.sk89q.worldedit.extension.platform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MissingWorldException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.ApplyBrushCommands;
import com.sk89q.worldedit.command.BiomeCommands;
import com.sk89q.worldedit.command.BiomeCommandsRegistration;
import com.sk89q.worldedit.command.BrushCommands;
import com.sk89q.worldedit.command.BrushCommandsRegistration;
import com.sk89q.worldedit.command.ChunkCommands;
import com.sk89q.worldedit.command.ChunkCommandsRegistration;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.ClipboardCommandsRegistration;
import com.sk89q.worldedit.command.ExpandCommands;
import com.sk89q.worldedit.command.GeneralCommands;
import com.sk89q.worldedit.command.GenerationCommands;
import com.sk89q.worldedit.command.GenerationCommandsRegistration;
import com.sk89q.worldedit.command.HistoryCommands;
import com.sk89q.worldedit.command.HistoryCommandsRegistration;
import com.sk89q.worldedit.command.NavigationCommands;
import com.sk89q.worldedit.command.NavigationCommandsRegistration;
import com.sk89q.worldedit.command.PaintBrushCommands;
import com.sk89q.worldedit.command.RegionCommands;
import com.sk89q.worldedit.command.RegionCommandsRegistration;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.command.SchematicCommandsRegistration;
import com.sk89q.worldedit.command.ScriptingCommands;
import com.sk89q.worldedit.command.ScriptingCommandsRegistration;
import com.sk89q.worldedit.command.SelectionCommands;
import com.sk89q.worldedit.command.SelectionCommandsRegistration;
import com.sk89q.worldedit.command.SnapshotCommands;
import com.sk89q.worldedit.command.SnapshotCommandsRegistration;
import com.sk89q.worldedit.command.SnapshotUtilCommands;
import com.sk89q.worldedit.command.SnapshotUtilCommandsRegistration;
import com.sk89q.worldedit.command.SuperPickaxeCommands;
import com.sk89q.worldedit.command.SuperPickaxeCommandsRegistration;
import com.sk89q.worldedit.command.ToolCommands;
import com.sk89q.worldedit.command.ToolUtilCommands;
import com.sk89q.worldedit.command.ToolUtilCommandsRegistration;
import com.sk89q.worldedit.command.UtilityCommands;
import com.sk89q.worldedit.command.UtilityCommandsRegistration;
import com.sk89q.worldedit.command.WorldEditCommands;
import com.sk89q.worldedit.command.WorldEditCommandsRegistration;
import com.sk89q.worldedit.command.argument.Arguments;
import com.sk89q.worldedit.command.argument.BooleanConverter;
import com.sk89q.worldedit.command.argument.Chunk3dVectorConverter;
import com.sk89q.worldedit.command.argument.ClipboardFormatConverter;
import com.sk89q.worldedit.command.argument.ClipboardShareDestinationConverter;
import com.sk89q.worldedit.command.argument.CommaSeparatedValuesConverter;
import com.sk89q.worldedit.command.argument.DirectionConverter;
import com.sk89q.worldedit.command.argument.DirectionVectorConverter;
import com.sk89q.worldedit.command.argument.EntityRemoverConverter;
import com.sk89q.worldedit.command.argument.EnumConverter;
import com.sk89q.worldedit.command.argument.FactoryConverter;
import com.sk89q.worldedit.command.argument.HeightConverter;
import com.sk89q.worldedit.command.argument.OffsetConverter;
import com.sk89q.worldedit.command.argument.RegionFactoryConverter;
import com.sk89q.worldedit.command.argument.RegistryConverter;
import com.sk89q.worldedit.command.argument.SelectorChoiceConverter;
import com.sk89q.worldedit.command.argument.SideEffectConverter;
import com.sk89q.worldedit.command.argument.SideEffectSetConverter;
import com.sk89q.worldedit.command.argument.VectorConverter;
import com.sk89q.worldedit.command.argument.WorldConverter;
import com.sk89q.worldedit.command.argument.ZonedDateTimeConverter;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.command.util.SubCommandPermissionCondition;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.annotation.OptionalArg;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.internal.command.CommandArgParser;
import com.sk89q.worldedit.internal.command.CommandLoggingHandler;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.exception.ExceptionConverter;
import com.sk89q.worldedit.internal.command.exception.WorldEditExceptionConverter;
import com.sk89q.worldedit.internal.util.ErrorReporting;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.Substring;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;
import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.world.World;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverters;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;
import org.enginehub.piston.exception.ConditionFailedException;
import org.enginehub.piston.exception.UsageException;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.impl.CommandManagerServiceImpl;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;
import org.enginehub.piston.inject.MemoizingValueAccess;
import org.enginehub.piston.inject.MergedValueAccess;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.suggestion.Suggestion;
import org.enginehub.piston.util.HelpGenerator;
import org.enginehub.piston.util.ValueProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the registration and invocation of commands.
 *
 * <p>This class is primarily for internal usage.</p>
 */
public final class PlatformCommandManager {

    public static final Pattern COMMAND_CLEAN_PATTERN = Pattern.compile("^[/]+");
    private static final Logger LOGGER = LogManagerCompat.getLogger();
    private static final java.util.logging.Logger COMMAND_LOG =
        java.util.logging.Logger.getLogger("com.sk89q.worldedit.CommandLog");

    private final WorldEdit worldEdit;
    private final PlatformManager platformManager;
    private final CommandManagerServiceImpl commandManagerService;
    private final CommandManager commandManager;
    private final InjectedValueStore globalInjectedValues;
    private final DynamicStreamHandler dynamicHandler = new DynamicStreamHandler();
    private final WorldEditExceptionConverter exceptionConverter;
    private final CommandRegistrationHandler registration;

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    PlatformCommandManager(final WorldEdit worldEdit, PlatformManager platformManager) {
        checkNotNull(worldEdit);
        checkNotNull(platformManager);
        this.worldEdit = worldEdit;
        this.platformManager = platformManager;
        this.exceptionConverter = new WorldEditExceptionConverter(worldEdit);
        this.commandManagerService = new CommandManagerServiceImpl();
        this.commandManager = commandManagerService.newCommandManager();
        this.globalInjectedValues = MapBackedValueStore.create();
        this.registration = new CommandRegistrationHandler(
            ImmutableList.of(
                new CommandLoggingHandler(worldEdit, COMMAND_LOG)
            ));
        // setup separate from main constructor
        // ensures that everything is definitely assigned
        initialize();
    }

    private void initialize() {
        // Register this instance for command events
        worldEdit.getEventBus().register(this);

        // Setup the logger
        COMMAND_LOG.addHandler(dynamicHandler);

        // Set up the commands manager
        registerAlwaysInjectedValues();
        registerArgumentConverters();
        registerAllCommands();
    }

    private void registerArgumentConverters() {
        DirectionVectorConverter.register(worldEdit, commandManager);
        DirectionConverter.register(worldEdit, commandManager);
        FactoryConverter.register(worldEdit, commandManager);
        for (int count = 2; count <= 3; count++) {
            commandManager.registerConverter(Key.of(double.class, Annotations.radii(count)),
                CommaSeparatedValuesConverter.wrapAndLimit(ArgumentConverters.get(
                    TypeToken.of(double.class)
                ), count)
            );
        }
        VectorConverter.register(commandManager);
        Chunk3dVectorConverter.register(commandManager);
        EnumConverter.register(commandManager);
        RegistryConverter.register(commandManager);
        ZonedDateTimeConverter.register(commandManager);
        BooleanConverter.register(commandManager);
        EntityRemoverConverter.register(commandManager);
        RegionFactoryConverter.register(commandManager);
        WorldConverter.register(commandManager);
        SideEffectConverter.register(commandManager);
        SideEffectSetConverter.register(commandManager);
        HeightConverter.register(commandManager);
        OffsetConverter.register(worldEdit, commandManager);
        ClipboardFormatConverter.register(commandManager);
        ClipboardShareDestinationConverter.register(commandManager);
        SelectorChoiceConverter.register(commandManager);
    }

    private void registerAlwaysInjectedValues() {
        globalInjectedValues.injectValue(Key.of(Region.class, Selection.class),
            context -> {
                LocalSession localSession = context.injectedValue(Key.of(LocalSession.class))
                    .orElseThrow(() -> new IllegalStateException("No LocalSession"));
                return context.injectedValue(Key.of(World.class))
                    .map(world -> {
                        try {
                            return localSession.getSelection(world);
                        } catch (IncompleteRegionException e) {
                            exceptionConverter.convert(e);
                            throw new AssertionError("Should have thrown a new exception.", e);
                        }
                    });
            });
        globalInjectedValues.injectValue(Key.of(EditSession.class),
            context -> {
                LocalSession localSession = context.injectedValue(Key.of(LocalSession.class))
                    .orElseThrow(() -> new IllegalStateException("No LocalSession"));
                return context.injectedValue(Key.of(Actor.class))
                    .map(actor -> {
                        EditSession editSession = localSession.createEditSession(actor);
                        editSession.enableStandardMode();
                        return editSession;
                    });
            });
        globalInjectedValues.injectValue(Key.of(World.class),
            context -> {
                LocalSession localSession = context.injectedValue(Key.of(LocalSession.class))
                    .orElseThrow(() -> new IllegalStateException("No LocalSession"));
                return context.injectedValue(Key.of(Actor.class))
                    .map(actor -> {
                        try {
                            if (localSession.hasWorldOverride()) {
                                return localSession.getWorldOverride();
                            } else if (actor instanceof Locatable && ((Locatable) actor).getExtent() instanceof World) {
                                return (World) ((Locatable) actor).getExtent();
                            } else {
                                throw new MissingWorldException();
                            }
                        } catch (MissingWorldException e) {
                            exceptionConverter.convert(e);
                            throw new AssertionError("Should have thrown a new exception.", e);
                        }
                    });
            });
    }

    /**
     * Internal use only.
     */
    public <CI> void registerSubCommands(String name, List<String> aliases, String desc,
                                         CommandRegistration<CI> registration, CI instance) {
        registerSubCommands(name, aliases, desc, registration, instance, m -> {
        });
    }

    private <CI> void registerSubCommands(String name, List<String> aliases, String desc,
                                          CommandRegistration<CI> registration, CI instance,
                                          Consumer<CommandManager> additionalConfig) {
        commandManager.register(name, cmd -> {
            cmd.aliases(aliases);
            cmd.description(TextComponent.of(desc));
            cmd.action(Command.Action.NULL_ACTION);

            CommandManager manager = commandManagerService.newCommandManager();
            this.registration.register(
                manager,
                registration,
                instance
            );
            additionalConfig.accept(manager);

            final List<Command> subCommands = manager.getAllCommands().collect(Collectors.toList());
            cmd.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"),
                TextComponent.of("Sub-command to run."))
                .withCommands(subCommands)
                .required()
                .build());

            cmd.condition(new SubCommandPermissionCondition.Generator(subCommands).build());
        });
    }

    private void registerAllCommands() {
        registerSubCommands(
            "schematic",
            ImmutableList.of("schem", "/schematic", "/schem"),
            "Schematic commands for saving/loading areas",
            SchematicCommandsRegistration.builder(),
            new SchematicCommands(worldEdit)
        );
        registerSubCommands(
            "snapshot",
            ImmutableList.of("snap"),
            "Snapshot commands for restoring backups",
            SnapshotCommandsRegistration.builder(),
            new SnapshotCommands(worldEdit)
        );
        registerSubCommands(
            "superpickaxe",
            ImmutableList.of("pickaxe", "sp"),
            "Super-pickaxe commands",
            SuperPickaxeCommandsRegistration.builder(),
            new SuperPickaxeCommands(worldEdit)
        );
        registerSubCommands(
            "brush",
            ImmutableList.of("br", "/brush", "/br"),
            "Brushing commands",
            BrushCommandsRegistration.builder(),
            new BrushCommands(worldEdit),
            manager -> {
                PaintBrushCommands.register(commandManagerService, manager, registration);
                ApplyBrushCommands.register(commandManagerService, manager, registration);
            }
        );
        registerSubCommands(
            "worldedit",
            ImmutableList.of("we"),
            "WorldEdit commands",
            WorldEditCommandsRegistration.builder(),
            new WorldEditCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            BiomeCommandsRegistration.builder(),
            new BiomeCommands()
        );
        this.registration.register(
            commandManager,
            ChunkCommandsRegistration.builder(),
            new ChunkCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            ClipboardCommandsRegistration.builder(),
            new ClipboardCommands()
        );
        GeneralCommands.register(
            registration,
            commandManager,
            commandManagerService,
            worldEdit
        );
        this.registration.register(
            commandManager,
            GenerationCommandsRegistration.builder(),
            new GenerationCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            HistoryCommandsRegistration.builder(),
            new HistoryCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            NavigationCommandsRegistration.builder(),
            new NavigationCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            RegionCommandsRegistration.builder(),
            new RegionCommands()
        );
        this.registration.register(
            commandManager,
            ScriptingCommandsRegistration.builder(),
            new ScriptingCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            SelectionCommandsRegistration.builder(),
            new SelectionCommands(worldEdit)
        );
        ExpandCommands.register(registration, commandManager, commandManagerService);
        this.registration.register(
            commandManager,
            SnapshotUtilCommandsRegistration.builder(),
            new SnapshotUtilCommands(worldEdit)
        );
        ToolCommands.register(registration, commandManager, commandManagerService, worldEdit);
        this.registration.register(
            commandManager,
            ToolUtilCommandsRegistration.builder(),
            new ToolUtilCommands(worldEdit)
        );
        this.registration.register(
            commandManager,
            UtilityCommandsRegistration.builder(),
            new UtilityCommands(worldEdit)
        );
    }

    public ExceptionConverter getExceptionConverter() {
        return exceptionConverter;
    }

    void registerCommandsWith(Platform platform) {
        LOGGER.info("Registering commands with " + platform.getClass().getCanonicalName());

        LocalConfiguration config = platform.getConfiguration();
        boolean logging = config.logCommands;
        String path = config.logFile;

        // Register log
        if (!logging || path.isEmpty()) {
            dynamicHandler.setHandler(null);
            COMMAND_LOG.setLevel(Level.OFF);
        } else {
            Path file = config.getWorkingDirectoryPath().resolve(path).toAbsolutePath();
            COMMAND_LOG.setLevel(Level.ALL);

            LOGGER.info("Logging WorldEdit commands to " + file);

            try {
                dynamicHandler.setHandler(new FileHandler(file.toString(), true));
            } catch (IOException e) {
                LOGGER.warn("Could not use command log file " + path + ": " + e.getMessage());
            }

            dynamicHandler.setFormatter(new LogFormat(config.logFormat));
        }

        platform.registerCommands(commandManager);
    }

    void removeCommands() {
        dynamicHandler.setHandler(null);
    }

    private Stream<Substring> parseArgs(String input) {
        return CommandArgParser.forArgString(input.substring(1)).parseArgs();
    }

    @Subscribe
    public void handleCommand(CommandEvent event) {
        Request.reset();

        Actor actor = platformManager.createProxyActor(event.getActor());
        String[] split = parseArgs(event.getArguments())
            .map(Substring::getSubstring)
            .toArray(String[]::new);

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

        MemoizingValueAccess context = initializeInjectedValues(event::getArguments, actor);

        long start = System.currentTimeMillis();

        try {
            // This is a bit of a hack, since the call method can only throw CommandExceptions
            // everything needs to be wrapped at least once. Which means to handle all WorldEdit
            // exceptions without writing a hook into every dispatcher, we need to unwrap these
            // exceptions and rethrow their converted form, if their is one.
            try {
                try {
                    commandManager.execute(context, ImmutableList.copyOf(split));
                } finally {
                    Optional<EditSession> editSessionOpt =
                        context.snapshotMemory().injectedValue(Key.of(EditSession.class));

                    if (editSessionOpt.isPresent()) {
                        EditSession editSession = editSessionOpt.get();
                        session.remember(editSession);
                        editSession.close();

                        if (config.profile) {
                            long time = System.currentTimeMillis() - start;
                            double timeS = (time / 1000.0);
                            int changed = editSession.getBlockChangeCount();
                            double throughput = timeS == 0 ? changed : changed / timeS;
                            actor.printDebug(TranslatableComponent.of(
                                "worldedit.command.time-elapsed",
                                TextComponent.of(timeS),
                                TextComponent.of(changed),
                                TextComponent.of(Math.round(throughput))
                            ));
                        }

                        worldEdit.flushBlockBag(actor, editSession);
                    }
                }
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
                actor.printError(TranslatableComponent.of("worldedit.command.permissions"));
            } else {
                actor.print(e.getRichMessage());
            }
        } catch (UsageException e) {
            actor.print(TextComponent.builder("")
                .color(TextColor.RED)
                .append(e.getRichMessage())
                .build());
            ImmutableList<Command> cmd = e.getCommands();
            if (!cmd.isEmpty()) {
                actor.printError(
                        TranslatableComponent.of("worldedit.error.incorrect-usage",
                        HelpGenerator.create(e.getCommandParseResult()).getUsage())
                );
            }
        } catch (CommandExecutionException e) {
            handleUnknownException(actor, e.getCause());
        } catch (CommandException e) {
            actor.printError(e.getRichMessage());
        } catch (Throwable t) {
            handleUnknownException(actor, t);
        } finally {
            Request.reset();
        }

        event.setCancelled(true);
    }

    private MemoizingValueAccess initializeInjectedValues(Arguments arguments, Actor actor) {
        InjectedValueStore store = MapBackedValueStore.create();
        store.injectValue(Key.of(Actor.class), ValueProvider.constant(actor));
        if (actor instanceof Player) {
            store.injectValue(Key.of(Player.class), ValueProvider.constant((Player) actor));
            store.injectValue(Key.of(Player.class, OptionalArg.class), ValueProvider.constant((Player) actor));
        } else {
            store.injectValue(Key.of(Player.class), context -> {
                throw new CommandException(TranslatableComponent.of("worldedit.command.player-only"), ImmutableList.of());
            });
        }
        store.injectValue(Key.of(Arguments.class), ValueProvider.constant(arguments));
        store.injectValue(Key.of(LocalSession.class),
            context -> {
                LocalSession localSession = worldEdit.getSessionManager().get(actor);
                localSession.tellVersion(actor);
                return Optional.of(localSession);
            });

        return MemoizingValueAccess.wrap(
            MergedValueAccess.of(store, globalInjectedValues)
        );
    }

    private void handleUnknownException(Actor actor, Throwable t) {
        LOGGER.error("An unexpected error while handling a WorldEdit command", t);
        ErrorReporting.trigger(actor, t);
    }

    @Subscribe
    public void handleCommandSuggestion(CommandSuggestionEvent event) {
        try {
            String arguments = event.getArguments();
            List<Substring> split = parseArgs(arguments).toList();
            List<String> argStrings = split.stream()
                .map(Substring::getSubstring)
                .collect(Collectors.toList());
            MemoizingValueAccess access = initializeInjectedValues(() -> arguments, event.getActor());
            ImmutableSet<Suggestion> suggestions;
            try {
                suggestions = commandManager.getSuggestions(access, argStrings);
            } catch (Throwable t) { // catch errors which are *not* command exceptions generated by parsers/suggesters
                if (!(t instanceof CommandException)) {
                    LOGGER.debug("Unexpected error occurred while generating suggestions for input: " + arguments, t);
                    return;
                }
                throw t;
            }

            event.setSuggestions(suggestions.stream()
                .map(suggestion -> {
                    int noSlashLength = arguments.length() - 1;
                    Substring original = suggestion.getReplacedArgument() == split.size()
                        ? Substring.from(arguments, noSlashLength, noSlashLength)
                        : split.get(suggestion.getReplacedArgument());
                    // increase original points by 1, for removed `/` in `parseArgs`
                    return Substring.wrap(
                        suggestion.getSuggestion(),
                        original.getStart() + 1,
                        original.getEnd() + 1
                    );
                }).collect(Collectors.toList()));
        } catch (ConditionFailedException e) {
            if (e.getCondition() instanceof PermissionCondition) {
                event.setSuggestions(new ArrayList<>());
            }
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

}
