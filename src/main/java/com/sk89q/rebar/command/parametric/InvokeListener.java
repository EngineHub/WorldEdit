// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.rebar.command.parametric;

import java.lang.reflect.Method;

import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.command.CommandCallable;
import com.sk89q.rebar.command.SimpleDescription;

/**
 * Listens to events related to {@link ParametricBuilder}.
 */
public interface InvokeListener {
    
    /**
     * Create a new invocation handler.
     * 
     * <p>An example use of an {@link InvokeHandler} would be to verify permissions
     * added by the {@link CommandPermissions} annotation.</p>
     * 
     * <p>For simple {@link InvokeHandler}, an object can implement both this
     * interface and {@link InvokeHandler}.</p>
     * 
     * @return a new invocation handler
     */
    InvokeHandler createInvokeHandler();

    /**
     * During creation of a {@link CommandCallable} by a {@link ParametricBuilder},
     * this will be called in case the description needs to be updated.
     * 
     * @param object the object
     * @param method the method
     * @param parameters a list of parameters
     * @param description the description to be updated
     */
    void updateDescription(Object object, Method method, ParameterData[] parameters,
            SimpleDescription description);

}
