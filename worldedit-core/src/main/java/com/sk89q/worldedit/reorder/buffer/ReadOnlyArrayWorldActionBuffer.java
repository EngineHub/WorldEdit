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

public class ReadOnlyArrayWorldActionBuffer extends SharedArrayWorldActionBuffer implements ReadOnlyWorldActionBuffer {

    ReadOnlyArrayWorldActionBuffer(WorldAction[] array, int offset, int capacity) {
        super(array, offset, capacity);
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer duplicate() {
        return new ReadOnlyArrayWorldActionBuffer(
            array, offset, capacity
        ).limit(limit).position(position);
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer slice() {
        return new ReadOnlyArrayWorldActionBuffer(
            array, offset + position, remaining()
        );
    }

    // Return value overrides:

    @Override
    public ReadOnlyArrayWorldActionBuffer clear() {
        ReadOnlyWorldActionBuffer.super.clear();
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer flip() {
        ReadOnlyWorldActionBuffer.super.flip();
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer rewind() {
        ReadOnlyWorldActionBuffer.super.rewind();
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer get(WorldAction[] out) {
        ReadOnlyWorldActionBuffer.super.get(out);
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer position(int position) {
        super.position(position);
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer limit(int limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public ReadOnlyArrayWorldActionBuffer get(WorldAction[] out, int offset, int length) {
        super.get(out, offset, length);
        return this;
    }
}
