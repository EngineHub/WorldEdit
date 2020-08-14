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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tests true if all contained masks test true.
 */
public class MaskIntersection2D implements Mask2D {

    private final Set<Mask2D> masks = new HashSet<>();

    /**
     * Create a new intersection.
     *
     * @param masks a list of masks
     */
    public MaskIntersection2D(Collection<Mask2D> masks) {
        checkNotNull(masks);
        this.masks.addAll(masks);
    }

    /**
     * Create a new intersection.
     *
     * @param mask a list of masks
     */
    public MaskIntersection2D(Mask2D... mask) {
        this(Arrays.asList(checkNotNull(mask)));
    }

    /**
     * Add some masks to the list.
     *
     * @param masks the masks
     */
    public void add(Collection<Mask2D> masks) {
        checkNotNull(masks);
        this.masks.addAll(masks);
    }

    /**
     * Add some masks to the list.
     *
     * @param mask the masks
     */
    public void add(Mask2D... mask) {
        add(Arrays.asList(checkNotNull(mask)));
    }

    /**
     * Get the masks that are tested with.
     *
     * @return the masks
     */
    public Collection<Mask2D> getMasks() {
        return masks;
    }

    @Override
    public boolean test(BlockVector2 vector) {
        if (masks.isEmpty()) {
            return false;
        }

        for (Mask2D mask : masks) {
            if (!mask.test(vector)) {
                return false;
            }
        }

        return true;
    }

}
