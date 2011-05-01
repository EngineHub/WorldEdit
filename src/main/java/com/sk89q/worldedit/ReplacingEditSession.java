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

package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.masks.Mask;

/**
 * An edit session that will only replace blocks as specified.
 * 
 * @author sk89q
 */
public class ReplacingEditSession extends EditSession {
    /**
     * Filter to use to filter blocks.
     */
    private Mask mask;
    
    /**
     * Construct the object.
     * 
     * @param world
     * @param maxBlocks
     * @param mask 
     */
    public ReplacingEditSession(LocalWorld world,
            int maxBlocks, Mask mask) {
        super(world, maxBlocks);
        this.mask = mask;
    }

    /**
     * Construct the object.
     * 
     * @param world
     * @param maxBlocks
     * @param blockBag
     * @param mask 
     */
    public ReplacingEditSession(LocalWorld world, int maxBlocks,
            BlockBag blockBag, Mask mask) {
        super(world, maxBlocks, blockBag);
        this.mask = mask;
    }
    
    /**
     * Sets a block without changing history.
     * 
     * @param pt
     * @param block
     * @return Whether the block changed
     */
    @Override
    public boolean rawSetBlock(Vector pt, BaseBlock block) {        
        int y = pt.getBlockY();
        
        if (y < 0 || y > 127) {
            return false;
        }
        
        if (!mask.matches(this, pt)) {
            return false;
        }
        
        return super.rawSetBlock(pt, block);
    }

}
