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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.enginehub.command.BuilderException;
import org.enginehub.command.CommandManager;
import org.enginehub.command.Expose;
import org.enginehub.command.parametric.ParametricBuilder;
import org.enginehub.worldedit.operation.Operation;

/**
 * Runtime registry of operations.
 */
public class OperationRegistry implements Set<Class<? extends Operation>> {

    private static final Logger logger = Logger.getLogger(
            OperationRegistry.class.getCanonicalName());
    
    private final Set<Class<? extends Operation>> operations =
            new HashSet<Class<? extends Operation>>();

    private final CommandManager commands;
    private final ParametricBuilder builder;

    OperationRegistry(CommandManager commands, ParametricBuilder builder) {
        this.commands = commands;
        this.builder = builder;
    }

    /**
     * Register the commands of an operation into the command manager.
     *
     * @param clazz the class
     * @throws BuilderException thrown if a command can't be built
     */
    private void registerCommands(Class<? extends Operation> clazz) throws BuilderException {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Expose expose = constructor.getAnnotation(Expose.class);
            if (expose != null) {
                OperationCommand command = new OperationCommand(constructor);
                builder.apply(command, expose, 
                        constructor.getParameterTypes(), 
                        constructor.getParameterAnnotations());
                commands.add(command);
            }
        }
    }

    /**
     * Register an operation with WorldEdit so that it is callable from WorldEdit.
     *
     * <p>At this time, operations cannot be un-registered.</p>
     *
     * @param operation the operation
     */
    @Override
    public synchronized boolean add(Class<? extends Operation> operation) {
        if (operations.contains(operation)) {
            return false; // Already registered!
        }

        operations.add(operation);
        try {
            registerCommands(operation);
        } catch (BuilderException e) {
            logger.log(Level.SEVERE, "Failed to register the commands in the " +
            		"operation " + operation.getCanonicalName(), e);
        }

        return true;
    }

    @Override
    public synchronized int size() {
        return operations.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return operations.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return operations.contains(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return operations.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends Class<? extends Operation>> c) {
        boolean changed = false;
        for (Class<? extends Operation> clazz : c) {
            changed = add(clazz) || changed;
        }
        return changed;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot remove operations at this time");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove operations at this time");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove operations at this time");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot remove operations at this time");
    }

    /**
     * Not thread-safe.
     *
     * @return an iterator
     */
    @Override
    public synchronized Iterator<Class<? extends Operation>> iterator() {
        return operations.iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return operations.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return operations.toArray(a);
    }
}
