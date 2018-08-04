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

package com.sk89q.worldedit.internal.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.parametric.AbstractInvokeListener;
import com.sk89q.worldedit.util.command.parametric.InvokeHandler;
import com.sk89q.worldedit.util.command.parametric.ParameterData;
import com.sk89q.worldedit.util.command.parametric.ParameterException;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Logs called commands to a logger.
 */
public class CommandLoggingHandler extends AbstractInvokeListener implements InvokeHandler, Closeable {

    private final WorldEdit worldEdit;
    private final Logger logger;

    /**
     * Create a new instance.
     *
     * @param worldEdit an instance of WorldEdit
     * @param logger the logger to send messages to
     */
    public CommandLoggingHandler(WorldEdit worldEdit, Logger logger) {
        checkNotNull(worldEdit);
        checkNotNull(logger);
        this.worldEdit = worldEdit;
        this.logger = logger;
    }

    @Override
    public void preProcess(Object object, Method method, ParameterData[] parameters, CommandContext context) throws CommandException, ParameterException {
    }

    @Override
    public void preInvoke(Object object, Method method, ParameterData[] parameters, Object[] args, CommandContext context) throws CommandException {
        Logging loggingAnnotation = method.getAnnotation(Logging.class);
        Logging.LogMode logMode;
        StringBuilder builder = new StringBuilder();
        
        if (loggingAnnotation == null) {
            logMode = null;
        } else {
            logMode = loggingAnnotation.value();
        }

        Actor sender = context.getLocals().get(Actor.class);
        Player player;

        if (sender == null) {
            return;
        }

        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return;
        }

        builder.append("WorldEdit: ").append(sender.getName());
        if (sender.isPlayer()) {
            builder.append(" (in \"").append(player.getWorld().getName()).append("\")");
        }

        builder.append(": ").append(context.getCommand());
        
        if (context.argsLength() > 0) {
            builder.append(" ").append(context.getJoinedStrings(0));
        }
        
        if (logMode != null && sender.isPlayer()) {
            Vector position = player.getLocation().toVector();
            LocalSession session = worldEdit.getSessionManager().get(player);
            
            switch (logMode) {
            case PLACEMENT:
                try {
                    position = session.getPlacementPosition(player);
                } catch (IncompleteRegionException e) {
                    break;
                }
                /* FALL-THROUGH */

            case POSITION:
                builder.append(" - Position: ").append(position);
                break;

            case ALL:
                builder.append(" - Position: ").append(position);
                /* FALL-THROUGH */

            case ORIENTATION_REGION:
                builder.append(" - Orientation: ").append(player.getCardinalDirection().name());
                /* FALL-THROUGH */

            case REGION:
                try {
                    builder.append(" - Region: ")
                            .append(session.getSelection(player.getWorld()));
                } catch (IncompleteRegionException e) {
                    break;
                }
                break;
            }
        }

        logger.info(builder.toString());
    }

    @Override
    public void postInvoke(Object object, Method method, ParameterData[] parameters, Object[] args, CommandContext context) throws CommandException {
    }

    @Override
    public InvokeHandler createInvokeHandler() {
        return this;
    }

    @Override
    public void close() {
        for (Handler h : logger.getHandlers()) {
            h.close();
        }
    }

}
