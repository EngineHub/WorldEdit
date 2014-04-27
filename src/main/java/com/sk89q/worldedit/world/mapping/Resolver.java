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

package com.sk89q.worldedit.world.mapping;

import com.sk89q.worldedit.blocks.BaseBlock;

import javax.annotation.Nullable;

/**
 * A resolver can create state objects (such as {@link BaseBlock}) from
 * universal identifiers, but also get the universal identifier for
 * a given state object (at least when it is known).
 * </p>
 * Identifiers may be case-sensitive. Routines that work with IDs should
 * not make changes to the casing of the IDs or perform case-insensitive
 * comparisons. Implementations may normalize the casing of IDs if it
 * is appropriate.
 *
 * @param <E> the type of state object
 */
public interface Resolver<E> {

    /**
     * Create an instance of the state object from the given ID.
     *
     * @param id the ID
     * @return an instance, otherwise null if an instance cannot be created
     */
    @Nullable E create(String id);

    /**
     * Get the ID for the given object.
     *
     * @param object the object
     * @return the ID, otherwise null if it is not known
     */
    @Nullable String getId(E object);

}
