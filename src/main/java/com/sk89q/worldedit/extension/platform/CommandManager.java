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

import com.sk89q.minecraft.util.commands.*;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.command.*;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.util.logging.DynamicStreamHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.*;
import java.util.regex.Matcher;

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
    private final CommandsManager<LocalPlayer> commands;
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
        commands = new CommandsManagerImpl();
        commands.setInjector(new SimpleInjector(worldEdit));
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

        register(platform, BiomeCommands.class);
        register(platform, ChunkCommands.class);
        register(platform, ClipboardCommands.class);
        register(platform, GeneralCommands.class);
        register(platform, GenerationCommands.class);
        register(platform, HistoryCommands.class);
        register(platform, NavigationCommands.class);
        register(platform, RegionCommands.class);
        register(platform, ScriptingCommands.class);
        register(platform, SelectionCommands.class);
        register(platform, SnapshotUtilCommands.class);
        register(platform, ToolUtilCommands.class);
        register(platform, ToolCommands.class);
        register(platform, UtilityCommands.class);
    }

    void unregister() {
        dynamicHandler.setHandler(null);
    }

    private void register(Platform platform, Class<?> clazz) {
        platform.onCommandRegistration(commands.registerAndReturn(clazz), commands);
    }

    public CommandsManager<LocalPlayer> getCommands() {
        return commands;
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
        } else if (worldEdit.getConfiguration().noDoubleSlash && commands.hasCommand("/" + searchCmd)) {
            split[0] = "/" + split[0];
        } else if (split[0].length() >= 2 && split[0].charAt(0) == '/'
                && commands.hasCommand(searchCmd.substring(1))) {
            split[0] = split[0].substring(1);
        }

        return split;
    }

    @Subscribe
    public void handleCommand(CommandEvent event) {
        Request.reset();

        LocalPlayer player = event.getPlayer();
        String[] split = event.getArguments();

        try {
            split = commandDetection(split);

            // No command found!
            if (!commands.hasCommand(split[0])) {
                return;
            }

            LocalSession session = worldEdit.getSession(player);
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
                event.setCancelled(true);
                return;
            } finally {
                session.remember(editSession);
                editSession.flushQueue();

                if (worldEdit.getConfiguration().profile) {
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

                worldEdit.flushBlockBag(player, editSession);
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
            player.printError("Maximum allowed brush size: " + worldEdit.getConfiguration().maxBrushRadius);
        } catch (MaxRadiusException e) {
            player.printError("Maximum allowed size: " + worldEdit.getConfiguration().maxRadius);
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

        event.setCancelled(true);
    }

    private class CommandsManagerImpl extends CommandsManager<LocalPlayer> {
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
            if (worldEdit.getConfiguration().logCommands) {
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
                    final LocalSession session = worldEdit.getSessionManager().get(player);

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

                getLogger().info(msg);
            }
            super.invokeMethod(parent, args, player, method, instance, methodArgs, level);
        }
    }

    public static Logger getLogger() {
        return logger;
    }

}
