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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.util.command.MissingParameterException;
import com.sk89q.worldedit.util.command.SimpleDescription;
import com.sk89q.worldedit.util.command.UnconsumedParameterException;

import java.lang.reflect.Method;

/**
 * Handles legacy properties on {@link Command} such as {@link Command#min()} and
 * {@link Command#max()}.
 */
public class LegacyCommandsHandler extends AbstractInvokeListener implements InvokeHandler {

    @Override
    public InvokeHandler createInvokeHandler() {
        return this;
    }

    @Override
    public void preProcess(Object object, Method method,
            ParameterData[] parameters, CommandContext context) 
                    throws CommandException, ParameterException {
    }

    @Override
    public void preInvoke(Object object, Method method,
            ParameterData[] parameters, Object[] args, CommandContext context)
            throws ParameterException {
        Command annotation = method.getAnnotation(Command.class);
        
        if (annotation != null) {
            if (context.argsLength() < annotation.min()) {
                throw new MissingParameterException();
            }
    
            if (annotation.max() != -1 && context.argsLength() > annotation.max()) {
                throw new UnconsumedParameterException(
                        context.getRemainingString(annotation.max()));
            }
        }
    }

    @Override
    public void postInvoke(Object object, Method method,
            ParameterData[] parameters, Object[] args, CommandContext context) {

    }

    @Override
    public void updateDescription(Object object, Method method,
            ParameterData[] parameters, SimpleDescription description) {
        Command annotation = method.getAnnotation(Command.class);
        
        // Handle the case for old commands where no usage is set and all of its
        // parameters are provider bindings, so its usage information would
        // be blank and would imply that there were no accepted parameters
        if (annotation != null && annotation.usage().isEmpty() 
                && (annotation.min() > 0 || annotation.max() > 0)) {
            boolean hasUserParameters = false;
            
            for (ParameterData parameter : parameters) {
                if (parameter.getBinding().getBehavior(parameter) != BindingBehavior.PROVIDES) {
                    hasUserParameters = true;
                    break;
                }
            }
            
            if (!hasUserParameters) {
                description.overrideUsage("(unknown usage information)");
            }
        }
    }

}
