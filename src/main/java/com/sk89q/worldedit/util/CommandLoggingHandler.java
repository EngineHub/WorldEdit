// $Id$

package com.sk89q.worldedit.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.rebar.command.parametric.AbstractInvokeListener;
import com.sk89q.rebar.command.parametric.InvokeHandler;
import com.sk89q.rebar.command.parametric.ParameterData;
import com.sk89q.rebar.command.parametric.ParameterException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;

/**
 * Logs called commands to a file.
 */
public class CommandLoggingHandler 
        extends AbstractInvokeListener 
        implements InvokeHandler, Closeable {
    
    private static final Logger logger = 
            Logger.getLogger(CommandLoggingHandler.class.getCanonicalName());
    
    private final WorldEdit worldEdit;
    private final LocalConfiguration config;
    private final Logger commandLogger = 
            Logger.getLogger("Minecraft.WorldEdit.CommandLogger");

    /**
     * Create a new instance.
     * 
     * @param worldEdit WorldEdit instance
     * @param config the configuration
     */
    public CommandLoggingHandler(WorldEdit worldEdit, LocalConfiguration config) {
        this.worldEdit = worldEdit;
        this.config = config;
        
        if (!config.logFile.equals("")) {
            try {
                FileHandler logFileHandler;
                File file = new File(config.getWorkingDirectory(), config.logFile);
                logFileHandler = new FileHandler(file.getAbsolutePath(), true);
                logFileHandler.setFormatter(new LogFormat());
                commandLogger.addHandler(logFileHandler);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not use command log file "
                        + config.logFile + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void preProcess(Object object, Method method,
            ParameterData[] parameters, CommandContext context) 
                    throws CommandException, ParameterException {
    }

    @Override
    public void preInvoke(Object object, Method method, ParameterData[] parameters, 
            Object[] args, CommandContext context) throws CommandException {
        if (!config.logCommands) {
            return;
        }
        
        Logging loggingAnnotation = method.getAnnotation(Logging.class);
        Logging.LogMode logMode;
        StringBuilder builder = new StringBuilder();
        
        if (loggingAnnotation == null) {
            logMode = null;
        } else {
            logMode = loggingAnnotation.value();
        }

        LocalPlayer sender = context.getLocals().get(LocalPlayer.class);
        if (sender == null) {
            return;
        }

        builder.append("WorldEdit: ").append(sender.getName());
        if (sender.isPlayer()) {
            builder.append(" (in \"" + sender.getWorld().getName() + "\")");
        }

        builder.append(": ").append(context.getCommand());
        
        if (context.argsLength() > 0) {
            builder.append(" ").append(context.getJoinedStrings(0));
        }
        
        if (logMode != null && sender.isPlayer()) {
            Vector position = sender.getPosition();
            LocalSession session = worldEdit.getSession(sender);
            
            switch (logMode) {
            case PLACEMENT:
                try {
                    position = session.getPlacementPosition(sender);
                } catch (IncompleteRegionException e) {
                    break;
                }
                /* FALL-THROUGH */

            case POSITION:
                builder.append(" - Position: " + position);
                break;

            case ALL:
                builder.append(" - Position: " + position);
                /* FALL-THROUGH */

            case ORIENTATION_REGION:
                builder.append(" - Orientation: "
                        + sender.getCardinalDirection().name());
                /* FALL-THROUGH */

            case REGION:
                try {
                    builder.append(" - Region: ")
                            .append(session.getSelection(sender.getWorld()));
                } catch (IncompleteRegionException e) {
                    break;
                }
                break;
            }
        }

        commandLogger.info(builder.toString());
    }

    @Override
    public void postInvoke(Object object, Method method, ParameterData[] parameters, 
            Object[] args, CommandContext context) throws CommandException {
    }

    @Override
    public InvokeHandler createInvokeHandler() {
        return this;
    }

    @Override
    public void close() {
        for (Handler h : commandLogger.getHandlers()) {
            h.close();
        }
    }

}
