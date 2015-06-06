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
import com.sk89q.minecraft.util.commands.CommandException;

import java.lang.reflect.Method;

/**
 * Called before and after a command is invoked for commands executed by a command
 * created using {@link ParametricBuilder}.
 * 
 * <p>Invocation handlers are created by {@link InvokeListener}s. Multiple
 * listeners and handlers can be registered, and all be run. However, if one handler
 * throws an exception, future handlers will not execute and the command will
 * not execute (if thrown in 
 * {@link #preInvoke(Object, Method, ParameterData[], Object[], CommandContext)}).</p>
 * 
 * @see InvokeListener the factory
 */
public interface InvokeHandler {

    /**
     * Called before parameters are processed.
     * 
     * @param object the object
     * @param method the method
     * @param parameters the list of parameters
     * @param context the context
     * @throws CommandException can be thrown for an error, which will stop invocation
     * @throws ParameterException on parameter error
     */
    void preProcess(Object object, Method method, ParameterData[] parameters, 
            CommandContext context) throws CommandException, ParameterException;

    /**
     * Called before the parameter is invoked.
     * 
     * @param object the object
     * @param method the method
     * @param parameters the list of parameters
     * @param args the arguments to be given to the method
     * @param context the context
     * @throws CommandException can be thrown for an error, which will stop invocation
     * @throws ParameterException on parameter error
     */
    void preInvoke(Object object, Method method, ParameterData[] parameters,
            Object[] args, CommandContext context) throws CommandException, ParameterException;

    /**
     * Called after the parameter is invoked.
     * 
     * @param object the object
     * @param method the method
     * @param parameters the list of parameters
     * @param args the arguments to be given to the method
     * @param context the context
     * @throws CommandException can be thrown for an error
     * @throws ParameterException on parameter error
     */
    void postInvoke(Object object, Method method, ParameterData[] parameters,
            Object[] args, CommandContext context) throws CommandException, ParameterException;

}
