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

package com.sk89q.jnbt;

import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps create compound tags.
 *
 * @deprecated Use {@link com.sk89q.worldedit.util.nbt.CompoundBinaryTag.Builder}.
 */
@Deprecated
public class CompoundTagBuilder {

    private final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

    /**
     * Create a new instance.
     */
    CompoundTagBuilder() {
    }

    /**
     * Create a new instance and use the given map (which will be modified).
     *
     * @param source the value
     */
    CompoundTagBuilder(CompoundBinaryTag source) {
        checkNotNull(source);
        for (String key : source.keySet()) {
            this.builder.put(key, Objects.requireNonNull(source.get(key)));
        }
    }

    /**
     * Put the given key and tag into the compound tag.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder put(String key, Tag value) {
        checkNotNull(key);
        checkNotNull(value);
        this.builder.put(key, value.asBinaryTag());
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ByteArrayTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByteArray(String key, byte[] value) {
        this.builder.putByteArray(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ByteTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByte(String key, byte value) {
        this.builder.putByte(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code DoubleTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putDouble(String key, double value) {
        this.builder.putDouble(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code FloatTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putFloat(String key, float value) {
        this.builder.putFloat(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code IntArrayTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putIntArray(String key, int[] value) {
        this.builder.putIntArray(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as an {@code IntTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putInt(String key, int value) {
        this.builder.putInt(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code LongArrayTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putLongArray(String key, long[] value) {
        this.builder.putLongArray(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code LongTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putLong(String key, long value) {
        this.builder.putLong(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ShortTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putShort(String key, short value) {
        this.builder.putShort(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code StringTag}.
     *
     * @param key they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putString(String key, String value) {
        this.builder.putString(key, value);
        return this;
    }

    /**
     * Remove the given key from the compound tag. Does nothing if the key doesn't exist.
     *
     * @param key the key
     * @return this object
     */
    public CompoundTagBuilder remove(String key) {
        checkNotNull(key);
        this.builder.remove(key);
        return this;
    }

    /**
     * Put all the entries from the given map into this map.
     *
     * @param value the map of tags
     * @return this object
     */
    public CompoundTagBuilder putAll(Map<String, ? extends Tag> value) {
        checkNotNull(value);
        for (Map.Entry<String, ? extends Tag> entry : value.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Build an unnamed compound tag with this builder's entries.
     *
     * @return the new compound tag
     */
    public CompoundTag build() {
        return new CompoundTag(this.builder.build());
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static CompoundTagBuilder create() {
        return new CompoundTagBuilder();
    }

}
