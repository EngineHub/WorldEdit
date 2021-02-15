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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagLike;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.NumberBinaryTag;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The {@code TAG_Compound} tag.
 *
 * @deprecated Use {@link com.sk89q.worldedit.util.nbt.CompoundBinaryTag}.
 */
@Deprecated
public final class CompoundTag extends Tag {

    private final CompoundBinaryTag innerTag;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public CompoundTag(Map<String, Tag> value) {
        this(CompoundBinaryTag.builder()
            .put(Maps.transformValues(value, BinaryTagLike::asBinaryTag))
            .build());
    }

    public CompoundTag(CompoundBinaryTag adventureTag) {
        this.innerTag = adventureTag;
    }

    /**
     * Returns whether this compound tag contains the given key.
     *
     * @param key the given key
     * @return true if the tag contains the given key
     */
    public boolean containsKey(String key) {
        return innerTag.keySet().contains(key);
    }

    @Override
    public Map<String, Tag> getValue() {
        ImmutableMap.Builder<String, Tag> map = ImmutableMap.builder();
        for (String key : innerTag.keySet()) {
            map.put(key, AdventureNBTConverter.fromAdventure(innerTag.get(key)));
        }
        return map.build();
    }

    /**
     * Return a new compound tag with the given values.
     *
     * @param value the value
     * @return the new compound tag
     */
    public CompoundTag setValue(Map<String, Tag> value) {
        return new CompoundTag(value);
    }

    /**
     * Create a compound tag builder.
     *
     * @return the builder
     */
    public CompoundTagBuilder createBuilder() {
        return new CompoundTagBuilder(innerTag);
    }

    /**
     * Get a byte array named with the given key.
     *
     * <p>If the key does not exist or its value is not a byte array tag,
     * then an empty byte array will be returned.</p>
     *
     * @param key the key
     * @return a byte array
     */
    public byte[] getByteArray(String key) {
        return this.innerTag.getByteArray(key);
    }

    /**
     * Get a byte named with the given key.
     *
     * <p>If the key does not exist or its value is not a byte tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a byte
     */
    public byte getByte(String key) {
        return this.innerTag.getByte(key);
    }

    /**
     * Get a double named with the given key.
     *
     * <p>If the key does not exist or its value is not a double tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a double
     */
    public double getDouble(String key) {
        return this.innerTag.getDouble(key);
    }

    /**
     * Get a double named with the given key, even if it's another
     * type of number.
     *
     * <p>If the key does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a double
     */
    public double asDouble(String key) {
        BinaryTag tag = this.innerTag.get(key);
        if (tag instanceof NumberBinaryTag) {
            return ((NumberBinaryTag) tag).doubleValue();
        }
        return 0;
    }

    /**
     * Get a float named with the given key.
     *
     * <p>If the key does not exist or its value is not a float tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a float
     */
    public float getFloat(String key) {
        return this.innerTag.getFloat(key);
    }

    /**
     * Get a {@code int[]} named with the given key.
     *
     * <p>If the key does not exist or its value is not an int array tag,
     * then an empty array will be returned.</p>
     *
     * @param key the key
     * @return an int array
     */
    public int[] getIntArray(String key) {
        return this.innerTag.getIntArray(key);
    }

    /**
     * Get an int named with the given key.
     *
     * <p>If the key does not exist or its value is not an int tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return an int
     */
    public int getInt(String key) {
        return this.innerTag.getInt(key);
    }

    /**
     * Get an int named with the given key, even if it's another
     * type of number.
     *
     * <p>If the key does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return an int
     */
    public int asInt(String key) {
        BinaryTag tag = this.innerTag.get(key);
        if (tag instanceof NumberBinaryTag) {
            return ((NumberBinaryTag) tag).intValue();
        }
        return 0;
    }

    /**
     * Get a list of tags named with the given key.
     *
     * <p>If the key does not exist or its value is not a list tag,
     * then an empty list will be returned.</p>
     *
     * @param key the key
     * @return a list of tags
     */
    public List<Tag> getList(String key) {
        return getListTag(key).getValue();
    }

    /**
     * Get a {@code TagList} named with the given key.
     *
     * <p>If the key does not exist or its value is not a list tag,
     * then an empty tag list will be returned.</p>
     *
     * @param key the key
     * @return a tag list instance
     */
    public ListTag getListTag(String key) {
        return new ListTag(this.innerTag.getList(key));
    }

    /**
     * Get a list of tags named with the given key.
     *
     * <p>If the key does not exist or its value is not a list tag,
     * then an empty list will be returned. If the given key references
     * a list but the list of of a different type, then an empty
     * list will also be returned.</p>
     *
     * @param key the key
     * @param listType the class of the contained type
     * @return a list of tags
     * @param <T> the type of list
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(String key, Class<T> listType) {
        ListTag listTag = getListTag(key);
        if (listTag.getType().equals(listType)) {
            return (List<T>) listTag.getValue();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a {@code long[]} named with the given key.
     *
     * <p>If the key does not exist or its value is not an long array tag,
     * then an empty array will be returned.</p>
     *
     * @param key the key
     * @return an int array
     */
    public long[] getLongArray(String key) {
        return this.innerTag.getLongArray(key);
    }

    /**
     * Get a long named with the given key.
     *
     * <p>If the key does not exist or its value is not a long tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a long
     */
    public long getLong(String key) {
        return this.innerTag.getLong(key);
    }

    /**
     * Get a long named with the given key, even if it's another
     * type of number.
     *
     * <p>If the key does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a long
     */
    public long asLong(String key) {
        BinaryTag tag = this.innerTag.get(key);
        if (tag instanceof NumberBinaryTag) {
            return ((NumberBinaryTag) tag).longValue();
        }
        return 0;
    }

    /**
     * Get a short named with the given key.
     *
     * <p>If the key does not exist or its value is not a short tag,
     * then {@code 0} will be returned.</p>
     *
     * @param key the key
     * @return a short
     */
    public short getShort(String key) {
        return this.innerTag.getShort(key);
    }

    /**
     * Get a string named with the given key.
     *
     * <p>If the key does not exist or its value is not a string tag,
     * then {@code ""} will be returned.</p>
     *
     * @param key the key
     * @return a string
     */
    public String getString(String key) {
        return this.innerTag.getString(key);
    }

    @Override
    public CompoundBinaryTag asBinaryTag() {
        return this.innerTag;
    }
}
