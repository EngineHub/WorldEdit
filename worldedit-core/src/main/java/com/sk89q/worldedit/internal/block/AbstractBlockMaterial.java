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
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.registry.BlockMaterial;

import java.util.EnumMap;
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

    @SuppressWarnings("this-escape")
    public LazyReference<EnumMap<Direction, EnumSet<ShapeType>>> isFullFace = LazyReference.from(() -> {
        EnumMap<Direction, EnumSet<ShapeType>> enumMap = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            if (!face.isUpright() && !face.isCardinal()) {
                continue;
            }

            EnumSet<ShapeType> enumSet = EnumSet.noneOf(ShapeType.class);
            for (ShapeType shapeType : ShapeType.values()) {
                if (isFaceFull(getShape(shapeType), face)) {
                    enumSet.add(shapeType);
                }
            }
            enumMap.put(face, enumSet);
        }
        return enumMap;
    });

    @Override
    public boolean isFullCube(ShapeType shapeType) {
        return isFullCube.getValue().contains(shapeType);
    }

    @Override
    public boolean isFullFace(ShapeType shapeType, Direction face) {
        return isFullFace.getValue().get(face).contains(shapeType);
    }

    protected abstract VS getShape(ShapeType shapeType);

    protected abstract boolean isShapeFullBlock(VS shape);

    protected abstract boolean isFaceFull(VS shape, Direction face);
}
