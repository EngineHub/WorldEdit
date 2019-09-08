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

package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.Vector3;

/**
 * Makes a transformation of {@link Vector3}s.
 */
public interface Transform {

    /**
     * Return whether this transform is an identity.
     *
     * <p>If it is not known, then {@code false} must be returned.</p>
     *
     * @return true if identity
     */
    boolean isIdentity();

    /**
     * Returns the result of applying the function to the input.
     *
     * @param input the input
     * @return the result
     */
    Vector3 apply(Vector3 input);

    /**
     * Create a new inverse transform.
     *
     * @return a new inverse transform
     */
    Transform inverse();

    /**
     * Create a new {@link Transform} that combines this transform with another.
     *
     * @param other the other transform to occur second
     * @return a new transform
     */
    Transform combine(Transform other);

}
