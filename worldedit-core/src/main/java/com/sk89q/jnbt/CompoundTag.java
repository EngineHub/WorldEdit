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
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The {@code TAG_Compound} tag.
 *
 * @deprecated Use {@link LinCompoundTag}.
 */
@Deprecated
public final class CompoundTag extends Tag<Object, LinCompoundTag> {
    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public CompoundTag(Map<String, Tag<?, ?>> value) {
        this(LinCompoundTag.of(Maps.transformValues(value, Tag::toLinTag)));
    }

    public CompoundTag(LinCompoundTag tag) {
        super(tag);
    }

    /**
     * Returns whether this compound tag contains the given key.
     *
     * @param key the given key
     * @return true if the tag contains the given key
     */
    public boolean containsKey(String key) {
        return linTag.value().containsKey(key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, Tag<?, ?>> getValue() {
        return ImmutableMap.copyOf(Maps.transformValues(
            linTag.value(),
            tag -> (Tag<?, ?>) AdventureNBTConverter.toJnbtTag((LinTag) tag)
        ));
    }

    /**
     * Return a new compound tag with the given values.
     *
     * @param value the value
     * @return the new compound tag
     */
    public CompoundTag setValue(Map<String, Tag<?, ?>> value) {
        return new CompoundTag(value);
    }

    /**
     * Create a compound tag builder.
     *
     * @return the builder
     */
    public CompoundTagBuilder createBuilder() {
        return new CompoundTagBuilder(linTag);
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
        var tag = linTag.findTag(key, LinTagType.byteArrayTag());
        return tag == null ? new byte[0] : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.byteTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.doubleTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.value().get(key);
        if (tag instanceof LinNumberTag<?> numberTag) {
            Number value = numberTag.value();
            return value.doubleValue();
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
        var tag = linTag.findTag(key, LinTagType.floatTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.intArrayTag());
        return tag == null ? new int[0] : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.intTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.value().get(key);
        if (tag instanceof LinNumberTag<?> numberTag) {
            Number value = numberTag.value();
            return value.intValue();
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
    public List<? extends Tag<?, ?>> getList(String key) {
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
    public <EV, E extends LinTag<EV>> ListTag<EV, E> getListTag(String key) {
        LinListTag<E> tag = linTag.findTag(key, LinTagType.listTag());
        if (tag == null) {
            // This is actually hella unsafe. But eh.
            @SuppressWarnings("unchecked")
            LinTagType<E> endGenerically = (LinTagType<E>) LinTagType.endTag();
            return new ListTag<>(LinListTag.empty(endGenerically));
        }
        return new ListTag<>(tag);
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
     * @param <T> the type of list
     * @return a list of tags
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag<?, ?>> List<T> getList(String key, Class<T> listType) {
        ListTag<?, ?> listTag = getListTag(key);
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
        var tag = linTag.findTag(key, LinTagType.longArrayTag());
        return tag == null ? new long[0] : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.longTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.value().get(key);
        if (tag instanceof LinNumberTag<?> numberTag) {
            Number value = numberTag.value();
            return value.longValue();
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
        var tag = linTag.findTag(key, LinTagType.shortTag());
        return tag == null ? 0 : tag.value();
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
        var tag = linTag.findTag(key, LinTagType.stringTag());
        return tag == null ? "" : tag.value();
    }
}
