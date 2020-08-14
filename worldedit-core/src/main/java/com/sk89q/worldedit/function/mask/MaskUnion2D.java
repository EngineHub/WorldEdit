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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector2;

import java.util.Collection;

/**
 * Tests true if any contained mask is true, even if it just one.
 */
public class MaskUnion2D extends MaskIntersection2D {

    /**
     * Create a new union.
     *
     * @param masks a list of masks
     */
    public MaskUnion2D(Collection<Mask2D> masks) {
        super(masks);
    }

    /**
     * Create a new union.
     *
     * @param mask a list of masks
     */
    public MaskUnion2D(Mask2D... mask) {
        super(mask);
    }

    @Override
    public boolean test(BlockVector2 vector) {
        Collection<Mask2D> masks = getMasks();

        for (Mask2D mask : masks) {
            if (mask.test(vector)) {
                return true;
            }
        }

        return false;
    }

}
