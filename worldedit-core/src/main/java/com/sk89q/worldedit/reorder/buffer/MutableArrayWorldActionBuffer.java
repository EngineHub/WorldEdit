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

import com.sk89q.worldedit.action.BlockPlacement;
import com.sk89q.worldedit.action.WorldAction;

import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkBounds;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkIndex;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkWritePosition;

public class MutableArrayWorldActionBuffer extends SharedArrayWorldActionBuffer implements MutableWorldActionBuffer {

    public static MutableArrayWorldActionBuffer allocate(int size) {
        return wrap(new BlockPlacement[size]);
    }

    public static MutableArrayWorldActionBuffer wrap(WorldAction[] blocks) {
        return new MutableArrayWorldActionBuffer(blocks, 0, blocks.length);
    }

    public static MutableArrayWorldActionBuffer wrap(WorldAction[] blocks, int offset, int length) {
        return new MutableArrayWorldActionBuffer(blocks, 0, blocks.length)
            .position(offset)
            .limit(length);
    }

    private MutableArrayWorldActionBuffer(WorldAction[] array, int offset, int capacity) {
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
    public WorldAction[] array() {
        return array;
    }

    @Override
    public int arrayOffset() {
        return offset;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer asReadOnlyBuffer() {
        return new ReadOnlyArrayWorldActionBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public MutableArrayWorldActionBuffer put(WorldAction worldAction) {
        array[checkWritePosition(position++, limit) + offset] = worldAction;
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer put(WorldAction[] worldActions, int offset, int length) {
        checkBounds(worldActions.length, offset, length);
        checkWritePosition(length, remaining());
        System.arraycopy(worldActions, offset,
            array, position + this.offset, length);
        position += length;
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer put(WorldActionBuffer buffer) {
        int remaining = buffer.remaining();
        checkWritePosition(remaining, remaining());
        if (buffer instanceof MutableWorldActionBuffer) {
            MutableWorldActionBuffer mut = (MutableWorldActionBuffer) buffer;
            if (mut.hasArray()) {
                WorldAction[] oa = mut.array();
                int oo = mut.arrayOffset();
                System.arraycopy(oa, buffer.position() + oo,
                    array, position + offset, remaining);
                mut.position(buffer.position() + remaining);
                position += remaining;
                return this;
            }
        }
        buffer.get(array, position + offset, remaining);
        position += remaining;
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer put(int index, WorldAction worldAction) {
        array[checkIndex(index, limit) + offset] = worldAction;
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer duplicate() {
        return new MutableArrayWorldActionBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public MutableArrayWorldActionBuffer slice() {
        return new MutableArrayWorldActionBuffer(
            array, offset + position, remaining()
        );
    }

    // Return value overrides:

    @Override
    public MutableArrayWorldActionBuffer put(WorldAction[] worldAction) {
        MutableWorldActionBuffer.super.put(worldAction);
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer clear() {
        MutableWorldActionBuffer.super.clear();
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer flip() {
        MutableWorldActionBuffer.super.flip();
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer rewind() {
        MutableWorldActionBuffer.super.rewind();
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer get(WorldAction[] out) {
        MutableWorldActionBuffer.super.get(out);
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer position(int position) {
        super.position(position);
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public MutableArrayWorldActionBuffer get(WorldAction[] out, int offset, int length) {
        super.get(out, offset, length);
        return this;
    }
}
