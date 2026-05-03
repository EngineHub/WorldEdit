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

package com.sk89q.worldedit.internal.block;

import com.sk89q.worldedit.blocks.ShapeType;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.registry.BlockMaterial;

import java.util.EnumSet;

public abstract class AbstractBlockMaterial<VS> implements BlockMaterial {
    @SuppressWarnings("this-escape")
    public LazyReference<EnumSet<ShapeType>> isFullCube = LazyReference.from(() -> {
        EnumSet<ShapeType> enumSet = EnumSet.noneOf(ShapeType.class);
        for (ShapeType shapeType : ShapeType.values()) {
            if (isShapeFullBlock(getShape(shapeType))) {
                enumSet.add(shapeType);
            }
        }

        return enumSet;
    });

    @Override
    public boolean isFullCube(ShapeType shapeType) {
        return isFullCube.getValue().contains(shapeType);
    }

    protected abstract VS getShape(ShapeType shapeType);

    protected abstract boolean isShapeFullBlock(VS shape);
}
