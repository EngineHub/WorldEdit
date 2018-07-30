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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.command.argument.RegionFunctionParser;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.factory.Apply;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;

public class ApplyCommand extends SimpleCommand<Contextual<? extends Operation>> {

    private final CommandExecutor<Contextual<? extends RegionFunction>> functionParser;
    private final String description;

    public ApplyCommand() {
        this(new RegionFunctionParser(), "Applies a function to every block");
    }

    public ApplyCommand(CommandExecutor<Contextual<? extends RegionFunction>> functionParser, String description) {
        checkNotNull(functionParser, "functionParser");
        checkNotNull(description, "description");
        this.functionParser = functionParser;
        this.description = description;
        addParameter(functionParser);
    }

    @Override
    public Apply call(CommandArgs args, CommandLocals locals) throws CommandException {
        Contextual<? extends RegionFunction> function = functionParser.call(args, locals);
        return new Apply(function);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    protected boolean testPermission0(CommandLocals locals) {
        return true;
    }

}
