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

package com.sk89q.worldedit.foundation;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * A world, portion of a world, clipboard, or other object that can have blocks, entities
 * and other game objects inspected or set.
 */
public interface Extent extends MutableExtent {
    
    /**
     * Get a copy of the block at the given location. May return null if the location
     * given is out of bounds. The returned block must not be tied to any real block
     * in the world, so changes to the returned {@link Block} have no effect until
     * {@link #setBlock(Vector, BaseBlock)} is called.
     * 
     * @param location location of the block
     * @return the block, or null if the block does not exist
     */
    BaseBlock getBlock(Vector location);
    
    /**
     * Get the block ID at the given location.
     * 
     * @param location location of the block
     * @return the block ID
     */
    int getBlockType(Vector location);

    /**
     * Get the data value of the block at the given location.
     * 
     * @param location the location of the block
     * @return the block data value
     */
    int getBlockData(Vector location);
    
}
