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

package com.sk89q.worldedit.regions.search;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.masks.Mask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A ground finder that uses a {@link com.sk89q.worldedit.masks.Mask} to determine
 * ground blocks.
 */
public class MaskingGroundSearch extends AbstractGroundSearch {

    private final EditSession editSession;
    private final Mask mask;

    /**
     * Create a new instance.
     * </p>
     * If a mask that matches non-ground blocks is available, it can be inverted with
     * {@link com.sk89q.worldedit.masks.InvertedMask}.
     *
     * @param editSession an edit session
     * @param mask a mask that matches ground blocks
     */
    public MaskingGroundSearch(EditSession editSession, Mask mask) {
        checkNotNull(editSession);
        checkNotNull(mask);

        this.editSession = editSession;
        this.mask = mask;
    }

    /**
     * Get the mask that matches ground blocks.
     *
     * @return the mask
     */
    public Mask getMask() {
        return mask;
    }

    @Override
    protected boolean isGround(Vector position) {
        return mask.matches(editSession, position);
    }

}
