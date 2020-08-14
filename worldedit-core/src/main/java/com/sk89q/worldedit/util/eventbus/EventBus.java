/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.eventbus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves.
 *
 * <p>This class is based on Guava's {@link EventBus} but priority is supported
 * and events are dispatched at the time of call, rather than being queued up.
 * This does allow dispatching during an in-progress dispatch.</p>
 */
public final class EventBus {

    private final Logger logger = LoggerFactory.getLogger(EventBus.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final SetMultimap<Class<?>, EventHandler> handlersByType =
        HashMultimap.create();

    /**
     * Strategy for finding handler methods in registered objects.  Currently,
     * only the {@link AnnotatedSubscriberFinder} is supported, but this is
     * encapsulated for future expansion.
     */
    private final SubscriberFindingStrategy finder = new AnnotatedSubscriberFinder();

    private final HierarchyCache flattenHierarchyCache = new HierarchyCache();

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param clazz the event class to register
     * @param handler the handler to register
     */
    public void subscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        lock.writeLock().lock();
        try {
            handlersByType.put(clazz, handler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param handlers a map of handlers
     */
    public void subscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        lock.writeLock().lock();
        try {
            handlersByType.putAll(handlers);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Unregisters the given handler for the given class.
     *
     * @param clazz the class
     * @param handler the handler
     */
    public void unsubscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        lock.writeLock().lock();
        try {
            handlersByType.remove(clazz, handler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Unregisters the given handlers.
     *
     * @param handlers a map of handlers
     */
    public void unsubscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        lock.writeLock().lock();
        try {
            for (Map.Entry<Class<?>, Collection<EventHandler>> entry : handlers.asMap().entrySet()) {
                handlersByType.get(entry.getKey()).removeAll(entry.getValue());
            }
        } finally {
            lock.writeLock().unlock();
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
     * @param event  event to post.
     */
    public void post(Object event) {
        List<EventHandler> dispatching = new ArrayList<>();

        Set<Class<?>> dispatchTypes = flattenHierarchyCache.get(event.getClass());
        lock.readLock().lock();
        try {
            for (Class<?> eventType : dispatchTypes) {
                Set<EventHandler> wrappers = handlersByType.get(eventType);

                if (wrappers != null && !wrappers.isEmpty()) {
                    dispatching.addAll(wrappers);
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        Collections.sort(dispatching);

        for (EventHandler handler : dispatching) {
            dispatch(event, handler);
        }
    }

    /**
     * Dispatches {@code event} to the handler in {@code handler}.
     *
     * @param event  event to dispatch.
     * @param handler  handler that will call the handler.
     */
    private void dispatch(Object event, EventHandler handler) {
        try {
            handler.handleEvent(event);
        } catch (InvocationTargetException e) {
            logger.error("Could not dispatch event: " + event + " to handler " + handler, e);
        }
    }

}
