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
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

/**
 * Places blocks from a {@link Pattern} on the top available space
 * in each column of a {@link Region}.
 */
public class OverlayBlocks extends AbstractOperation implements BlockChange {

    private final EditSession context;
    private final Region region;
    private final Pattern pattern;

    private int affected = 0;

    /**
     * Create a block overlay operation.
     * 
     * @param context edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     */
    public OverlayBlocks(EditSession context, Region region, Pattern pattern) {
        this.context = context;
        this.region = region;
        this.pattern = pattern;
    }

    @Override
    protected Operation resume() throws MaxChangedBlocksException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        LocalWorld world = context.getWorld();

        Iterator<BlockVector> points = region.columnIterator();
        int maxY = Math.min(context.getWorld().getMaxY(), max.getBlockY() + 1);
        int minY = Math.max(0, min.getBlockY() - 1);

        while (points.hasNext()) {
            Vector columnPt = points.next();

            for (int y = maxY - 1; y >= minY; --y) {
                Vector pt = columnPt.setY(y);
                if (!context.getBlock(pt).isAir() &&
                        context.getBlock(pt.setY(y + 1)).isAir()) {
                    if (context.setBlock(pt, pattern.next(pt))) {
                        ++affected;
                        break;
                    }
                }
            }
            break;
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
