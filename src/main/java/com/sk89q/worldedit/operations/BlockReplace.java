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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Replace all blocks within a given {@link Region} with a {@link Pattern}. A 
 * {@link Mask} can optionally be specified.
 */
public class BlockReplace implements Operation, BlockChange {
    
    private final EditSession editSession;
    private final Region region;
    private final Pattern pattern;
    private final Mask mask;
    
    private int affected = 0;
    
    /**
     * Create a block replace operation.
     * 
     * @param editSession edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     */
    public BlockReplace(EditSession editSession, Region region, Pattern pattern) {
        this.editSession = editSession;
        this.region = region;
        this.pattern = pattern;
        this.mask = null;
    }
    
    /**
     * Create a block replace operation.
     * 
     * @param editSession edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     * @param mask an optional mask to use, null is allowed
     */
    public BlockReplace(EditSession editSession, Region region, Pattern pattern, Mask mask) {
        this.editSession = editSession;
        this.region = region;
        this.pattern = pattern;
        this.mask = mask;
    }

    @Override
    public Operation resume() throws MaxChangedBlocksException {
        if (region instanceof CuboidRegion) { // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        if (matches(pt) && editSession.setBlock(pt, pattern.next(pt))) {
                            ++affected;
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                if (matches(pt) && editSession.setBlock(pt, pattern.next(pt))) {
                    ++affected;
                }
            }
        }

        return null;
    }
    
    /**
     * Returns whether the given point matches the mask.
     * 
     * @param pt point
     * @return true if it matches
     */
    private boolean matches(Vector pt) {
        return mask == null || mask.matches(editSession, pt);
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
