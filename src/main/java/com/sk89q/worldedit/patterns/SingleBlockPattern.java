// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Always returns the same block type.
 *
 * @author sk89q
 */
public class SingleBlockPattern implements Pattern {
    /**
     * Block type.
     */
    private BaseBlock block;

    /**
     * Construct the object.
     *
     * @param block
     */
    public SingleBlockPattern(BaseBlock block) {
        this.block = block;
    }

    /**
     * Get next block.
     *
     * @param pos
     * @return
     */
    public BaseBlock next(Vector pos) {
        return block;
    }

    /**
     * Get the block.
     *
     * @return
     */
    public BaseBlock getBlock() {
        return block;
    }
}
