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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.composition.FlagParser.Flag;

import java.util.List;

public abstract class ParameterCommand<T> implements CommandExecutor<T> {

    private final List<CommandExecutor<?>> parameters = Lists.newArrayList();
    private final FlagParser flagParser = new FlagParser();

    public ParameterCommand() {
        addParameter(flagParser);
    }

    protected List<CommandExecutor<?>> getParameters() {
        return parameters;
    }

    public <E extends CommandExecutor<?>> E addParameter(E executor) {
        parameters.add(executor);
        return executor;
    }

    public <E> Flag<E> addFlag(char flag, CommandExecutor<E> executor) {
        return flagParser.registerFlag(flag, executor);
    }

    protected FlagParser getFlagParser() {
        return flagParser;
    }

    @Override
    public final String getUsage() {
        List<String> parts = Lists.newArrayList();
        for (CommandExecutor<?> executor : parameters) {
            String usage = executor.getUsage();
            if (!usage.isEmpty()) {
                parts.add(executor.getUsage());
            }
        }
        return Joiner.on(" ").join(parts);
    }

    @Override
    public final boolean testPermission(CommandLocals locals) {
        for (CommandExecutor<?> executor : parameters) {
            if (!executor.testPermission(locals)) {
                return false;
            }
        }
        return testPermission0(locals);
    }

    protected abstract boolean testPermission0(CommandLocals locals);

}
