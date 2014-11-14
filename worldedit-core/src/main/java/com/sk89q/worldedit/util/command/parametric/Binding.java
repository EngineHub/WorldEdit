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
import com.sk89q.worldedit.util.command.binding.PrimitiveBindings;
import com.sk89q.worldedit.util.command.binding.StandardBindings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Used to parse user input for a command, based on available method types 
 * and annotations.
 * 
 * <p>A binding can be used to handle several types at once. For a binding to be
 * called, it must be registered with a {@link ParametricBuilder} with
 * {@link ParametricBuilder#addBinding(Binding, java.lang.reflect.Type...)}.</p>
 * 
 * @see PrimitiveBindings an example of primitive bindings
 * @see StandardBindings standard bindings
 */
public interface Binding {
    
    /**
     * Get the types that this binding handles.
     * 
     * @return the types
     */
    Type[] getTypes();

    /**
     * Get how this binding consumes from a {@link ArgumentStack}.
     * 
     * @param parameter information about the parameter
     * @return the behavior
     */
    BindingBehavior getBehavior(ParameterData parameter);
    
    /**
     * Get the number of arguments that this binding will consume, if this
     * information is available.
     * 
     * <p>This method must return -1 for binding behavior types that are not
     * {@link BindingBehavior#CONSUMES}.</p>
     * 
     * @param parameter information about the parameter
     * @return the number of consumed arguments, or -1 if unknown or irrelevant
     */
    int getConsumedCount(ParameterData parameter);
    
    /**
     * Attempt to consume values (if required) from the given {@link ArgumentStack}
     * in order to instantiate an object for the given parameter.
     * 
     * @param parameter information about the parameter
     * @param scoped the arguments the user has input
     * @param onlyConsume true to only consume arguments
     * @return an object parsed for the given parameter
     * @throws ParameterException thrown if the parameter could not be formulated
     * @throws CommandException on a command exception
     */
    Object bind(ParameterData parameter, ArgumentStack scoped, boolean onlyConsume)
            throws ParameterException, CommandException, InvocationTargetException;

    /**
     * Get a list of suggestions for the given parameter and user arguments.
     * 
     * @param parameter information about the parameter
     * @param prefix what the user has typed so far (may be an empty string)
     * @return a list of suggestions
     */
    List<String> getSuggestions(ParameterData parameter, String prefix);

}
