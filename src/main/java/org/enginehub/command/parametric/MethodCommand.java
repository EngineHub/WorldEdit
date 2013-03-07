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

package org.enginehub.command.parametric;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.enginehub.command.CommandException;
import org.enginehub.command.ExecutionException;

/**
 * An implementation of {@link ParametricCommand} that calls methods.
 */
public class MethodCommand extends ParametricCommand {

    private final Object object;
    private final Method method;

    public MethodCommand(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    @Override
    protected void execute(List<Object> arguments) throws CommandException {
        try {
            method.invoke(object, arguments.toArray());
        } catch (IllegalAccessException e) {
            throw new ExecutionException("Failure invoking the command's method", e);
        } catch (InvocationTargetException e) {
            throw new ExecutionException("Failure invoking the command's method", e);
        }
    }

}
