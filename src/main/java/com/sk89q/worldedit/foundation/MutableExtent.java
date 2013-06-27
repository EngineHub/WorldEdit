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
 * A world, portion of a world, clipboard, or other object that can have blocks set or
 * entities placed.
 */
public interface MutableExtent {
    
    /**
     * Change the block at the given location to the given block. The operation may
     * not tie the given {@link Block} to the world, so future changes to the
     * {@link Block} do not affect the world until this method is called again.
     * 
     * <p>The return value of this method indicates whether the change "went through," as
     * in the block was changed in the world in any way. If the new block is no different
     * than the block already at the position in the world, 'false' would be returned.
     * If the position is invalid (out of bounds, for example), then nothing should
     * occur and 'false' should be returned. If possible, the return value should be
     * accurate as possible, but implementations may choose to not provide an accurate
     * value if it is not possible to know.</p>
     * 
     * @param location location of the block
     * @param block block to set
     * @return true if the block was successfully set (return value may not be accurate)
     */
    boolean setBlock(Vector location, BaseBlock block);

}
