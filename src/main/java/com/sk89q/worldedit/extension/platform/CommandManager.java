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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.*;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.CommandLoggingHandler;
import com.sk89q.worldedit.util.CommandPermissionsHandler;
import com.sk89q.worldedit.util.WorldEditBinding;
import com.sk89q.worldedit.util.WorldEditExceptionConverter;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.InvalidUsageException;
import com.sk89q.worldedit.util.command.fluent.CommandGraph;
import com.sk89q.worldedit.util.command.parametric.LegacyCommandsHandler;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;
import com.sk89q.worldedit.util.logging.LogFormat;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles the registration and invocation of commands.
 * </p>
 * This class is primarily for internal usage.
 */
public final class CommandManager {

    private static final Logger logger = Logger.getLogger(CommandManager.class.getCanonicalName());
    private static final java.util.regex.Pattern numberFormatExceptionPattern = java.util.regex.Pattern.compile("^For input string: \"(.*)\"$");

    private final WorldEdit worldEdit;
    private final Dispatcher dispatcher;
    private final DynamicStreamHandler dynamicHandler = new DynamicStreamHandler();

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    CommandManager(final WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;

        // Register this instance for command events
        worldEdit.getEventBus().register(this);

        // Setup the logger
        logger.addHandler(dynamicHandler);
        dynamicHandler.setFormatter(new LogFormat());

        // Set up the commands manager
        ParametricBuilder builder = new ParametricBuilder();
        builder.addBinding(new WorldEditBinding(worldEdit));
        builder.attach(new CommandPermissionsHandler());
        builder.attach(new WorldEditExceptionConverter(worldEdit));
        builder.attach(new LegacyCommandsHandler());
        builder.attach(new CommandLoggingHandler(worldEdit, logger));

        dispatcher = new CommandGraph()
                .builder(builder)
                    .commands()
                        .build(new BiomeCommands(worldEdit))
                        .build(new ChunkCommands(worldEdit))
                        .build(new ClipboardCommands(worldEdit))
                        .build(new GeneralCommands(worldEdit))
                        .build(new GenerationCommands(worldEdit))
                        .build(new HistoryCommands(worldEdit))
                        .build(new NavigationCommands(worldEdit))
                        .build(new RegionCommands(worldEdit))
                        .build(new ScriptingCommands(worldEdit))
                        .build(new SelectionCommands(worldEdit))
                        .build(new SnapshotUtilCommands(worldEdit))
                        .build(new ToolUtilCommands(worldEdit))
                        .build(new ToolCommands(worldEdit))
                        .build(new UtilityCommands(worldEdit))
                        .group("worldedit", "we")
                            .describe("WorldEdit commands")
                            .build(new WorldEditCommands(worldEdit))
                            .parent()
                        .group("schematic", "schem", "/schematic", "/schem")
                            .describe("Schematic commands for saving/loading areas")
                            .build(new SchematicCommands(worldEdit))
                            .parent()
                        .group("snapshot", "snap")
                            .describe("Schematic commands for saving/loading areas")
                            .build(new SnapshotCommands(worldEdit))
                            .parent()
                        .group("brush", "br")
                            .describe("Brushing commands")
                            .build(new BrushCommands(worldEdit))
                            .parent()
                        .group("superpickaxe", "pickaxe", "sp")
                            .describe("Super-pickaxe commands")
                            .build(new SuperPickaxeCommands(worldEdit))
                            .parent()
                        .group("tool")
                            .describe("Bind functions to held items")
                            .build(new ToolCommands(worldEdit))
                            .parent()
                        .graph()
                .getDispatcher();
    }

    void register(Platform platform) {
        logger.log(Level.FINE, "Registering commands with " + platform.getClass().getCanonicalName());

        LocalConfiguration config = platform.getConfiguration();
        boolean logging = config.logCommands;
        String path = config.logFile;

        // Register log
        if (!logging || path.isEmpty()) {
            dynamicHandler.setHandler(null);
        } else {
            File file = new File(config.getWorkingDirectory(), path);

            logger.log(Level.INFO, "Logging WorldEdit commands to " + file.getAbsolutePath());

            try {
                dynamicHandler.setHandler(new FileHandler(file.getAbsolutePath(), true));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not use command log file " + path + ": " + e.getMessage());
            }
        }

        platform.registerCommands(dispatcher);
    }

    void unregister() {
        dynamicHandler.setHandler(null);
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
        } else if (worldEdit.getConfiguration().noDoubleSlash && dispatcher.contains("/" + searchCmd)) {
            split[0] = "/" + split[0];
        } else if (split[0].length() >= 2 && split[0].charAt(0) == '/'
                && dispatcher.contains(searchCmd.substring(1))) {
            split[0] = split[0].substring(1);
        }
        return split;
    }

    @Subscribe
    public void handleCommand(CommandEvent event) {
        Request.reset();

        Actor actor = event.getPlayer();
        String split[] = commandDetection(event.getArguments());

        // No command found!
        if (!dispatcher.contains(split[0])) {
            return;
        }

        LocalSession session = worldEdit.getSessionManager().get(actor);
        LocalConfiguration config = worldEdit.getConfiguration();

        CommandLocals locals = new CommandLocals();
        locals.put(Actor.class, actor);

        long start = System.currentTimeMillis();

        try {
            dispatcher.call(split, locals);
        } catch (CommandPermissionsException e) {
            actor.printError("You don't have permission to do this.");
        } catch (InvalidUsageException e) {
            actor.printError(e.getMessage() + "\nUsage: " + e.getUsage("/"));
        } catch (WrappedCommandException e) {
            Throwable t = e.getCause();
            actor.printError("Please report this error: [See console]");
            actor.printRaw(t.getClass().getName() + ": " + t.getMessage());
            t.printStackTrace();
        } catch (CommandException e) {
            actor.printError(e.getMessage());
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

                worldEdit.flushBlockBag(event.getPlayer(), editSession);
            }
        }

        event.setCancelled(true);
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
        return logger;
    }

}
