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

package com.sk89q.rebar.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.rebar.util.Owner;

/**
 * A global event handler.
 */
public final class EventSystem implements EventDispatcher {
    
    private final static Logger logger = 
            Logger.getLogger(EventSystem.class.getCanonicalName());
    private final static EventSystem instance;
    
    static {
        instance = new EventSystem();
    }
    
    /**
     * Get an instance to the event system.
     * 
     * @return the event system
     */
    public static EventSystem getInstance() {
        return instance;
    }
    
    /* ---- Non-static below ---- */

    /**
     * Private constructor.
     */
    private EventSystem() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event dispatch(Event event) {
        event.getExecutors().execute(event); // Thread-safe
        return event;
    }

    /**
     * Dispatch an event, and then returns the {@link Cancellable} status.
     * 
     * @param event the event
     * @return true the value of the event's {@link Cancellable#isCancelled()}
     */
    public <E extends Event & Cancellable> boolean testDispatch(E event) {
        dispatch(event);
        return event.isCancelled();
    }
    
    /**
     * Register an un-owned event executor.
     * 
     * <p>It is extremely recommended that you prefer 
     * {@link #registerHandler(Class, EventExecutor, Owner)} instead.</p>
     * 
     * @param clazz the event class
     * @param executor the executor
     * @see EventSystem#registerHandler(Class, EventExecutor, Owner)
     */
    @SuppressWarnings("unchecked")
    public void registerHandler(Class<? extends Event> clazz, EventExecutor<?> executor) {
        getExecutors(clazz).add(executor); // Thread-safe
    }
    
    /**
     * Register an owned event executor.
     * 
     * <p>If the given owner parameter is null, 
     * {@link #registerHandler(Class, EventExecutor)}
     * will be called (which does not wrap the {@link EventExecutor} in an
     * {@link OwnedExecutor}).</p>
     * 
     * @param clazz the event class
     * @param executor the executor
     * @param owner the owner of the executor
     */
    public <T extends Event> void registerHandler(Class<? extends Event> clazz,
            EventExecutor<T> executor, Owner owner) {
        if (owner != null) {
            registerHandler(clazz, new OwnedExecutor<T>(executor, owner));
        } else {
            registerHandler(clazz, executor);
        }
    }

    /**
     * Extracts {@link EventExecutor}s from methods annotated with {@link EventHandler}.
     * 
     * <p>This method will set the owner of added {@link EventExecutor} to be that of
     * the provided object.</p>
     * 
     * @param object the object to extract from
     */
    public void registerListener(Owner object) {
        registerListener(object, object);
    }

    /**
     * Extracts {@link EventExecutor}s from methods annotated with {@link EventHandler}.
     * 
     * <p>Static methods are skipped.</p>
     * 
     * @param object the object to extract from
     * @param owner the owner
     */
    @SuppressWarnings("unchecked")
    public void registerListener(Object object, Owner owner) {
        Method[] methods = object.getClass().getMethods();
        
        for (Method method : methods) {
            // Static methods are skipped
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            
            try {
                EventHandler handler = method.getAnnotation(EventHandler.class);
                if (handler != null) {
                    Class<?>[] types = method.getParameterTypes();
                    
                    if (types.length != 1) {
                        throw new RuntimeException(
                                "@Handler method can only have one parameter");
                    }
                    
                    if (!Event.class.isAssignableFrom(types[0])) {
                        throw new RuntimeException("@Handler method expected first arg to " +
                                "be subclass of Event");
                    }
                    
                    EventExecutor<?> executor = 
                            new MethodExecutor<Event>(object, method, handler.priority());
                    registerHandler((Class<? extends Event>) types[0], executor, owner);
                }
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to register the @EventHandler of " +
                        object.getClass() + " [" + method.toGenericString() + "]", e);
            }
        }
    }

    /**
     * Extracts {@link EventExecutor}s from methods annotated with {@link EventHandler}
     * and remove listeners owned by the given owner.
     * 
     * <p>Static methods are skipped.</p>
     * 
     * @param object the object to extract from
     * @param owner the owner
     */
    @SuppressWarnings("unchecked")
    public void removeListener(Object object, Owner owner) {
        Method[] methods = object.getClass().getMethods();
        
        for (Method method : methods) {
            // Static methods are skipped
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            
            try {
                EventHandler handler = method.getAnnotation(EventHandler.class);
                if (handler != null) {
                    Class<?>[] types = method.getParameterTypes();
                    removeHandler((Class<? extends Event>) types[0], owner);
                }
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "Failed to remove the @EventHandler of " +
                        object.getClass() + " [" + method.toGenericString() + "]", e);
            }
        }
    }
    
    /**
     * Remove an event executor on an event.
     * 
     * @param clazz the event class
     * @param executor the executor
     */
    public void removeHandler(Class<? extends Event> clazz, EventExecutor<?> executor) {
        getExecutors(clazz).remove(executor);
    }
    
    /**
     * Remove all event executors of an event owned by a given {@link Owner}.
     * 
     * @param clazz the event class
     * @param owner the owner of the executor (cannot be null)
     */
    public void removeHandler(Class<? extends Event> clazz, Owner owner) {
        getExecutors(clazz).removeOwned(owner);
    }

    /**
     * Creates an instance of an event class.
     * 
     * @param clazz the class
     * @return the event
     * @throws RuntimeException if the instance couldn't be created
     */
    @SuppressWarnings({ "rawtypes" })
    private static ExecutorList getExecutors(Class<? extends Event> clazz)  {
        try {
            Method method = clazz.getDeclaredMethod("staticExecutors");
            method.setAccessible(true);
            return (ExecutorList<?>) method.invoke(null);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to call staticExecutors() on "
                    + clazz.getCanonicalName(), e);
        }
    }

}
