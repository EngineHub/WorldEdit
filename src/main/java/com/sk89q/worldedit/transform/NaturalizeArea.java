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
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.ColumnVisitor;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.regions.FlatRegion;

/**
 * Performs a "naturalization" of an area where all stone, grass, and dirt blocks are
 * transformed into one layer of grass, three layers of dirt, and all layers of stone
 * below. The top block in a column in a selection is chosen to be the grass block,
 * whether it is exposed to skylight or not.
 * <p>
 * At the moment, this operation assumes the region is a cuboid.
 */
public class NaturalizeArea extends ColumnVisitor implements ChangeCountable {

    private final BaseBlock grass = new BaseBlock(BlockID.GRASS);
    private final BaseBlock dirt = new BaseBlock(BlockID.DIRT);
    private final BaseBlock stone = new BaseBlock(BlockID.STONE);

    private final EditSession context;
    private final int minY;
    private final int maxY;

    private int affected = 0;

    /**
     * Create a naturalization operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     */
    public NaturalizeArea(EditSession context, FlatRegion region) {
        super(region);

        this.context = context;
        maxY = Math.min(context.getWorld().getMaxY(), region.getMaximumPoint().getBlockY() + 1);
        minY = Math.max(0, region.getMinimumPoint().getBlockY() - 1);
    }

    @Override
    public void visitColumn(ExecutionHint opt, Vector columnPt) throws WorldEditException {
        int level = -1;

        for (int y = maxY; y >= minY; --y) {
            Vector pt = columnPt.setY(y);

            int blockType = context.getBlockType(pt);

            boolean isTransformable =
                    blockType == BlockID.GRASS
                    || blockType == BlockID.DIRT
                    || blockType == BlockID.STONE;

            // Still searching for the top block
            if (level == -1) {
                if (!isTransformable) {
                    continue; // Not transforming this column yet
                }

                level = 0;
            }

            if (level >= 0) {
                if (isTransformable) {
                    if (level == 0) {
                        context.setBlock(pt, grass);
                        affected++;
                    } else if (level <= 2) {
                        context.setBlock(pt, dirt);
                        affected++;
                    } else {
                        context.setBlock(pt, stone);
                        affected++;
                    }
                }

                level++;
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
        return String.format("NaturalizeArea(region=%s)", getRegion());
    }

}
