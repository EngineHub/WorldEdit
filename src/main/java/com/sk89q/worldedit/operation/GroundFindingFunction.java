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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * An abstract implementation of {@link com.sk89q.worldedit.operation.FlatRegionFunction}
 * that searches for the ground, which is considered to be any non-air block.
 */
public abstract class GroundFindingFunction implements FlatRegionFunction {

    private final EditSession editSession;
    private int lowerY;
    private int upperY;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     * @param lowerY the lower Y
     * @param upperY the upper Y (lowerY <= upperY)
     */
    protected GroundFindingFunction(EditSession editSession, int lowerY, int upperY) {
        this.editSession = editSession;
        this.lowerY = lowerY;
        this.upperY = upperY;
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
     * Set the upper Y coordinate to start the ground search from.
     *
     * @param upperY the upper Y coordinate
     */
    public void setUpperY(int upperY) {
        this.upperY = upperY;
        checkYBounds();
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
     * Set the lower Y coordinate to end the ground search at.
     *
     * @return lowerY the lower Y coordinate
     */
    public void setLowerY(int lowerY) {
        this.lowerY = lowerY;
        checkYBounds();
    }

    @Override
    public final boolean apply(Vector2D pt) throws WorldEditException {
        // Don't want to be in the ground
        if (!editSession.getBlock(pt.toVector(upperY)).isAir()) {
            return false;
        }

        if (!shouldContinue(pt)) {
            return false;
        }

        for (int y = upperY; y >= lowerY; --y) {
            Vector testPt = pt.toVector(y);
            BaseBlock block = editSession.getBlock(testPt);

            if (block.getType() != BlockID.AIR) {
                return apply(testPt, block);
            }
        }

        return false;
    }

    /**
     * Returns whether a search for the ground should be performed for the given point.
     *
     * @param pt the point
     * @return true if the search should begin
     */
    protected boolean shouldContinue(Vector2D pt) {
        return true;
    }

    /**
     * Apply the function to the given ground block.
     *
     * @param pt the position
     * @param block the block
     * @return true if something was changed
     * @throws WorldEditException thrown on an error
     */
    protected abstract boolean apply(Vector pt, BaseBlock block) throws WorldEditException;

}
