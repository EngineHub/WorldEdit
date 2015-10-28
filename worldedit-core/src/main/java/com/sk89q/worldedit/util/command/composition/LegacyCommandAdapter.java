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

package com.sk89q.worldedit.util.command.composition;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.command.SimpleDescription;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;
import com.sk89q.worldedit.util.command.argument.UnusedArgumentsException;

import java.util.List;

public class LegacyCommandAdapter implements CommandCallable {

    private final CommandExecutor<?> executor;

    private LegacyCommandAdapter(CommandExecutor<?> executor) {
        this.executor = executor;
    }

    @Override
    public final Object call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        CommandArgs args = new CommandArgs.Parser().parse(arguments);

        if (args.hasNext()) {
            if (args.uncheckedPeek().equals("-?")) {
                throw new CommandException(executor.getUsage());
            }
        }

        Object ret = executor.call(args, locals);
        try {
            args.requireAllConsumed();
        } catch (UnusedArgumentsException e) {
            throw new CommandException(e.getMessage());
        }
        return ret;
    }

    @Override
    public Description getDescription() {
        return new SimpleDescription()
                .setDescription(executor.getDescription())
                .overrideUsage(executor.getUsage());
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return executor.testPermission(locals);
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        CommandArgs args = new CommandArgs.Parser().setUsingHangingArguments(true).parse(arguments);
        try {
            return executor.getSuggestions(args, locals);
        } catch (MissingArgumentException e) {
            return Lists.newArrayList();
        }
    }

    public static LegacyCommandAdapter adapt(CommandExecutor<?> executor) {
        return new LegacyCommandAdapter(executor);
    }

}
