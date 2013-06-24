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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;

/**
 * A block type mask that only matches blocks that are not in the list.
 * 
 * <p>This class is not data-aware and is deprecated.</p>
 * 
 * @deprecated see {@link InvertedBlockTypeMask} and {@link BlockMask}
 */
@Deprecated
public class InvertedBlockTypeMask extends BlockTypeMask {
    
    /**
     * Create a new instance with no blocks.
     */
    public InvertedBlockTypeMask() {
    }

    /**
     * Create a new instance with only one type of block.
     * 
     * @param type the type of block
     */
    public InvertedBlockTypeMask(int type) {
        super(type);
    }

    /**
     * Create a new instance with a list of type IDs.
     * 
     * @param types a list of types
     */
    public InvertedBlockTypeMask(Set<Integer> types) {
        super(types);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return !super.matches(editSession, pos);
    }

    @Override
    public String toString() {
        return String.format("InvertedBlockTypeMask(blocks=%s)", getBlocks());
    }

}
