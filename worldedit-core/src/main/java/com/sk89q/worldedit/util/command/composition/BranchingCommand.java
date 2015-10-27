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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.ArgumentUtils;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BranchingCommand<T> implements CommandExecutor<T> {

    private final String name;
    private final Map<String, CommandExecutor<? extends T>> options = Maps.newHashMap();
    private final Set<String> primaryAliases = Sets.newHashSet();

    public BranchingCommand(String name) {
        this.name = name;
    }

    public void putOption(CommandExecutor<? extends T> executor, String primaryAlias, String... aliases) {
        options.put(primaryAlias, executor);
        primaryAliases.add(primaryAlias);
        for (String alias : aliases) {
            options.put(alias, executor);
        }
    }

    @Override
    public T call(CommandArgs args, CommandLocals locals) throws CommandException {
        try {
            String classifier = args.next();
            CommandExecutor<? extends T> executor = options.get(classifier.toLowerCase());
            if (executor != null) {
                return executor.call(args, locals);
            } else {
                throw new CommandException("'" + classifier + "' isn't a valid option for '" + name + "'. " +
                        "Try one of: " + Joiner.on(", ").join(primaryAliases));
            }
        } catch (MissingArgumentException e) {
            throw new CommandException("Missing value for <" + name + "> " +
                    "(try one of " + Joiner.on(" | ").join(primaryAliases) + ").");
        }
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        String classifier = args.next();
        try {
            CommandExecutor<? extends T> executor = options.get(classifier.toLowerCase());
            if (executor != null) {
                return executor.getSuggestions(args, locals);
            }
        } catch (MissingArgumentException ignored) {
        }

        return ArgumentUtils.getMatchingSuggestions((classifier.isEmpty() ? primaryAliases : options.keySet()), classifier);
    }

    @Override
    public String getUsage() {
        List<String> optionUsages = Lists.newArrayList();
        for (String alias : primaryAliases) {
            CommandExecutor<? extends T> executor = options.get(alias);
            String usage = executor.getUsage();
            if (usage.isEmpty()) {
                optionUsages.add(alias);
            } else {
                optionUsages.add(alias + " " + executor.getUsage());
            }
        }

        return "(" + Joiner.on(" | ").join(optionUsages) + ")";
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        for (CommandExecutor<?> executor : options.values()) {
            if (!executor.testPermission(locals)) {
                return false;
            }
        }
        return true;
    }

}
