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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.regions.FlatRegion;

import java.util.List;

/**
 * Applies region functions to columns in a {@link FlatRegion}.
 */
public class FlatRegionVisitor implements Operation {

    private final FlatRegion flatRegion;
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

        this.flatRegion = flatRegion;
        this.function = function;
    }

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        for (Vector2D pt : flatRegion.asFlatRegion()) {
            if (function.apply(pt)) {
                affected++;
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addStatusMessages(List<String> messages) {
        messages.add(getAffected() + " columns affected");
    }

}

