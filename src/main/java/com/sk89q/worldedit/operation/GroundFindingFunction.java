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
 * that searches for the first "ground" block." A ground block is found when the
 * method {@link #shouldPassThrough(Vector, BaseBlock)} returns false, which, by default,
 * does so for all non-air blocks.
 * <p>
 * This function starts from the provided upperY in each block column and traverses
 * down the column until it finds the first ground block, at which point
 * {@link #apply(Vector, BaseBlock)} is called with the position and the
 * {@link BaseBlock} for the found ground block. Implementations that want
 * to skip certain columns (and avoid the ground search) can override
 * {@link #shouldContinue(com.sk89q.worldedit.Vector2D)} and return true as necessary.
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

            if (!shouldPassThrough(testPt, block)) {
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
     * Returns whether the given block should be "passed through" when
     * conducting the ground search.
     * <p>
     * Examples of blocks where this method could return true include snow, tall
     * grass, shrubs, and flowers. Note that this method will also receive
     * calls on each air block so that condition must be handled. Be aware
     * that blocks passed through are not automatically removed
     * from the world, so implementations may need to remove the block
     * immediately above the ground.
     * <p>
     * The default implementation only returns true for air blocks.
     *
     * @param position the position
     * @param block the block
     * @return true if the block should be passed through during the ground search
     */
    protected boolean shouldPassThrough(Vector position, BaseBlock block) {
        return block.getType() == BlockID.AIR;
    }

    /**
     * Apply the function to the given ground block.
     * <p>
     * Naive implementations may provide flowers, tall grass, and other
     * blocks not often considered to be the ground as the ground block.
     *
     * @param position the position
     * @param block the block
     * @return true if something was changed
     * @throws WorldEditException thrown on an error
     */
    protected abstract boolean apply(Vector position, BaseBlock block) throws WorldEditException;

}
