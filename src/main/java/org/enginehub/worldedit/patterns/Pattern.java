// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.foundation.Block;

/**
 * A factory for the next block type to use when placing the block.
 */
public interface Pattern {
    
    /**
     * Get a block for a position. This return value of this method does
     * not have to be consistent for the same position.
     * 
     * @param position the position of the block
     * @return the block
     */
    public Block next(Vector position);
    
}
