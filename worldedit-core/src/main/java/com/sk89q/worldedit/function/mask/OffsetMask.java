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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;

import javax.annotation.Nullable;

/**
 * Checks whether another mask tests true for a position that is offset
 * a given vector.
 */
public class OffsetMask extends AbstractMask {

    private Mask mask;
    private Vector offset;

    /**
     * Create a new instance.
     *
     * @param mask the mask
     * @param offset the offset
     */
    public OffsetMask(Mask mask, Vector offset) {
        checkNotNull(mask);
        checkNotNull(offset);
        this.mask = mask;
        this.offset = offset;
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
     * Set the mask.
     *
     * @param mask the mask
     */
    public void setMask(Mask mask) {
        checkNotNull(mask);
        this.mask = mask;
    }

    /**
     * Get the offset.
     *
     * @return the offset
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * Set the offset.
     *
     * @param offset the offset
     */
    public void setOffset(Vector offset) {
        checkNotNull(offset);
        this.offset = offset;
    }

    @Override
    public boolean test(Vector vector) {
        return getMask().test(vector.add(offset));
    }

    @Nullable
    @Override
    public Mask2D toMask2D() {
        Mask2D childMask = getMask().toMask2D();
        if (childMask != null) {
            return new OffsetMask2D(childMask, getOffset().toVector2D());
        } else {
            return null;
        }
    }

}
