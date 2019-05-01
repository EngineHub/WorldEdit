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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.Vector3;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.gen.CommandCallListener;
import org.enginehub.piston.inject.Key;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Logs called commands to a logger.
 */
public class CommandLoggingHandler implements CommandCallListener, AutoCloseable {

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
    public void beforeCall(Method method, CommandParameters parameters) {
        Logging loggingAnnotation = method.getAnnotation(Logging.class);
        Logging.LogMode logMode;
        StringBuilder builder = new StringBuilder();

        if (loggingAnnotation == null) {
            logMode = null;
        } else {
            logMode = loggingAnnotation.value();
        }

        Optional<Player> playerOpt = parameters.injectedValue(Key.of(Actor.class))
            .filter(Player.class::isInstance)
            .map(Player.class::cast);

        if (!playerOpt.isPresent()) {
            return;
        }

        Player player = playerOpt.get();

        builder.append("WorldEdit: ").append(player.getName());
        builder.append(" (in \"").append(player.getWorld().getName()).append("\")");

        builder.append(": ").append(parameters.getMetadata().getCalledName());

        builder.append(": ")
            .append(Stream.concat(
                Stream.of(parameters.getMetadata().getCalledName()),
                parameters.getMetadata().getArguments().stream()
            ).collect(Collectors.joining(" ")));

        if (logMode != null) {
            Vector3 position = player.getLocation().toVector();
            LocalSession session = worldEdit.getSessionManager().get(player);

            switch (logMode) {
                case PLACEMENT:
                    try {
                        position = session.getPlacementPosition(player).toVector3();
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
    public void close() {
        for (Handler h : logger.getHandlers()) {
            h.close();
        }
    }

}
