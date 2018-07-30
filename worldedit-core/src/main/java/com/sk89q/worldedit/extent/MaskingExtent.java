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

package com.sk89q.worldedit.extent;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.world.block.BlockStateHolder;

/**
 * Requires that all mutating methods pass a given {@link Mask}.
 */
public class MaskingExtent extends AbstractDelegateExtent {

    private Mask mask;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param mask the mask
     */
    public MaskingExtent(Extent extent, Mask mask) {
        super(extent);
        checkNotNull(mask);
        this.mask = mask;
    }

    /**
     * Get the mask.
     *
     * @return the mask
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set a mask.
     *
     * @param mask a mask
     */
    public void setMask(Mask mask) {
        checkNotNull(mask);
        this.mask = mask;
    }

    @Override
    public boolean setBlock(Vector location, BlockStateHolder block) throws WorldEditException {
        return mask.test(location) && super.setBlock(location, block);
    }

}
