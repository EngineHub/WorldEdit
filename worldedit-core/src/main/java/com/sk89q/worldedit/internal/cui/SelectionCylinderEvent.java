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

// $Id$

package com.sk89q.worldedit.internal.cui;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;

public class SelectionCylinderEvent implements CUIEvent {

    protected final BlockVector3 pos;
    protected final Vector2 radius;

    public SelectionCylinderEvent(BlockVector3 pos, Vector2 radius) {
        this.pos = pos;
        this.radius = radius;
    }

    @Override
    public String getTypeId() {
        return "cyl";
    }

    @Override
    public String[] getParameters() {
        return new String[] {
            String.valueOf(pos.getBlockX()),
            String.valueOf(pos.getBlockY()),
            String.valueOf(pos.getBlockZ()),
            String.valueOf(radius.getX()),
            String.valueOf(radius.getZ())
        };
    }
}
