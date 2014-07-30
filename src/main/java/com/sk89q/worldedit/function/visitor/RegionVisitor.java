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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.AbstractOperation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.util.AffectedCounter;
import com.sk89q.worldedit.regions.ChunkSortedIterable;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.task.progress.Progress;
import com.sk89q.worldedit.util.task.progress.ProgressIterator;

import java.util.Iterator;

/**
 * Utility class to apply region functions to {@link com.sk89q.worldedit.regions.Region}.
 */
public class RegionVisitor extends AbstractOperation implements AffectedCounter {

    private final ProgressIterator<? extends Vector> iterator;
    private final RegionFunction function;
    private int affected = 0;

    public RegionVisitor(Region region, RegionFunction function) {
        Iterator<? extends Vector> iterator;
        if (region instanceof ChunkSortedIterable) {
            iterator = ((ChunkSortedIterable) region.clone()).chunkSortedIterator();
        } else {
            iterator = region.clone().iterator();
        }

        this.iterator = ProgressIterator.create(iterator, region.getArea());
        this.function = function;
    }

    @Override
    public int getAffected() {
        return affected;
    }

    @Override
    public Result resume(RunContext run) throws WorldEditException {
        if (run.isCancelled()) {
            return Result.STOP;
        }

        while (iterator.hasNext()) {
            Vector pt = iterator.next();
            if (function.apply(pt)) {
                affected++;
            }

            if (!run.shouldContinue()) {
                return Result.CONTINUE;
            }
        }

        return Result.STOP;
    }

    @Override
    public String toString() {
        return "{" + function + "} over a region";
    }

    @Override
    public Progress getProgress() {
        return iterator.getProgress();
    }

}

