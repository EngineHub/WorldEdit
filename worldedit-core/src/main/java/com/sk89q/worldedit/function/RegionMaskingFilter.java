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

package com.sk89q.worldedit.function;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.mask.Mask;

/**
 * Passes calls to {@link #apply(com.sk89q.worldedit.Vector)} to the
 * delegate {@link com.sk89q.worldedit.function.RegionFunction} if they
 * match the given mask.
 */
public class RegionMaskingFilter implements RegionFunction {

    private final RegionFunction function;
    private Mask mask;

    /**
     * Create a new masking filter.
     *
     * @param mask the mask
     * @param function the function
     */
    public RegionMaskingFilter(Mask mask, RegionFunction function) {
        checkNotNull(function);
        checkNotNull(mask);
        this.mask = mask;
        this.function = function;
    }

    @Override
    public boolean apply(Vector position) throws WorldEditException {
        return mask.test(position) && function.apply(position);
    }

}
