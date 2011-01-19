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

/**
 * An edit session that can be set to only replace existing blocks.
 * 
 * @author sk89q
 */
public class ReplacingExistingEditSession extends EditSession {
    /**
     * True to replace existing.
     */
    private boolean replaceExisting = false;
    
    /**
     * Construct the object.
     * 
     * @param server
     * @param world
     * @param maxBlocks
     */
    public ReplacingExistingEditSession(ServerInterface server, LocalWorld world,
            int maxBlocks) {
        super(server, world, maxBlocks);
    }

    /**
     * Construct the object.
     * 
     * @param server
     * @param world
     * @param maxBlocks
     * @param blockBag
     */
    public ReplacingExistingEditSession(ServerInterface server, LocalWorld world,
            int maxBlocks, BlockBag blockBag) {
        super(server, world, maxBlocks, blockBag);
    }
    
    /**
     * Enables block replacing.
     */
    public void enableReplacing() {
        replaceExisting = true;
    }
    
    /**
     * Disables block replacing.
     */
    public void disableReplacing() {
        replaceExisting = false;
    }
    
    /**
     * Sets a block without changing history.
     * 
     * @param pt
     * @param blockType
     * @return Whether the block changed
     */
    public boolean rawSetBlock(Vector pt, BaseBlock block) {
        if (!replaceExisting) {
            return super.rawSetBlock(pt, block);
        }
        
        int y = pt.getBlockY();
        
        if (y < 0 || y > 127) {
            return false;
        }
        
        int existing = world.getBlockType(pt);
        
        if (existing == 0) {
            return false;
        }
        
        return super.rawSetBlock(pt, block);
    }

}
