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
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A simple implementation of {@link Dispatcher}.
 */
public class SimpleDispatcher implements Dispatcher {

    private final Map<String, CommandMapping> commands = new HashMap<String, CommandMapping>();
    private final SimpleDescription description = new SimpleDescription();

    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        CommandMapping mapping = new CommandMapping(callable, alias);
        
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
    public Collection<CommandMapping> getCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }
    
    @Override
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(commands.keySet());
    }
    
    @Override
    public Set<String> getPrimaryAliases() {
        Set<String> aliases = new HashSet<String>();
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
    public Set<Character> getValueFlags() {
        return Collections.emptySet();
    }

    @Override
    public boolean call(@Nullable String alias, String arguments, CommandLocals locals) throws CommandException {
        String[] split = CommandContext.split(arguments);
        Set<String> aliases = getPrimaryAliases();

        if (aliases.isEmpty()) {
            throw new InvalidUsageException("This command has no sub-commands.", getDescription());
        } else if (split.length > 0) {
            String subCommand = split[0];
            String subArguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));
            CommandMapping mapping = get(subCommand);

            if (mapping != null) {
                try {
                    mapping.getCallable().call(subCommand, subArguments, locals);
                } catch (CommandException e) {
                    e.prependStack(subCommand);
                    throw e;
                } catch (Throwable t) {
                    throw new WrappedCommandException(t);
                }

                return true;
            }

        }

        throw new InvalidUsageException(getSubcommandList(), getDescription());
    }

    @Override
    public List<String> getSuggestions(String arguments) throws CommandException {
        String[] split = CommandContext.split(arguments);

        if (split.length == 0) {
            return new ArrayList<String>(getAliases());
        } else if (split.length == 1) {
            String prefix = split[0];
            if (!prefix.isEmpty()) {
                List<String> suggestions = new ArrayList<String>();

                for (String alias : getAliases()) {
                    if (alias.startsWith(prefix)) {
                        suggestions.add(alias);
                    }
                }

                return suggestions;
            } else {
                return new ArrayList<String>(getAliases());
            }
        } else {
            String subCommand = split[0];
            CommandMapping mapping = get(subCommand);
            String passedArguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));

            if (mapping != null) {
                return mapping.getCallable().getSuggestions(passedArguments);
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

    /**
     * Get a list of subcommands for display.
     *
     * @return a string
     */
    private String getSubcommandList() {
        Set<String> aliases = getPrimaryAliases();

        StringBuilder builder = new StringBuilder("Subcommands: ");
        for (String alias : getPrimaryAliases()) {
            builder.append("\n- ").append(alias);
        }

        if (aliases.size() == 1) {
            builder.append(" (there is only one)");
        }

        return builder.toString();
    }

}
