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

import com.sk89q.worldedit.action.WorldAction;

import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkBounds;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkIndex;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkReadPosition;
import static com.sk89q.worldedit.reorder.buffer.BufferConditions.checkWithin;

/**
 * Shared implementation between mutable + read-only implementation.
 */
abstract class SharedArrayWorldActionBuffer implements WorldActionBuffer {
    protected final WorldAction[] array;
    protected final int offset;
    protected final int capacity;
    protected int position;
    protected int limit;

    SharedArrayWorldActionBuffer(WorldAction[] array, int offset, int capacity) {
        this.array = array;
        this.offset = offset;
        this.capacity = capacity;
        this.limit = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public int position() {
        return position;
    }

    public SharedArrayWorldActionBuffer position(int position) {
        this.position = checkWithin(position, limit);
        return this;
    }

    public int limit() {
        return limit;
    }

    public SharedArrayWorldActionBuffer limit(int limit) {
        this.limit = checkWithin(limit, capacity);
        return this;
    }

    public WorldAction get() {
        return array[checkReadPosition(position++, limit) + offset];
    }

    public WorldAction get(int index) {
        return array[checkIndex(index, limit) + offset];
    }

    public SharedArrayWorldActionBuffer get(WorldAction[] out, int offset, int length) {
        checkBounds(out.length, offset, length);
        checkReadPosition(length, remaining());
        System.arraycopy(array, position + this.offset,
            out, offset, length);
        position += length;
        return this;
    }
}
