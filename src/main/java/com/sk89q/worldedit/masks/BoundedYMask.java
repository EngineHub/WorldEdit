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
import com.sk89q.worldedit.Vector;

/**
 * Fails all tests against locations outside a range of Y values.
 */
public class BoundedYMask extends AbstractMask {

    private final int minY;
    private final int maxY;

    public BoundedYMask(int minY, int maxY) {
        if (minY > maxY) {
            throw new IllegalArgumentException("minY must be less than or equal to maxY");
        }
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        int y = pos.getBlockY();
        return y >= minY && y <= maxY;
    }

}
