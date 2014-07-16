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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.entity.Entity;

import javax.annotation.Nullable;

/**
 * Indicates that an object can provide various "facets," which are
 * specific distinct interfaces that can represent a portion of the object.
 *
 * <p>For example, an instance of an {@link Entity} may have a facet
 * for accessing its inventory (if it contains an inventory) or a facet
 * for accessing its health (if it has health).</p>
 *
 * <p>Facets are referred to by their interface or abstract class and
 * it is dependent on the implementation of the object specifying this
 * interface to return the most applicable implementation. However, in
 * many cases, such an implementation may not apply or it has not been
 * implemented so a request for a facet may return {@code null}.</p>
 */
public interface Faceted {

    /**
     * Get the facet corresponding to the given class or interface.
     *
     * @param cls the class or interface
     * @param <T> the type
     * @return an implementation of the facet or {@code null} if one is unavailable
     */
    @Nullable
    <T> T getFacet(Class<? extends T> cls);

}
