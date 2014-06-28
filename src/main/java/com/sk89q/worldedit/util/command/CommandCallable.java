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
import java.util.Set;

/**
 * A command that can be executed.
 */
public interface CommandCallable {
    
    /**
     * Get a list of value flags used by this command.
     * 
     * @return a list of value flags
     */
    Set<Character> getValueFlags();

    /**
     * Execute the correct command based on the input.
     *
     * @param arguments the arguments
     * @param locals the locals
     * @return the called command, or null if there was no command found
     * @throws CommandException thrown on a command error
     */
    boolean call(String arguments, CommandLocals locals) throws CommandException;

    /**
     * Get a list of suggestions based on input.
     *
     * @param arguments the arguments entered up to this point
     * @return a list of suggestions
     * @throws CommandException thrown if there was a parsing error
     */
    Collection<String> getSuggestions(String arguments) throws CommandException;
    
    /**
     * Get an object describing this command.
     * 
     * @return the command description
     */
    Description getDescription();

}
