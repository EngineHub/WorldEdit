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

package com.sk89q.worldedit.util.eventbus;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.eventbus.DeadEvent;
import com.sk89q.worldedit.internal.annotation.RequiresNewerGuava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves.
 *
 * <p>This class is based on Guava's {@link EventBus} but priority is supported
 * and events are dispatched at the time of call, rather than being queued up.
 * This does allow dispatching during an in-progress dispatch.</p>
 *
 * <p>This implementation utilizes naive synchronization on all getter and
 * setter methods. Dispatch does not occur when a lock has been acquired,
 * however.</p>
 */
public class EventBus {

    private final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private final SetMultimap<Class<?>, EventHandler> handlersByType =
            Multimaps.newSetMultimap(new HashMap<>(), this::newHandlerSet);

    /**
     * Strategy for finding handler methods in registered objects.  Currently,
     * only the {@link AnnotatedSubscriberFinder} is supported, but this is
     * encapsulated for future expansion.
     */
    private final SubscriberFindingStrategy finder = new AnnotatedSubscriberFinder();

    @RequiresNewerGuava
    private HierarchyCache flattenHierarchyCache = new HierarchyCache();

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param clazz the event class to register
     * @param handler the handler to register
     */
    public synchronized void subscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        handlersByType.put(clazz, handler);
    }

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param handlers a map of handlers
     */
    public synchronized void subscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        handlersByType.putAll(handlers);
    }

    /**
     * Unregisters the given handler for the given class.
     *
     * @param clazz the class
     * @param handler the handler
     */
    public synchronized void unsubscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        handlersByType.remove(clazz, handler);
    }

    /**
     * Unregisters the given handlers.
     *
     * @param handlers a map of handlers
     */
    public synchronized void unsubscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        for (Map.Entry<Class<?>, Collection<EventHandler>> entry : handlers.asMap().entrySet()) {
            Set<EventHandler> currentHandlers = getHandlersForEventType(entry.getKey());
            Collection<EventHandler> eventMethodsInListener = entry.getValue();

            if (currentHandlers != null &&!currentHandlers.containsAll(entry.getValue())) {
                currentHandlers.removeAll(eventMethodsInListener);
            }
        }
    }

    /**
     * Registers all handler methods on {@code object} to receive events.
     * Handler methods are selected and classified using this EventBus's
     * {@link SubscriberFindingStrategy}; the default strategy is the
     * {@link AnnotatedSubscriberFinder}.
     *
     * @param object object whose handler methods should be registered.
     */
    public void register(Object object) {
        subscribeAll(finder.findAllSubscribers(object));
    }

    /**
     * Unregisters all handler methods on a registered {@code object}.
     *
     * @param object  object whose handler methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     */
    public void unregister(Object object) {
        unsubscribeAll(finder.findAllSubscribers(object));
    }

    /**
     * Posts an event to all registered handlers.  This method will return
     * successfully after the event has been posted to all handlers, and
     * regardless of any exceptions thrown by handlers.
     *
     * <p>If no handlers have been subscribed for {@code event}'s class, and
     * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
     * DeadEvent and reposted.
     *
     * @param event  event to post.
     */
    public void post(Object event) {
        List<EventHandler> dispatching = new ArrayList<>();

        synchronized (this) {
            Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());

            for (Class<?> eventType : dispatchTypes) {
                Set<EventHandler> wrappers = getHandlersForEventType(eventType);

                if (wrappers != null && !wrappers.isEmpty()) {
                    dispatching.addAll(wrappers);
                }
            }
        }

        Collections.sort(dispatching);

        for (EventHandler handler : dispatching) {
            dispatch(event, handler);
        }
    }

    /**
     * Dispatches {@code event} to the handler in {@code handler}.  This method
     * is an appropriate override point for subclasses that wish to make
     * event delivery asynchronous.
     *
     * @param event  event to dispatch.
     * @param handler  handler that will call the handler.
     */
    protected void dispatch(Object event, EventHandler handler) {
        try {
            handler.handleEvent(event);
        } catch (InvocationTargetException e) {
            logger.error("Could not dispatch event: " + event + " to handler " + handler, e);
        }
    }

    /**
     * Retrieves a mutable set of the currently registered handlers for
     * {@code type}.  If no handlers are currently registered for {@code type},
     * this method may either return {@code null} or an empty set.
     *
     * @param type  type of handlers to retrieve.
     * @return currently registered handlers, or {@code null}.
     */
    synchronized Set<EventHandler> getHandlersForEventType(Class<?> type) {
        return handlersByType.get(type);
    }

    /**
     * Creates a new Set for insertion into the handler map.  This is provided
     * as an override point for subclasses. The returned set should support
     * concurrent access.
     *
     * @return a new, mutable set for handlers.
     */
    protected synchronized Set<EventHandler> newHandlerSet() {
        return new HashSet<>();
    }

    /**
     * Flattens a class's type hierarchy into a set of Class objects.  The set
     * will include all superclasses (transitively), and all interfaces
     * implemented by these superclasses.
     *
     * @param concreteClass  class whose type hierarchy will be retrieved.
     * @return {@code clazz}'s complete type hierarchy, flattened and uniqued.
     */
    Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        return flattenHierarchyCache.get(concreteClass);
    }

}
