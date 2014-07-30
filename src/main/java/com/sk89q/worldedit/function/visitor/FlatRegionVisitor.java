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

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.operation.AbstractOperation;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.util.AffectedCounter;
import com.sk89q.worldedit.regions.FlatChunkSortedIterable;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.util.task.progress.Progress;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Applies region functions to columns in a {@link FlatRegion}.
 */
public class FlatRegionVisitor extends AbstractOperation implements AffectedCounter {

    private final Iterator<? extends Vector2D> iterator;
    private final FlatRegionFunction function;
    private int affected = 0;

    /**
     * Create a new visitor.
     *
     * @param flatRegion a flat region
     * @param function a function to apply to columns
     */
    public FlatRegionVisitor(FlatRegion flatRegion, FlatRegionFunction function) {
        checkNotNull(flatRegion);
        checkNotNull(function);

        if (flatRegion instanceof FlatChunkSortedIterable) {
            this.iterator = ((FlatChunkSortedIterable) flatRegion.clone()).flatChunkSortedIterator();
        } else {
            this.iterator = flatRegion.clone().asFlatRegion().iterator();
        }

        this.function = function;
    }

    @Override
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        while (iterator.hasNext()) {
            if (function.apply(iterator.next())) {
                affected++;
            }

            if (!run.shouldContinue()) {
                return this;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public Progress getProgress() {
        return Progress.indeterminate();
    }

}

