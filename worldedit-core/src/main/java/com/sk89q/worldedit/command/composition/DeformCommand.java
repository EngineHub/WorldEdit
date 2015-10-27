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

package com.sk89q.worldedit.command.composition;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.factory.Deform;
import com.sk89q.worldedit.function.factory.Deform.Mode;
import com.sk89q.worldedit.util.command.CommandExecutor;
import com.sk89q.worldedit.util.command.argument.CommandArgs;

public class DeformCommand extends CommandExecutor<Deform> {

    @Override
    public Deform call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException {
        String expression = args.next();
        boolean rawCoords = args.containsFlag('r');
        boolean offset = args.containsFlag('o');

        Deform deform = new Deform(expression);

        if (rawCoords) {
            deform.setMode(Mode.RAW_COORD);
        } else if (offset) {
            deform.setMode(Mode.OFFSET);
            Player player = (Player) locals.get(Actor.class);
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(locals.get(Actor.class));
            try {
                deform.setOffset(session.getPlacementPosition(player));
            } catch (IncompleteRegionException e) {
                throw new WrappedCommandException(e);
            }
        } else {
            deform.setMode(Mode.UNIT_CUBE);
        }

        return deform;
    }

}
