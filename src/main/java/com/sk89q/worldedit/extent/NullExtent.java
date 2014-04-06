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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.operation.Operation;

import javax.annotation.Nullable;

/**
 * An extent that returns air blocks for all blocks and does not
 * pass on any changes.
 */
public class NullExtent implements Extent {

    private final Vector nullPoint = new Vector(0, 0, 0);

    @Override
    public Vector getMinimumPoint() {
        return nullPoint;
    }

    @Override
    public Vector getMaximumPoint() {
        return nullPoint;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return new BaseBlock(0);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return new BaseBlock(0);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        return false;
    }

    @Nullable
    @Override
    public Operation commit() {
        return null;
    }

}
