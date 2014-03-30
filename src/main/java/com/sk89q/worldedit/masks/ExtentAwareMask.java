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

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Extent;

/**
 * Extended by masks to make them potentially use an {@link Extent} rather than
 * the {@link EditSession}.
 * </p>
 * At the moment, masks are coupled to {@link EditSession} when they should
 * not be. However, because a change to {@link Mask} would cause massive breakage in
 * the API, that change is deferred until further notice and this class exists as
 * an opt-in mixin for adding support for {@link Extent}s.
 */
public abstract class ExtentAwareMask extends AbstractMask {

    private Extent extent;

    /**
     * Get the extent that will be used for lookups.
     *
     * @return the extent, or null if the {@link EditSession} is to be used
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set the extent that will be used for lookups.
     *
     * @param extent the extent, or null if the {@link EditSession} is to be used
     */
    public void setExtent(Extent extent) {
        this.extent = extent;
    }

    /**
     * Get the extent to use for operations. Subclasses should call this method
     * rather than access the passed {@link EditSession} directly.
     *
     * @param editSession the passed in {@link EditSession}
     * @return an extent
     */
    protected Extent getExtent(EditSession editSession) {
        if (extent != null) {
            return extent;
        } else {
            return editSession;
        }
    }

}
