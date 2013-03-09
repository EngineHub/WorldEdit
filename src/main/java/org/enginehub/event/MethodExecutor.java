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

package org.enginehub.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Executes a method with the first parameter as the event object.
 *
 * @param <T> the event type
 */
public class MethodExecutor<T extends Event> implements EventExecutor<T> {
    
    private final Method method;
    private final Object object;
    private final int priority;
    private boolean ignoringCancelled = false;

    /**
     * Create a new instance with a given object and corresponding method.
     * 
     * @param object the object
     * @param method the object's method
     */
    public MethodExecutor(Object object, Method method) {
        this.object = object;
        this.method = method;
        this.priority = Priority.DEFAULT;
    }

    /**
     * Create a new instance with a given object and corresponding method.
     * 
     * @param object the object
     * @param method the object's method
     * @param priority the priority (see {@link Priority}).
     */
    public MethodExecutor(Object object, Method method, int priority) {
        this.object = object;
        this.method = method;
        this.priority = priority;
    }

    /**
     * Get the method that is called.
     * 
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the method that the object is called on.
     * 
     * @return the object
     */
    public Object getObject() {
        return object;
    }
    
    @Override
    public boolean isIgnoringCancelled() {
        return ignoringCancelled;
    }

    /**
     * Set whether cancelled events are skipped.
     * 
     * @param ignoringCancelled true if cancelled events are skipped
     */
    public void setIgnoringCancelled(boolean ignoringCancelled) {
        this.ignoringCancelled = ignoringCancelled;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void execute(T event) throws EventException {
        try {
            method.invoke(object, event);
        } catch (IllegalAccessException e) {
            throw new EventException("Failed to execute event method", e);
        } catch (IllegalArgumentException e) {
            throw new EventException("Failed to execute event method", e);
        } catch (InvocationTargetException e) {
            throw new EventException("Failed to execute event method", e);
        }
    }

}
