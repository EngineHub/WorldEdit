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

package com.sk89q.worldedit.masks;

import java.util.Set;

import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * A filter that matches blocks based on block types.
 *
 * @deprecated replaced by {@link BlockMask} 
 */
@Deprecated
public class BlockTypeMask extends BlockMask {

    /**
     * Create a new instance with no blocks in the list.
     */
    public BlockTypeMask() {
    }

    /**
     * Create a new instance with one block type.
     * 
     * @param type the block type
     */
    public BlockTypeMask(int type) {
        this();
        add(type);
    }

    /**
     * Create a new instance with a list of blocks
     * 
     * @param types a list of block type IDs
     */
    public BlockTypeMask(Set<Integer> types) {
        super();
        for (int type : types) {
            add(type);
        }
    }

    /**
     * Add a block type to the list.
     * 
     * @param type the type
     */
    public void add(int type) {
        add(new BaseBlock(type));
    }
    
}
