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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Various utility functions related to {@link Mask} and {@link Mask2D}.
 */
public final class Masks {

    private Masks() {
    }

    /**
     * Return a 3D mask that always returns true;
     *
     * @return a mask
     */
    public static Mask alwaysTrue() {
        return new AbstractMask() {
            @Override
            public boolean test(Vector vector) {
                return true;
            }
        };
    }

    /**
     * Return a 2D mask that always returns true;
     *
     * @return a mask
     */
    public static Mask2D alwaysTrue2D() {
        return new AbstractMask2D() {
            @Override
            public boolean test(Vector2D vector) {
                return true;
            }
        };
    }

    /**
     * Negate the given mask.
     *
     * @param mask the mask
     * @return a new mask
     */
    public static Mask negate(final Mask mask) {
        checkNotNull(mask);
        return new AbstractMask() {
            @Override
            public boolean test(Vector vector) {
                return !mask.test(vector);
            }
        };
    }

    /**
     * Negate the given mask.
     *
     * @param mask the mask
     * @return a new mask
     */
    public static Mask2D negate(final Mask2D mask) {
        checkNotNull(mask);
        return new AbstractMask2D() {
            @Override
            public boolean test(Vector2D vector) {
                return !mask.test(vector);
            }
        };
    }

    /**
     * Wrap an old-style mask and convert it to a new mask.
     *
     * @param editSession the edit session to bind to
     * @param mask the old-style mask
     * @return a new-style mask
     */
    public static Mask wrap(final EditSession editSession, final com.sk89q.worldedit.masks.Mask mask) {
        checkNotNull(mask);
        return new AbstractMask() {
            @Override
            public boolean test(Vector vector) {
                return mask.matches(editSession, vector);
            }
        };
    }

}
