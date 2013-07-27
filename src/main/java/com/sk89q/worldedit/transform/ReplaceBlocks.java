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

import java.util.Iterator;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.operation.ExecutionWatch;
import com.sk89q.worldedit.operation.Operation;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Replace all blocks within a given {@link Region} with a {@link Pattern}. A 
 * {@link Mask} can optionally be specified.
 */
public class ReplaceBlocks implements Operation, ChangeCountable {
    
    private final EditSession editSession;
    private final Region region;
    private final Pattern pattern;
    private final Mask mask;
    private final Iterator<BlockVector> it;
    
    private boolean started = false;
    private int affected = 0;
    
    /**
     * Create a block replace operation that replaces all blocks within the given
     * region with a block returned by the given block pattern.
     *
     * <p>This is the operation most commonly known as "//set".</p>
     * 
     * @param editSession edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     */
    public ReplaceBlocks(EditSession editSession, Region region, Pattern pattern) {
        this.editSession = editSession;
        this.region = region;
        this.pattern = pattern;
        this.mask = null;
        it = region.iterator();
    }
    
    /**
     * Create a block replace operation.
     * 
     * @param editSession edit session to apply changes to
     * @param region area to apply changes to
     * @param pattern pattern to set
     * @param mask an optional mask to use, null is allowed
     */
    public ReplaceBlocks(EditSession editSession, Region region, Pattern pattern, Mask mask) {
        this.editSession = editSession;
        this.region = region;
        this.pattern = pattern;
        this.mask = mask;
        it = region.iterator();
    }

    @Override
    public Operation resume(ExecutionHint opt) throws WorldEditException, InterruptedException {
        if (!started && opt.preferSingleRun() && region instanceof CuboidRegion) { // Doing this for speed
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
            ExecutionWatch watch = opt.createWatch();
            
            while (it.hasNext() && watch.shouldContinue()) {
                Vector pt = it.next();
                if (matches(pt) && editSession.setBlock(pt, pattern.next(pt))) {
                    ++affected;
                }
            }
        }
        
        started = true;

        return it.hasNext() ? this : null;
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
    public int getChangeCount() {
        return affected;
    }
    
    @Override
    public String toString() {
        return String.format("ReplaceBlocks(region=%s, mask=%s, pattern=%s)", 
                region, mask, pattern);
    }

}
