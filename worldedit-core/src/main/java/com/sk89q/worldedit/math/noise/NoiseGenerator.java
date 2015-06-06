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

package com.sk89q.worldedit.math.noise;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 * Generates noise in a deterministic or non-deterministic manner.
 */
public interface NoiseGenerator {

    /**
     * Get the noise value for the given position. The returned value may
     * change on every future call for the same position.
     *
     * @param position the position
     * @return a noise value between 0 (inclusive) and 1 (inclusive)
     */
    float noise(Vector2D position);

    /**
     * Get the noise value for the given position. The returned value may
     * change on every future call for the same position.
     *
     * @param position the position
     * @return a noise value between 0 (inclusive) and 1 (inclusive)
     */
    float noise(Vector position);

}
