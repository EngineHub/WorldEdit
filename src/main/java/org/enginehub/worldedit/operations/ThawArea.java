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

package org.enginehub.worldedit.operations;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.Region;

/**
 * Remove snow and ice from a given area. Only the topmost non-air blocks in the
 * region are checked, so a region with a "roof" will be unaffected.
 */
public class ThawArea extends ColumnVisitor implements ChangeCountable {

    private final BaseBlock air = new BaseBlock(0);
    private final BaseBlock water = new BaseBlock(BlockID.STATIONARY_WATER);
    
    private final EditSession context;
    private final int minY;
    private final int maxY;

    private int affected = 0;
    
    /**
     * Create a thawing operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     */
    public ThawArea(EditSession context, Region region) {
        super(region);
        
        this.context = context;

        maxY = region.getMaximumPoint().getBlockY();
        minY = region.getMinimumPoint().getBlockY();
    }

    @Override
    public void visitColumn(Execution opt, Vector columnPt) throws WorldEditException {
        outer:
        for (int y = maxY; y >= minY; --y) {
            Vector pt = columnPt.setY(y);
            int id = context.getBlockType(pt);

            switch (id) {
            case BlockID.ICE:
                if (context.setBlock(pt, water)) {
                    ++affected;
                }
                break outer;

            case BlockID.SNOW:
                if (context.setBlock(pt, air)) {
                    ++affected;
                }
                break outer;

            case BlockID.AIR:
                continue;

            default:
                break outer;
            }
        }
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }

    @Override
    public int getChangeCount() {
        return affected;
    }

}
