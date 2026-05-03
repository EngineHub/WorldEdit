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

import com.google.errorprone.annotations.InlineMe;
import com.sk89q.worldedit.blocks.ShapeType;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

public final class FullCubeMask extends AbstractExtentMask {

    private final ShapeType shapeType;

    @Deprecated
    @InlineMe(replacement = "this(extent, ShapeType.SHAPE)", imports = "com.sk89q.worldedit.blocks.ShapeType")
    public FullCubeMask(Extent extent) {
        this(extent, ShapeType.SHAPE);
    }

    public FullCubeMask(Extent extent, ShapeType shapeType) {
        super(extent);
        this.shapeType = shapeType;
    }

    @Override
    public boolean test(BlockVector3 vector) {
        Extent extent = getExtent();
        BlockState block = extent.getBlock(vector);
        return block.getMaterial().isFullCube(shapeType);
    }

}
