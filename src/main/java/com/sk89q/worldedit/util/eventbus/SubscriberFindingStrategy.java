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

/**
 * A method for finding event handler methods in objects, for use by
 * {@link EventBus}.
 */
interface SubscriberFindingStrategy {

    /**
     * Finds all suitable event handler methods in {@code source}, organizes them
     * by the type of event they handle, and wraps them in {@link EventHandler}s.
     *
     * @param source  object whose handlers are desired.
     * @return EventHandler objects for each handler method, organized by event
     *         type.
     *
     * @throws IllegalArgumentException if {@code source} is not appropriate for
     *         this strategy (in ways that this interface does not define).
     */
    Multimap<Class<?>, EventHandler> findAllSubscribers(Object source);

}