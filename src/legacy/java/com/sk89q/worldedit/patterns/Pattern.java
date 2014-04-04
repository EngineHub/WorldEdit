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

package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * @deprecated See {@link com.sk89q.worldedit.function.pattern.Pattern}
 */
@Deprecated
public interface Pattern {
    /**
     * Get a block for a position. This return value of this method does
     * not have to be consistent for the same position.
     *
     * @param pos
     * @return
     */
    public BaseBlock next(Vector pos);

    /**
     * Get a block for a position. This return value of this method does
     * not have to be consistent for the same position.
     *
     * @param pos
     * @return
     */
    public BaseBlock next(int x, int y, int z);
}
