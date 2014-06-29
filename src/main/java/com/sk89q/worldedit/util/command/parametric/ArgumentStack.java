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

package com.sk89q.worldedit.util.command.parametric;

import com.sk89q.minecraft.util.commands.CommandContext;

public interface ArgumentStack {

    /**
     * Get the next string, which may come from the stack or a value flag.
     * 
     * @return the value
     * @throws ParameterException on a parameter error
     */
    String next() throws ParameterException;

    /**
     * Get the next integer, which may come from the stack or a value flag.
     * 
     * @return the value
     * @throws ParameterException on a parameter error
     */
    Integer nextInt() throws ParameterException;

    /**
     * Get the next double, which may come from the stack or a value flag.
     * 
     * @return the value
     * @throws ParameterException on a parameter error
     */
    Double nextDouble() throws ParameterException;

    /**
     * Get the next boolean, which may come from the stack or a value flag.
     * 
     * @return the value
     * @throws ParameterException on a parameter error
     */
    Boolean nextBoolean() throws ParameterException;

    /**
     * Get all remaining string values, which will consume the rest of the stack.
     * 
     * @return the value
     * @throws ParameterException on a parameter error
     */
    String remaining() throws ParameterException;

    /**
     * Set as completely consumed.
     */
    void markConsumed();

    /**
     * Get the underlying context.
     * 
     * @return the context
     */
    CommandContext getContext();

}