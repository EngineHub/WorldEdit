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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.enginehub.util.Owner;

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
    public void dispatch(Event event) {
        event.getExecutors().execute(event); // Thread-safe
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
     * {@link #register(Event, EventExecutor, Owner)} instead.</p>
     * 
     * @param event the event
     * @param executor the executor
     * @see EventSystem#register(Event, EventExecutor, Owner)
     */
    @SuppressWarnings("unchecked")
    public void register(Event event, EventExecutor<?> executor) {
        event.getExecutors().add(executor); // Thread-safe
    }
    
    /**
     * Register an owned event executor.
     * 
     * <p>If the given owner parameter is null, {@link #register(Event, EventExecutor)}
     * will be called (which does not wrap the {@link EventExecutor} in an
     * {@link OwnedExecutor}).</p>
     * 
     * @param event the event
     * @param executor the executor
     * @param owner the owner of the executor
     */
    public <T extends Event> void register(Event event, EventExecutor<T> executor, Owner owner) {
        if (owner != null) {
            register(event, new OwnedExecutor<T>(executor, owner));
        } else {
            register(event, executor);
        }
    }

    /**
     * Extracts {@link EventExecutor}s from methods annotated with {@link Handler}.
     * 
     * <p>This method will set the owner of added {@link EventExecutor} to be that of
     * the provided object.</p>
     * 
     * @param object the object to extract from
     */
    public void register(Owner object) {
        register(object, object);
    }

    /**
     * Extracts {@link EventExecutor}s from methods annotated with {@link Handler}.
     * 
     * <p>Static methods are skipped.</p>
     * 
     * @param object the object to extract from
     * @param owner the owner
     */
    @SuppressWarnings("unchecked")
    public void register(Object object, Owner owner) {
        Method[] methods = object.getClass().getMethods();
        
        for (Method method : methods) {
            // Static methods are skipped
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            
            try {
                Handler handler = method.getAnnotation(Handler.class);
                if (handler != null) {
                    Class<?>[] types = method.getParameterTypes();
                    
                    if (types.length != 1) {
                        throw new EventException(
                                "@Handler method can only have one parameter");
                    }
                    
                    if (!Event.class.isAssignableFrom(types[0])) {
                        throw new EventException("@Handler method expected first arg to " +
                                "be subclass of Event");
                    }
                    
                    Event event = createEvent((Class<? extends Event>) types[0]);
                    EventExecutor<?> executor = 
                            new MethodExecutor<Event>(object, method, handler.priority());
                    register(event, executor, owner);
                }
            } catch (EventException e) {
                logger.log(Level.WARNING, "Failed to register the  @Handler of " +
                        object.getClass() + " [" + method.toGenericString() + "]", e);
            }
        }
    }
    
    /**
     * Remove an event executor on an event.
     * 
     * @param event the event
     * @param executor the executor
     */
    public void remove(Event event, EventExecutor<?> executor) {
        event.getExecutors().remove(executor);
    }
    
    /**
     * Remove an event executor on an event.
     * 
     * @param event the event
     * @param owner the owner of the executor (cannot be null)
     */
    public void remove(Event event, Owner owner) {
        event.getExecutors().removeOwned(owner);
    }

    /**
     * Creates an instance of an event class.
     * 
     * @param clazz the class
     * @return the event
     * @throws EventException if the instance couldn't be created
     */
    private static Event createEvent(Class<? extends Event> clazz) throws EventException {
        try {
            Constructor<? extends Event> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        } catch (IllegalAccessException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        } catch (SecurityException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        } catch (NoSuchMethodException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        } catch (IllegalArgumentException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        } catch (InvocationTargetException e) {
            throw new EventException("Called no-arg constructor, couldn't create", e);
        }
    }

}
