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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.Implied;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.Region;

/**
 * Stack the blocks within a given {@link Region} in a direction. For example, if
 * a portion of a bridge is stacked in the direction of the bridge, it would be
 * repeated a given number of times.
 * <p>
 * At the moment, this operation assumes the region is a cuboid.
 */
public class StackArea implements Operation, ChangeCountable {
    
    private final EditSession context;
    private final Region region;
    
    private boolean copyAir = true;
    private int count = 1;
    private Vector dir;
    private int affected = 0;

    /**
     * Create a stacking operation.
     *
     * @param context to apply changes to
     * @param region area to apply changes to
     */
    public StackArea(EditSession context, Region region) {
        this.context = context;
        this.region = region;
    }

    /**
     * Create a stacking operation.
     *
     * @param context to apply changes to
     * @param region area to apply changes to
     * @param airIgnore true to not copy air
     */
    @Command(name = "Stack Area",
             aliases = "stack", desc = "Copies a selection repeatedly in a direction",
             help = "Makes copies of an area end to end in a given direction. " +
                    "For example, a part of a bridge may need to repeat itself for some " +
                    "length and this function allows one portion to be repeated.")
    public StackArea(EditSession context, Region region,
                     boolean airIgnore, @Implied Vector direction) {
        this.context = context;
        this.region = region;
        setCopyAir(!airIgnore); // Reversed!
    }

    /**
     * Returns whether air is being copied.
     * 
     * @return true if copying air
     */
    public boolean hasCopyAir() {
        return copyAir;
    }

    /**
     * Set whether air is copied.
     * 
     * @param copyAir true to copy air
     */
    public void setCopyAir(boolean copyAir) {
        this.copyAir = copyAir;
    }

    /**
     * Get the number of times to repeat the area.
     * 
     * @return the number of times to repeat
     */
    public int getCount() {
        return count;
    }

    /**
     * Set the number of times to repeat the area.
     * 
     * @param count the number of times to repeat
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Get the direction to stack the area. Usually this is a unit vector.
     * 
     * @return the direction
     */
    public Vector getDirection() {
        return dir;
    }

    /**
     * Set the direction to stack the area. Usually this is a unit vector.
     * 
     * @param dir the direction
     */
    public void setDirection(Vector dir) {
        this.dir = dir;
    }

    @Override
    public Operation resume(Execution opt) throws WorldEditException {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();

        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        int xs = region.getWidth();
        int ys = region.getHeight();
        int zs = region.getLength();

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = minY; y <= maxY; ++y) {
                    BaseBlock block = context.getBlock(new Vector(x, y, z));

                    if (!block.isAir() || copyAir) {
                        for (int i = 1; i <= count; ++i) {
                            Vector pos = new Vector(x + xs * dir.getBlockX()
                                    * i, y + ys * dir.getBlockY() * i, z + zs
                                    * dir.getBlockZ() * i);

                            if (context.setBlock(pos, block)) {
                                ++affected;
                            }
                        }
                    }
                }
            }
        }

        return null;
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
