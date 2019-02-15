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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * A brush is a long-range build tool.
 */
public interface Brush {

    /**
     * Build the object.
     * 
     * @param editSession the {@code EditSession}
     * @param position the position
     * @param pattern the pattern
     * @param size the size of the brush
     * @throws MaxChangedBlocksException 
     */
    public void build(EditSession editSession, Vector position, Pattern pattern, double size) throws MaxChangedBlocksException;

    /**
     * Get the region that represents the area that would be affected if called at the given position.
     *
     * @param session the editsession to build in
     * @param position the brush target
     * @param size the brush size
     * @return the bounds of the affected area
     */
    default public Region getBounds(EditSession session, Vector position, double size) {
        return CuboidRegion.fromCenter(position, (int) Math.ceil(size));
    }
}
