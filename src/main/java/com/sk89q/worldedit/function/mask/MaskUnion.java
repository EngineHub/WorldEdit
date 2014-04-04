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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.Vector;

import java.util.Collection;

/**
 * Combines several masks and requires that one or more masks return true
 * when a certain position is tested. It serves as a logical OR operation
 * on a list of masks.
 */
public class MaskUnion extends MaskIntersection {

    /**
     * Create a new union.
     *
     * @param masks a list of masks
     */
    public MaskUnion(Collection<Mask> masks) {
        super(masks);
    }

    /**
     * Create a new union.
     *
     * @param mask a list of masks
     */
    public MaskUnion(Mask... mask) {
        super(mask);
    }

    @Override
    public boolean test(Vector vector) {
        Collection<Mask> masks = getMasks();

        for (Mask mask : masks) {
            if (mask.test(vector)) {
                return true;
            }
        }

        return false;
    }

}
