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

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

/**
 * Represents a world instance that can be modified. The world instance could be
 * loaded in-game or loaded in a stand-alone editor.
 * </p>
 * This class is meant to replace {@link LocalWorld} eventually, once this class has been
 * fleshed out with the required methods and it has been decided that it is time to
 * start breaking some API compatibility.
 */
public interface World {

    /**
     * Change the block at the given location to the given block. The operation may
     * not tie the given {@link Block} to the world, so future changes to the
     * {@link Block} do not affect the world until this method is called again.
     * </p>
     * Implementations may or may not consider the value of the notifyAdjacent
     * parameter, and implementations may to choose to either apply physics anyway or
     * to not apply any physics (particularly in a stand-alone implementation).
     * </p>
     * The return value of this method indicates whether the change "went through," as
     * in the block was changed in the world in any way. If the new block is no different
     * than the block already at the position in the world, 'false' would be returned.
     * If the position is invalid (out of bounds, for example), then nothing should
     * occur and 'false' should be returned. If possible, the return value should be
     * accurate as possible, but implementations may choose to not provide an accurate
     * value if it is not possible to know.
     * 
     * @param location location of the block
     * @param block block to set
     * @param notifyAdjacent true to to notify adjacent (perform physics)
     * @return true if the block was successfully set (return value may not be accurate)
     */
    boolean setBlock(Vector location, Block block, boolean notifyAdjacent);
    
    /**
     * Get a copy of the block at the given location. May return null if the location
     * given is out of bounds. The returned block must not be tied to any real block
     * in the world, so changes to the returned {@link Block} have no effect until
     * {@link #setBlock(Vector, Block, boolean)} is called.
     * 
     * @param location location of the block
     * @return the block, or null if the block does not exist
     */
    Block getBlock(Vector location);

}
