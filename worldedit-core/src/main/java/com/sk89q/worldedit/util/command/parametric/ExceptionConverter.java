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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

/**
 * Used to convert a recognized {@link Throwable} into an appropriate 
 * {@link CommandException}.
 * 
 * <p>Methods (when invoked by a {@link ParametricBuilder}-created command) may throw
 * relevant exceptions that are not caught by the command manager, but translate
 * into reasonable exceptions for an application. However, unknown exceptions are
 * normally simply wrapped in a {@link WrappedCommandException} and bubbled up. Only
 * normal {@link CommandException}s will be printed correctly, so a converter translates
 * one of these unknown exceptions into an appropriate {@link CommandException}.</p>
 * 
 * <p>This also allows the code calling the command to not need be aware of these
 * application-specific exceptions, as they will all be converted to
 * {@link CommandException}s that are handled normally.</p>
 */
public interface ExceptionConverter {
    
    /**
     * Attempt to convert the given throwable into a {@link CommandException}.
     * 
     * <p>If the exception is not recognized, then nothing should be thrown.</p>
     * 
     * @param t the throwable
     * @throws CommandException a command exception
     */
    void convert(Throwable t) throws CommandException;

}
