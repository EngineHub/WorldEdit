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

package com.sk89q.worldedit.operations;

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.Region;

/**
 * Remove snow from a given area. Only skylight-exposed snow is removed.
 */
public class ThawArea extends AbstractOperation implements BlockChange {
    
    private final EditSession context;
    private final Region region;

    private int affected = 0;
    
    /**
     * Create a thawing operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     */
    public ThawArea(EditSession context, Region region) {
        this.context = context;
        this.region = region;
    }

    @Override
    protected Operation resume() throws MaxChangedBlocksException {
        BaseBlock air = new BaseBlock(0);
        BaseBlock water = new BaseBlock(BlockID.STATIONARY_WATER);

        Iterator<BlockVector> points = region.columnIterator();
        int maxY = region.getMaximumPoint().getBlockY();
        int minY = region.getMinimumPoint().getBlockY();
        
        while (points.hasNext()) {
            Vector columnPt = points.next();

            for (int y = maxY; y >= minY; --y) {
                Vector pt = columnPt.setY(y);
                int id = context.getBlockType(pt);

                switch (id) {
                case BlockID.ICE:
                    if (context.setBlock(pt, water)) {
                        ++affected;
                    }
                    break;

                case BlockID.SNOW:
                    if (context.setBlock(pt, air)) {
                        ++affected;
                    }
                    break;

                case BlockID.AIR:
                    continue;

                default:
                    break;
                }

                break;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }

    @Override
    public int getBlocksChanged() {
        return affected;
    }

}
