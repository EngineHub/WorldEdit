/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.visitor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.FlatRegion;

import java.util.List;

/**
 * Visits the layers within a region.
 *
 * <p>This class works by iterating over all the columns in a {@link FlatRegion},
 * finding the first ground block in each column (searching from a given
 * maximum Y down to a minimum Y), and then applies a {@link LayerFunction} to
 * each layer.</p>
 */
public class LayerVisitor implements Operation {

    private final FlatRegion flatRegion;
    private final LayerFunction function;
    private Mask2D mask = Masks.alwaysTrue2D();
    private int minY;
    private int maxY;

    /**
     * Create a new visitor.
     *
     * @param flatRegion the flat region to visit
     * @param minY the minimum Y to stop the search at
     * @param maxY the maximum Y to begin the search at
     * @param function the layer function to apply t blocks
     */
    public LayerVisitor(FlatRegion flatRegion, int minY, int maxY, LayerFunction function) {
        checkNotNull(flatRegion);
        checkArgument(minY <= maxY, "minY <= maxY required");
        checkNotNull(function);

        this.flatRegion = flatRegion;
        this.minY = minY;
        this.maxY = maxY;
        this.function = function;
    }

    /**
     * Get the mask that determines which columns within the flat region
     * will be visited.
     *
     * @return a 2D mask
     */
    public Mask2D getMask() {
        return mask;
    }

    /**
     * Set the mask that determines which columns within the flat region
     * will be visited.
     *
     * @param mask a 2D mask
     */
    public void setMask(Mask2D mask) {
        checkNotNull(mask);
        this.mask = mask;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        for (BlockVector2 column : flatRegion.asFlatRegion()) {
            if (!mask.test(column)) {
                continue;
            }

            // Abort if we are underground
            if (function.isGround(column.toBlockVector3(maxY + 1))) {
                continue;
            }

            boolean found = false;
            int groundY = 0;
            for (int y = maxY; y >= minY; --y) {
                BlockVector3 test = column.toBlockVector3(y);
                if (!found) {
                    if (function.isGround(test)) {
                        found = true;
                        groundY = y;
                    }
                }

                if (found) {
                    if (!function.apply(test, groundY - y)) {
                        break;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

}
