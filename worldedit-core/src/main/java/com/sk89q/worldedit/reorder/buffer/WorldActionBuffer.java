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

import java.nio.ByteBuffer;

/**
 * Similar to {@link ByteBuffer}, but for world actions.
 *
 * Many of the conditions for arguments are the same as with {@link ByteBuffer}.
 */
public interface WorldActionBuffer {

    /**
     * The maximum number of elements this buffer can hold.
     */
    int capacity();

    /**
     * The current position of the buffer.
     */
    int position();

    /**
     * Set the current position to {@code position}.
     *
     * It must be non-negative and no greater than the current {@linkplain #limit() limit}.
     *
     * @param position the new position
     * @return {@code this}
     */
    WorldActionBuffer position(int position);

    /**
     * The current limit of the buffer.
     */
    int limit();

    /**
     * Set the current limit to {@code limit}.
     *
     * It must be non-negative and no greater than the {@linkplain #capacity() capacity}.
     *
     * @param limit the new limit
     * @return {@code this}
     */
    WorldActionBuffer limit(int limit);

    /**
     * Set the position to zero and the limit to the {@linkplain #capacity() capacity}.
     *
     * @return {@code this}
     */
    default WorldActionBuffer clear() {
        position(0);
        limit(capacity());
        return this;
    }

    /**
     * Set the position to zero and the limit to the original position.
     *
     * @return {@code this}
     */
    default WorldActionBuffer flip() {
        limit(position());
        position(0);
        return this;
    }

    /**
     * Set the position to zero.
     *
     * @return {@code this}
     */
    default WorldActionBuffer rewind() {
        position(0);
        return this;
    }

    /**
     * The number of elements remaining in the buffer.
     */
    default int remaining() {
        return limit() - position();
    }

    /**
     * {@code true} if the buffer has elements remaining in the buffer.
     */
    default boolean hasRemaining() {
        return position() < limit();
    }

    /**
     * {@code true} if the buffer is read-only.
     *
     * Note: If this returns {@code true}, it is NOT safe to cast this to
     * {@link ReadOnlyWorldActionBuffer}. That interface is currently only a convenience for
     * implementors, not a required interface.
     */
    boolean isReadOnly();

    /**
     * Create a new read-only <em>view</em> of this buffer. Changes to this buffer are reflected
     * in the new buffer.
     *
     * If this buffer is {@linkplain #isReadOnly() read-only}, this is equivalent to
     * {@link #duplicate()}.
     */
    ReadOnlyWorldActionBuffer asReadOnlyBuffer();

    /**
     * Relative {@code get} method. Increments position by one after retrieving the element.
     *
     * @return the element
     */
    WorldAction get();

    /**
     * Absolute {@code get} method.
     *
     * @param index the index of the element to retrieve
     * @return the element
     */
    WorldAction get(int index);

    /**
     * Relative bulk {@code get} method. Increments position by {@code out.length} after copying
     * the elements.
     *
     * @param out the output array
     * @return {@code this}
     */
    default WorldActionBuffer get(WorldAction[] out) {
        return get(out, 0, out.length);
    }

    /**
     * Relative bulk {@code get} method. Increments position by {@code length} after copying
     * the elements.
     *
     * @param out the output array
     * @return {@code this}
     */
    WorldActionBuffer get(WorldAction[] out, int offset, int length);

    /**
     * Duplicate this buffer. Returns a new buffer with the same capacity, limit, and position.
     * Changes to each buffer's content will be visible in the other.
     *
     * @return the new buffer
     */
    WorldActionBuffer duplicate();

    /**
     * Slice this buffer. Returns a new buffer with a capacity and limit equal to
     * {@linkplain #remaining() the number of remaining elements}, and a position of zero.
     * Changes to each buffer's content will be visible in the other.
     *
     * @return the new buffer
     */
    WorldActionBuffer slice();

}
