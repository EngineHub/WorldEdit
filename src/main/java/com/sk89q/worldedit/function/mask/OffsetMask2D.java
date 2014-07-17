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

import com.sk89q.worldedit.Vector2D;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Checks whether another mask tests true for a position that is offset
 * a given vector.
 */
public class OffsetMask2D extends AbstractMask2D {

    private Mask2D mask;
    private Vector2D offset;

    /**
     * Create a new instance.
     *
     * @param mask the mask
     * @param offset the offset
     */
    public OffsetMask2D(Mask2D mask, Vector2D offset) {
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
    public Mask2D getMask() {
        return mask;
    }

    /**
     * Set the mask.
     *
     * @param mask the mask
     */
    public void setMask(Mask2D mask) {
        checkNotNull(mask);
        this.mask = mask;
    }

    /**
     * Get the offset.
     *
     * @return the offset
     */
    public Vector2D getOffset() {
        return offset;
    }

    /**
     * Set the offset.
     *
     * @param offset the offset
     */
    public void setOffset(Vector2D offset) {
        checkNotNull(offset);
        this.offset = offset;
    }

    @Override
    public boolean test(Vector2D vector) {
        return getMask().test(vector.add(offset));
    }

}
