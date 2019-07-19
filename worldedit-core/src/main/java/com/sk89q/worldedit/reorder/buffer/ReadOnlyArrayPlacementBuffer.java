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

package com.sk89q.worldedit.reorder.buffer;

import com.sk89q.worldedit.util.LocatedBlock;

public class ReadOnlyArrayPlacementBuffer extends SharedArrayPlacementBuffer implements ReadOnlyPlacementBuffer {

    ReadOnlyArrayPlacementBuffer(LocatedBlock[] array, int offset, int capacity) {
        super(array, offset, capacity);
    }

    @Override
    public ReadOnlyArrayPlacementBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    @Override
    public ReadOnlyArrayPlacementBuffer duplicate() {
        return new ReadOnlyArrayPlacementBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public ReadOnlyArrayPlacementBuffer slice() {
        return new ReadOnlyArrayPlacementBuffer(
            array, offset + position, remaining()
        );
    }

    // Return value overrides:

    @Override
    public ReadOnlyArrayPlacementBuffer clear() {
        ReadOnlyPlacementBuffer.super.clear();
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer flip() {
        ReadOnlyPlacementBuffer.super.flip();
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer rewind() {
        ReadOnlyPlacementBuffer.super.rewind();
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer get(LocatedBlock[] out) {
        ReadOnlyPlacementBuffer.super.get(out);
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer position(int position) {
        super.position(position);
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer get(LocatedBlock[] out, int offset, int length) {
        super.get(out, offset, length);
        return this;
    }
}
