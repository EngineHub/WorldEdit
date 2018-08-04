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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;

/**
 * An implementation of an {@link BreadthFirstSearch} that uses a mask to
 * determine where a block should be visited.
 */
public class RecursiveVisitor extends BreadthFirstSearch {

    private final Mask mask;

    /**
     * Create a new recursive visitor.
     *
     * @param mask the mask
     * @param function the function
     */
    public RecursiveVisitor(Mask mask, RegionFunction function) {
        super(function);
        checkNotNull(mask);
        this.mask = mask;
    }

    @Override
    protected boolean isVisitable(Vector from, Vector to) {
        return mask.test(to);
    }
}
