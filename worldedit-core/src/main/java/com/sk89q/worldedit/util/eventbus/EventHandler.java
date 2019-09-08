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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Event handler object for {@link EventBus} that is able to dispatch
 * an event.
 *
 * <p>Original for Guava, licensed under the Apache License, Version 2.0.</p>
 */
public abstract class EventHandler implements Comparable<EventHandler> {

    public enum Priority {
        VERY_EARLY,
        EARLY,
        NORMAL,
        LATE,
        VERY_LATE
    }

    private final Priority priority;

    /**
     * Create a new event handler.
     *
     * @param priority the priority
     */
    protected EventHandler(Priority priority) {
        checkNotNull(priority);
        this.priority = priority;
    }

    /**
     * Get the priority.
     *
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Dispatch the given event.
     *
     * <p>Subclasses should override {@link #dispatch(Object)}.</p>
     *
     * @param event the event
     * @throws InvocationTargetException thrown if an exception is thrown during dispatch
     */
    public final void handleEvent(Object event) throws InvocationTargetException {
        try {
            dispatch(event);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }

    /**
     * Dispatch the event.
     *
     * @param event the event object
     * @throws Exception an exception that may be thrown
     */
    public abstract void dispatch(Object event) throws Exception;

    @Override
    public int compareTo(EventHandler o) {
        return getPriority().ordinal() - o.getPriority().ordinal();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public String toString() {
        return "EventHandler{" +
                "priority=" + priority +
                '}';
    }

}
