/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions.shape;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.internal.expression.runtime.ExpressionEnvironment;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;

public class WorldEditExpressionEnvironment implements ExpressionEnvironment {

    private final Vector3 unit;
    private final Vector3 zero2;
    private Vector3 current = Vector3.ZERO;
    private EditSession editSession;

    public WorldEditExpressionEnvironment(EditSession editSession, Vector3 unit, Vector3 zero) {
        this.editSession = editSession;
        this.unit = unit;
        this.zero2 = zero.add(0.5, 0.5, 0.5);
    }

    public BlockVector3 toWorld(double x, double y, double z) {
        // unscale, unoffset, round-nearest
        return Vector3.at(x, y, z).multiply(unit).add(zero2).toBlockPoint();
    }

    public Vector3 toWorldRel(double x, double y, double z) {
        return current.add(x, y, z);
    }

    @Override
    public int getBlockType(double x, double y, double z) {
        return editSession.getBlock(toWorld(x, y, z)).getBlockType().getLegacyId();
    }

    @Override
    public int getBlockData(double x, double y, double z) {
        return 0;
    }

    @Override
    public int getBlockTypeAbs(double x, double y, double z) {
        return editSession.getBlock(BlockVector3.at(x, y, z)).getBlockType().getLegacyId();
    }

    @Override
    public int getBlockDataAbs(double x, double y, double z) {
        return 0;
    }

    @Override
    public int getBlockTypeRel(double x, double y, double z) {
        return editSession.getBlock(toWorldRel(x, y, z).toBlockPoint()).getBlockType().getLegacyId();
    }

    @Override
    public int getBlockDataRel(double x, double y, double z) {
        return 0;
    }

    public void setCurrentBlock(Vector3 current) {
        this.current = current;
    }

}
