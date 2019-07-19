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

import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkBounds;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkIndex;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkWritePosition;

public class MutableArrayPlacementBuffer extends SharedArrayPlacementBuffer implements MutablePlacementBuffer {

    public static MutableArrayPlacementBuffer allocate(int size) {
        return wrap(new LocatedBlock[size]);
    }

    public static MutableArrayPlacementBuffer wrap(LocatedBlock[] blocks) {
        return new MutableArrayPlacementBuffer(blocks, 0, blocks.length);
    }

    public static MutableArrayPlacementBuffer wrap(LocatedBlock[] blocks, int offset, int length) {
        return new MutableArrayPlacementBuffer(blocks, 0, blocks.length)
            .position(offset)
            .limit(length);
    }

    private MutableArrayPlacementBuffer(LocatedBlock[] array, int offset, int capacity) {
        super(array, offset, capacity);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public LocatedBlock[] array() {
        return array;
    }

    @Override
    public int arrayOffset() {
        return offset;
    }

    @Override
    public ReadOnlyArrayPlacementBuffer asReadOnlyBuffer() {
        return new ReadOnlyArrayPlacementBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public MutableArrayPlacementBuffer put(LocatedBlock placement) {
        array[checkWritePosition(position++, limit) + offset] = placement;
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer put(LocatedBlock[] placements, int offset, int length) {
        checkBounds(placements.length, offset, length);
        checkWritePosition(length, remaining());
        System.arraycopy(placements, offset,
            array, position + this.offset, length);
        position += length;
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer put(PlacementBuffer placements) {
        int remaining = placements.remaining();
        checkWritePosition(remaining, remaining());
        if (placements instanceof MutablePlacementBuffer) {
            MutablePlacementBuffer mut = (MutablePlacementBuffer) placements;
            if (mut.hasArray()) {
                LocatedBlock[] oa = mut.array();
                int oo = mut.arrayOffset();
                System.arraycopy(oa, placements.position() + oo,
                    array, position + offset, remaining);
                mut.position(placements.position() + remaining);
                position += remaining;
                return this;
            }
        }
        placements.get(array, position + offset, remaining);
        position += remaining;
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer put(int index, LocatedBlock placement) {
        array[checkIndex(index, limit) + offset] = placement;
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer duplicate() {
        return new MutableArrayPlacementBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public MutableArrayPlacementBuffer slice() {
        return new MutableArrayPlacementBuffer(
            array, offset + position, remaining()
        );
    }

    // Return value overrides:

    @Override
    public MutableArrayPlacementBuffer put(LocatedBlock[] placement) {
        MutablePlacementBuffer.super.put(placement);
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer clear() {
        MutablePlacementBuffer.super.clear();
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer flip() {
        MutablePlacementBuffer.super.flip();
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer rewind() {
        MutablePlacementBuffer.super.rewind();
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer get(LocatedBlock[] out) {
        MutablePlacementBuffer.super.get(out);
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer position(int position) {
        super.position(position);
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public MutableArrayPlacementBuffer get(LocatedBlock[] out, int offset, int length) {
        super.get(out, offset, length);
        return this;
    }
}
