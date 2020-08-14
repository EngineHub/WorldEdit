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

import com.sk89q.worldedit.extent.Extent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract implementation of {@link Mask} that takes uses an {@link Extent}.
 */
public abstract class AbstractExtentMask extends AbstractMask {

    private Extent extent;

    /**
     * Construct a new mask.
     *
     * @param extent the extent
     */
    protected AbstractExtentMask(Extent extent) {
        setExtent(extent);
    }

    /**
     * Get the extent.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set the extent.
     *
     * @param extent the extent
     */
    public void setExtent(Extent extent) {
        checkNotNull(extent);
        this.extent = extent;
    }

}
