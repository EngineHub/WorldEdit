/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
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

package com.sk89q.worldedit.operation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.Region;

/**
 * An abstract implementation of {@link com.sk89q.worldedit.operation.FlatRegionFunction}
 * that searches for the ground, which is considered to be any non-air block.
 * <p>
 * It functions by starting from the upperY in each column and traversing
 * down the column until it finds the first non-air block, at which point
 * {@link #apply(com.sk89q.worldedit.Vector, com.sk89q.worldedit.blocks.BaseBlock)}
 * is called on that non-air block. {@link #shouldContinue(com.sk89q.worldedit.Vector2D)}
 * is called before each column is traversed, which allows implementations
 * to "skip" a column and avoid the ground search.
 */
public abstract class GroundFindingFunction implements FlatRegionFunction {

    private final EditSession editSession;
    private int lowerY;
    private int upperY;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     */
    protected GroundFindingFunction(EditSession editSession) {
        this.editSession = editSession;
        checkYBounds();
    }

    /**
     * Check whether upperY is >= lowerY.
     */
    private void checkYBounds() {
        if (upperY < lowerY) {
            throw new IllegalArgumentException("upperY must be greater than or equal to lowerY");
        }
    }

    /**
     * Get the upper Y coordinate to start the ground search from.
     *
     * @return the upper Y coordinate
     */
    public int getUpperY() {
        return upperY;
    }

    /**
     * Get the lower Y coordinate to end the ground search at.
     *
     * @return lowerY the lower Y coordinate
     */
    public int getLowerY() {
        return lowerY;
    }

    /**
     * Set the range of Y coordinates to perform a search for ground within.
     *
     * @param lowerY the lower Y coordinate
     * @param upperY the upper Y coordinate (upperY >= lowerY)
     */
    public void setRange(int lowerY, int upperY) {
        this.lowerY = lowerY;
        this.upperY = upperY;
        checkYBounds();
    }

    /**
     * Set the range of Y coordinates to perform a search for ground within from
     * the minimum and maximum Y of the given region.
     *
     * @param region the region
     */
    public void setRange(Region region) {
        setRange(region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
    }

    /**
     * Increase the upper Y by the given amount.
     *
     * @param y the amount to increase the upper Y by
     */
    public void raiseCeiling(int y) {
        if (y <= 0) {
            throw new IllegalArgumentException("Can't raise by a negative");
        }
        upperY += y;
    }

    @Override
    public final boolean apply(Vector2D pt) throws WorldEditException {
        // Don't want to be in the ground
        if (!editSession.getBlock(pt.toVector(upperY + 1)).isAir()) {
            return false;
        }

        if (!shouldContinue(pt)) {
            return false;
        }

        for (int y = upperY + 1; y >= lowerY; --y) {
            Vector testPt = pt.toVector(y);
            BaseBlock block = editSession.getBlock(testPt);

            if (block.getType() != BlockID.AIR) {
                return apply(testPt, block);
            }
        }

        return false;
    }

    /**
     * Returns whether a search for the ground should be performed for the given
     * column. Return false if the column is to be skipped.
     *
     * @param pt the point
     * @return true if the search should begin
     */
    protected boolean shouldContinue(Vector2D pt) {
        return true;
    }

    /**
     * Apply the function to the given ground block.
     * <p>
     * The block above the given ground block may or may not be air, but it is
     * a block that can be replaced. For example, this block may be a tall
     * grass block or a flower. However, the ground block also could be
     * the flower or grass block itself, depending on the configuration of the
     * GroundFindingFunction.
     *
     * @param pt the position
     * @param block the block
     * @return true if something was changed
     * @throws WorldEditException thrown on an error
     */
    protected abstract boolean apply(Vector pt, BaseBlock block) throws WorldEditException;

}
