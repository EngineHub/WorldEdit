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

package com.sk89q.worldedit.command.tool.brush;

import com.google.common.base.Function;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.command.composition.PointGeneratorCommand;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.factory.RegionApply;
import com.sk89q.worldedit.util.command.CommandExecutor;
import com.sk89q.worldedit.util.command.argument.CommandArgs;

public class ApplyCommand extends CommandExecutor<RegionApply> {

    @Override
    public RegionApply call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException {
        Function<EditContext, ? extends RegionFunction> function = new PointGeneratorCommand().call(args, locals, parentCommands);
        return new RegionApply(function);
    }

}
