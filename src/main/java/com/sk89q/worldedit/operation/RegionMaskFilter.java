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

package com.sk89q.worldedit.operation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.masks.Mask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Passes positions that match the given mask onto the provided function.
 */
public class RegionMaskFilter implements RegionFunction {

    private final EditSession editSession;
    private final Mask mask;
    private final RegionFunction function;

    public RegionMaskFilter(EditSession editSession, Mask mask, RegionFunction function) {
        checkNotNull(function, "function must not be null");
        checkNotNull(editSession, "editSession must not be null");
        checkNotNull(mask, "mask must not be null");

        this.editSession = editSession;
        this.mask = mask;
        this.function = function;
    }

    @Override
    public boolean apply(Vector position) throws WorldEditException {
        if (mask.matches(editSession, position)) {
            return function.apply(position);
        } else {
            return false;
        }
    }

}
