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

package org.enginehub.worldedit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.enginehub.command.CommandException;
import org.enginehub.command.ExecutionException;
import org.enginehub.command.parametric.ParametricCommand;
import org.enginehub.worldedit.operation.Operation;
import org.enginehub.worldedit.operation.OperationHelper;


/**
 * Wraps commands exposed via {@link Operation}s.
 */
class OperationCommand extends ParametricCommand {
    
    private final Constructor<Operation> constructor;
    
    /**
     * Construct the operation command wrapper.
     * 
     * @param constructor the constructor
     */
    @SuppressWarnings("unchecked")
    public OperationCommand(Constructor<?> constructor) {
        this.constructor = (Constructor<Operation>) constructor;
    }

    @Override
    protected void execute(List<Object> arguments) throws CommandException {
        try {
            Operation op = constructor.newInstance(arguments.toArray());
            OperationHelper.completeLegacy(op); // @TODO: More complex operations
        } catch (InstantiationException e) {
            throw new ExecutionException("Failure constructing the operation", e);
        } catch (IllegalAccessException e) {
            throw new ExecutionException("Failure constructing the operation", e);
        } catch (IllegalArgumentException e) {
            throw new ExecutionException("Failure constructing the operation", e);
        } catch (InvocationTargetException e) {
            throw new ExecutionException("Failure constructing the operation", e);
        }
    }

}
