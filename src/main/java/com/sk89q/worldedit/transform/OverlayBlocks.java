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

package com.sk89q.worldedit.transform;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.ColumnVisitor;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Places blocks from a {@link Pattern} on the top available space
 * in each column of a {@link Region}.
 */
public class OverlayBlocks extends ColumnVisitor implements ChangeCountable {

    private final EditSession context;
    private final int minY;
    private final int maxY;

    private final Pattern pattern;

    private int affected = 0;

    /**
     * Create a block overlay operation.
     * 
     * @param context edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     */
    public OverlayBlocks(EditSession context, FlatRegion region, Pattern pattern) {
        super(region);

        this.context = context;
        this.pattern = pattern;

        maxY = Math.min(context.getWorld().getMaxY(), region.getMaximumPoint().getBlockY() + 1);
        minY = Math.max(0, region.getMinimumPoint().getBlockY() - 1);
    }

    @Override
    public void visitColumn(ExecutionHint opt, Vector columnPt) throws WorldEditException {
        for (int y = maxY - 1; y >= minY; --y) {
            Vector pt = columnPt.setY(y);

            if (!context.getBlock(pt).isAir()) {
                // If it's at the top of the map, the (non-existent) block
                // above will always be "air", so don't attempt to check
                Vector above = pt.add(0, 1, 0);
                if (y == context.getWorld().getMaxY() || context.getBlock(above).isAir()) {
                    if (context.setBlock(above, pattern.next(above))) {
                        ++affected;
                        break;
                    }
                }
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
    
    @Override
    public String toString() {
        return String.format("OverlayBlocks(region=%s, pattern=%s)", getRegion(), pattern);
    }

}
