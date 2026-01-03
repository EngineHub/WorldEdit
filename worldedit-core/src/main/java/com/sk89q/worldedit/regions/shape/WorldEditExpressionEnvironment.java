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

package com.sk89q.worldedit.regions.shape;

import com.google.errorprone.annotations.InlineMe;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.expression.ExpressionEnvironment;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.ScaleAndTranslateTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.registry.LegacyMapper;

public class WorldEditExpressionEnvironment implements ExpressionEnvironment {

    private final Transform transform;
    private Vector3 current = Vector3.ZERO;
    private final Extent extent;

    /**
     * Creates a new WorldEditExpressionEnvironment.
     *
     * @deprecated Use {@link #WorldEditExpressionEnvironment(Extent, Transform)} and pass a {@link ScaleAndTranslateTransform}.
     */
    @InlineMe(replacement = "this(extent, new ScaleAndTranslateTransform(zero, unit))", imports = "com.sk89q.worldedit.math.transform.ScaleAndTranslateTransform")
    @Deprecated
    public WorldEditExpressionEnvironment(Extent extent, Vector3 unit, Vector3 zero) {
        this(extent, new ScaleAndTranslateTransform(zero, unit));
    }

    public WorldEditExpressionEnvironment(Extent extent, Transform transform) {
        this.extent = extent;
        this.transform = transform;
    }

    public BlockVector3 toWorld(double x, double y, double z) {
        return transform.apply(Vector3.at(x, y, z)).add(0.5, 0.5, 0.5).toBlockPoint();
    }

    public Vector3 toWorldRel(double x, double y, double z) {
        return current.add(x, y, z);
    }

    private int getLegacy(BlockVector3 position, int index) {
        final int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(extent.getBlock(position).toImmutableState());
        return legacy == null ? 0 : legacy[index];
    }

    @Override
    public int getBlockType(double x, double y, double z) {
        return getLegacy(toWorld(x, y, z), 0);
    }

    @Override
    public int getBlockData(double x, double y, double z) {
        return getLegacy(toWorld(x, y, z), 1);
    }

    @Override
    public int getBlockTypeAbs(double x, double y, double z) {
        return getLegacy(BlockVector3.at(x, y, z), 0);
    }

    @Override
    public int getBlockDataAbs(double x, double y, double z) {
        return getLegacy(BlockVector3.at(x, y, z), 1);
    }

    @Override
    public int getBlockTypeRel(double x, double y, double z) {
        return getLegacy(toWorldRel(x, y, z).toBlockPoint(), 0);
    }

    @Override
    public int getBlockDataRel(double x, double y, double z) {
        return getLegacy(toWorldRel(x, y, z).toBlockPoint(), 1);
    }

    public void setCurrentBlock(Vector3 current) {
        this.current = current;
    }

}
