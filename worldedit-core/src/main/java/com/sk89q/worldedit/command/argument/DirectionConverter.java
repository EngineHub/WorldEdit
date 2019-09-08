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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.UnknownDirectionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import org.enginehub.piston.CommandManager;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DirectionConverter extends AbstractDirectionConverter<Direction> {

    private DirectionConverter(WorldEdit worldEdit, boolean includeDiagonals) {
        super(worldEdit, includeDiagonals);
    }

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        for (boolean includeDiagonals : new boolean[] { false, true }) {
            DirectionConverter directionConverter = new DirectionConverter(worldEdit, includeDiagonals);
            register(commandManager, directionConverter, Direction.class, includeDiagonals);
        }
    }

    @Override
    protected Direction convertDirection(String argument, @Nullable Player player, boolean includeDiagonals) throws UnknownDirectionException {
        final BlockVector3 vec = includeDiagonals
                ? getWorldEdit().getDiagonalDirection(player, argument)
                : getWorldEdit().getDirection(player, argument);
        return Optional.ofNullable(Direction.findClosest(vec.toVector3(), Direction.Flag.ALL))
                .orElseThrow(() -> new UnknownDirectionException(argument));
    }

}
