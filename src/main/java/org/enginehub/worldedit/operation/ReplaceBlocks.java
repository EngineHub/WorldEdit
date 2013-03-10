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

package org.enginehub.worldedit.operation;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.enginehub.command.Expose;
import org.enginehub.worldedit.EditSession;
import org.enginehub.worldedit.WorldEditException;
import org.enginehub.worldedit.patterns.Pattern;

/**
 * Replace all blocks within a given {@link Region} with a {@link Pattern}. A 
 * {@link Mask} can optionally be specified.
 */
public class ReplaceBlocks implements Operation, ChangeCountable {
    
    private final EditSession editSession;
    private final Region region;
    private final Pattern pattern;
    private final Mask mask;
    
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
    @Expose(name = "Set Blocks", aliases = {"/set", "/s"},
            desc = "Set all the blocks in an area to a given pattern",
            help = "Iterates over an area and replaces all the blocks inside the area " +
                   "with the provided pattern.",
            group = "worldedit", key = "command-set")
    public ReplaceBlocks(EditSession editSession, Region region, Pattern pattern) {
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
    public ReplaceBlocks(EditSession editSession, Region region, Mask mask, Pattern pattern) {
        this.editSession = editSession;
        this.region = region;
        this.pattern = pattern;
        this.mask = mask;
    }

    @Override
    public Operation resume(Execution opt) throws WorldEditException {
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
    public int getChangeCount() {
        return affected;
    }

}
