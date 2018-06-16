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

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple implementation of {@link Dispatcher}.
 */
public class SimpleDispatcher implements Dispatcher {

    private final Map<String, CommandMapping> commands = new HashMap<>();
    private final SimpleDescription description = new SimpleDescription();

    /**
     * Create a new instance.
     */
    public SimpleDispatcher() {
        description.getParameters().add(new SimpleParameter("subcommand"));
        SimpleParameter extraArgs = new SimpleParameter("...");
        extraArgs.setOptional(true);
        description.getParameters().add(extraArgs);
    }

    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        CommandMapping mapping = new SimpleCommandMapping(callable, alias);
        
        // Check for replacements
        for (String a : alias) {
            String lower = a.toLowerCase();
            if (commands.containsKey(lower)) {
                throw new IllegalArgumentException(
                        "Replacing commands is currently undefined behavior");
            }
        }
        
        for (String a : alias) {
            String lower = a.toLowerCase();
            commands.put(lower, mapping);
        }
    }

    @Override
    public Set<CommandMapping> getCommands() {
        return Collections.unmodifiableSet(new HashSet<>(commands.values()));
    }
    
    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(commands.keySet());
    }
    
    @Override
    public Set<String> getPrimaryAliases() {
        Set<String> aliases = new HashSet<>();
        for (CommandMapping mapping : getCommands()) {
            aliases.add(mapping.getPrimaryAlias());
        }
        return Collections.unmodifiableSet(aliases);
    }

    @Override
    public boolean contains(String alias) {
        return commands.containsKey(alias.toLowerCase());
    }

    @Override
    public CommandMapping get(String alias) {
        return commands.get(alias.toLowerCase());
    }

    @Override
    public Object call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        // We have permission for this command if we have permissions for subcommands
        if (!testPermission(locals)) {
            throw new CommandPermissionsException();
        }

        String[] split = CommandContext.split(arguments);
        Set<String> aliases = getPrimaryAliases();

        if (aliases.isEmpty()) {
            throw new InvalidUsageException("This command has no sub-commands.", this);
        } else if (split.length > 0) {
            String subCommand = split[0];
            String subArguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));
            String[] subParents = Arrays.copyOf(parentCommands, parentCommands.length + 1);
            subParents[parentCommands.length] = subCommand;
            CommandMapping mapping = get(subCommand);

            if (mapping != null) {
                try {
                    return mapping.getCallable().call(subArguments, locals, subParents);
                } catch (CommandException e) {
                    e.prependStack(subCommand);
                    throw e;
                } catch (Throwable t) {
                    throw new WrappedCommandException(t);
                }
            }

        }

        throw new InvalidUsageException("Please choose a sub-command.", this, true);
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        String[] split = CommandContext.split(arguments);

        if (split.length <= 1) {
            String prefix = split.length > 0 ? split[0] : "";

            List<String> suggestions = new ArrayList<>();

            for (CommandMapping mapping : getCommands()) {
                if (mapping.getCallable().testPermission(locals)) {
                    for (String alias : mapping.getAllAliases()) {
                        if (prefix.isEmpty() || alias.startsWith(arguments)) {
                            suggestions.add(mapping.getPrimaryAlias());
                            break;
                        }
                    }
                }
            }

            return suggestions;
        } else {
            String subCommand = split[0];
            CommandMapping mapping = get(subCommand);
            String passedArguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));

            if (mapping != null) {
                return mapping.getCallable().getSuggestions(passedArguments, locals);
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public SimpleDescription getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        for (CommandMapping mapping : getCommands()) {
            if (mapping.getCallable().testPermission(locals)) {
                return true;
            }
        }

        return false;
    }

}
