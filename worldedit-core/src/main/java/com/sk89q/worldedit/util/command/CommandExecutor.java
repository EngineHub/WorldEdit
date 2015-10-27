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

package com.sk89q.worldedit.util.command;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.CommandArgs;

import java.util.List;

public abstract class CommandExecutor<T> implements CommandCallable {

    @Override
    public final T call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        CommandArgs args = new CommandArgs.Parser().parse(arguments);
        T ret = call(args, locals, parentCommands);
        args.requireAllConsumed();
        return ret;
    }

    public abstract T call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException;

    @Override
    public Description getDescription() {
        return new SimpleDescription();
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return false;
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return Lists.newArrayList();
    }

}
