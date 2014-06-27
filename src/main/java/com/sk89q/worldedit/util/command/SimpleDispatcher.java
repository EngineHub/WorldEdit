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

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

import java.util.*;

/**
 * A simple implementation of {@link Dispatcher}.
 */
public class SimpleDispatcher implements Dispatcher {

    private final Map<String, CommandMapping> commands = new HashMap<String, CommandMapping>();

    @Override
    public void register(CommandCallable callable, String... alias) {
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
    public Set<String> getAllAliases() {
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
    public CommandMapping call(String arguments, CommandLocals locals) throws CommandException {
        return call(CommandContext.split(arguments), locals);
    }

    @Override
    public CommandMapping call(String[] arguments, CommandLocals locals) throws CommandException {
        CommandContext dummyContext = new CommandContext(arguments);
        CommandMapping mapping = get(dummyContext.getCommand());
        if (mapping != null) {
            CommandCallable c = mapping.getCallable();
            CommandContext context = 
                    new CommandContext(arguments, c.getValueFlags(), false, locals);
            try {
                c.call(context);
            } catch (CommandException e) {
                e.prependStack(context.getCommand());
                throw e;
            } catch (Throwable t) {
                throw new WrappedCommandException(t);
            }
        }
        return mapping;
    }

    @Override
    public Collection<String> getSuggestions(String arguments) throws CommandException {
        CommandContext dummyContext = new CommandContext(arguments);
        CommandMapping mapping = get(dummyContext.getCommand());
        if (mapping != null) {
            CommandCallable c = mapping.getCallable();
            CommandContext context = 
                    new CommandContext(arguments, c.getValueFlags(), true);
            return c.getSuggestions(context);
        }
        return new ArrayList<String>();
    }

}
