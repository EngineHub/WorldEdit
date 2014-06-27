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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;

import java.util.Collection;

/**
 * Executes a command based on user input.
 */
public interface Dispatcher {

    /**
     * Register a command with this dispatcher.
     * 
     * @param callable the command executor
     * @param alias a list of aliases, where the first alias is the primary name
     */
    void register(CommandCallable callable, String... alias);
    
    /**
     * Get a list of command registrations.
     * 
     * <p>The returned collection cannot be modified.</p>
     * 
     * @return a list of registrations
     */
    Collection<CommandMapping> getCommands();

    /**
     * Get a list of primary aliases.
     * 
     * <p>The returned collection cannot be modified.</p>
     * 
     * @return a list of aliases
     */
    Collection<String> getPrimaryAliases();

    /**
     * Get a list of all the command aliases.
     * 
     * <p>A command may have more than one alias assigned to it. The returned 
     * collection cannot be modified.</p>
     * 
     * @return a list of aliases
     */
    Collection<String> getAllAliases();

    /**
     * Get the {@link CommandCallable} associated with an alias.
     * 
     * @param alias the alias
     * @return the command mapping
     */
    CommandMapping get(String alias);

    /**
     * Returns whether the dispatcher contains a registered command for the given alias.
     * 
     * @param alias the alias
     * @return true if a registered command exists
     */
    boolean contains(String alias);

    /**
     * Execute the correct command based on the input.
     * 
     * @param arguments the arguments
     * @param locals the locals
     * @return the called command, or null if there was no command found
     * @throws CommandException thrown on a command error
     */
    CommandMapping call(String arguments, CommandLocals locals) throws CommandException;

    /**
     * Execute the correct command based on the input.
     * 
     * @param arguments the arguments
     * @param locals the locals
     * @return the called command, or null if there was no command found
     * @throws CommandException thrown on a command error
     */
    CommandMapping call(String[] arguments, CommandLocals locals) throws CommandException;

    /**
     * Get a list of suggestions based on input.
     * 
     * @param arguments the arguments entered up to this point
     * @return a list of suggestions
     * @throws CommandException thrown if there was a parsing error
     */
    Collection<String> getSuggestions(String arguments) throws CommandException;

}