// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Patterns return a block to use for a given position.
 */
public interface Pattern {
    
    /**
     * Get a block for a position.
     * 
     * <p>This return value of this method does not have to be consistent for the
     * same position.</p>
     *
     * @param position the position of the block
     * @return a block
     */
    public BaseBlock next(Vector position);

    /**
     * Get a block for a position.
     * 
     * <p>This return value of this method does not have to be consistent for the
     * same position.</p>
     *
     * @param x the X position
     * @param y the Y position
     * @param z the Z position
     * @return a block
     */
    public BaseBlock next(int x, int y, int z);
    
}
