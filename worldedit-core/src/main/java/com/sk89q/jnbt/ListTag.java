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

import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The {@code TAG_List} tag.
 *
 * @deprecated Use {@link LinListTag}.
 */
@Deprecated
public final class ListTag<EV, E extends LinTag<EV>> extends Tag<Object, LinListTag<E>> {
    /**
     * Creates the tag with an empty name.
     *
     * @param type the type of tag
     * @param value the value of the tag
     */
    public ListTag(Class<? extends Tag<EV, E>> type, List<? extends Tag<EV, E>> value) {
        this(LinListTag.of(
            LinTagType.fromId(LinTagId.fromId(NBTUtils.getTypeCode(type))),
            value.stream().map(Tag::toLinTag).collect(Collectors.toList())
        ));
    }

    public ListTag(LinListTag<E> tag) {
        super(tag);
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Tag<EV, E>> getType() {
        return (Class<? extends Tag<EV, E>>) NBTUtils.getTypeClass(linTag.elementType().id().id());
    }

    @Override
    public List<? extends Tag<EV, E>> getValue() {
        return linTag.value().stream().map(LinBusConverter::toJnbtTag).toList();
    }

    /**
     * Create a new list tag with this tag's name and type.
     *
     * @param list the new list
     * @return a new list tag
     */
    public ListTag<EV, E> setValue(List<? extends Tag<EV, E>> list) {
        return new ListTag<>(getType(), list);
    }

    private <T> T accessIfExists(int index, Supplier<T> defaultValue, IntFunction<T> accessor) {
        if (index >= this.linTag.value().size()) {
            return defaultValue.get();
        }
        return accessor.apply(index);
    }

    @SuppressWarnings("unchecked")
    private <T, LT extends LinTag<T>> T extractViaValue(
        int index, Class<LT> requiredType, Supplier<T> defaultValue
    ) {
        if (index >= this.linTag.value().size()) {
            return defaultValue.get();
        }
        E value = this.linTag.get(index);
        if (!requiredType.isInstance(value)) {
            return defaultValue.get();
        }
        return (T) value.value();
    }

    /**
     * Get the tag if it exists at the given index.
     *
     * @param index the index
     * @return the tag or null
     */
    @Nullable
    public Tag<EV, E> getIfExists(int index) {
        return accessIfExists(
            index,
            () -> null,
            i -> LinBusConverter.toJnbtTag(this.linTag.get(i))
        );
    }

    /**
     * Get a byte array named with the given index.
     *
     * <p>If the index does not exist or its value is not a byte array tag,
     * then an empty byte array will be returned.</p>
     *
     * @param index the index
     * @return a byte array
     */
    public byte[] getByteArray(int index) {
        return extractViaValue(index, LinByteArrayTag.class, () -> new byte[0]);
    }

    /**
     * Get a byte named with the given index.
     *
     * <p>If the index does not exist or its value is not a byte tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a byte
     */
    public byte getByte(int index) {
        return extractViaValue(index, LinByteTag.class, () -> (byte) 0);
    }

    /**
     * Get a double named with the given index.
     *
     * <p>If the index does not exist or its value is not a double tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a double
     */
    public double getDouble(int index) {
        return extractViaValue(index, LinDoubleTag.class, () -> 0.0);
    }

    /**
     * Get a double named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a double
     */
    public double asDouble(int index) {
        return accessIfExists(
            index,
            () -> 0.0,
            i -> this.linTag.get(i) instanceof LinNumberTag<?> tag
                ? tag.value().doubleValue()
                : 0.0
        );
    }

    /**
     * Get a float named with the given index.
     *
     * <p>If the index does not exist or its value is not a float tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a float
     */
    public float getFloat(int index) {
        return extractViaValue(index, LinFloatTag.class, () -> 0f);
    }

    /**
     * Get a {@code int[]} named with the given index.
     *
     * <p>If the index does not exist or its value is not an int array tag,
     * then an empty array will be returned.</p>
     *
     * @param index the index
     * @return an int array
     */
    public int[] getIntArray(int index) {
        return extractViaValue(index, LinIntArrayTag.class, () -> new int[0]);
    }

    /**
     * Get an int named with the given index.
     *
     * <p>If the index does not exist or its value is not an int tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return an int
     */
    public int getInt(int index) {
        return extractViaValue(index, LinIntTag.class, () -> 0);
    }

    /**
     * Get an int named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return an int
     */
    public int asInt(int index) {
        return accessIfExists(
            index,
            () -> 0,
            i -> this.linTag.get(i) instanceof LinNumberTag<?> tag
                ? tag.value().intValue()
                : 0
        );
    }

    /**
     * Get a list of tags named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty list will be returned.</p>
     *
     * @param index the index
     * @return a list of tags
     */
    public List<? extends Tag<?, ?>> getList(int index) {
        return getListTag(index).getValue();
    }

    /**
     * Get a {@code TagList} named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty tag list will be returned.</p>
     *
     * @param index the index
     * @return a tag list instance
     */
    @SuppressWarnings("unchecked")
    public ListTag<?, ?> getListTag(int index) {
        return new ListTag<>(extractViaValue(
            index,
            LinListTag.class,
            () -> LinListTag.empty(LinTagType.endTag())
        ));
    }

    /**
     * Get a list of tags named with the given index.
     *
     * <p>If the index does not exist or its value is not a list tag,
     * then an empty list will be returned. If the given index references
     * a list but the list of of a different type, then an empty
     * list will also be returned.</p>
     *
     * @param index the index
     * @param listType the class of the contained type
     * @param <T> the NBT type
     * @return a list of tags
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag<?, ?>> List<T> getList(int index, Class<T> listType) {
        ListTag<?, ?> listTag = getListTag(index);
        if (listTag.getType().equals(listType)) {
            return (List<T>) listTag.getValue();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a long named with the given index.
     *
     * <p>If the index does not exist or its value is not a long tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a long
     */
    public long getLong(int index) {
        return extractViaValue(index, LinLongTag.class, () -> 0L);
    }

    /**
     * Get a long named with the given index, even if it's another
     * type of number.
     *
     * <p>If the index does not exist or its value is not a number,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a long
     */
    public long asLong(int index) {
        return accessIfExists(
            index,
            () -> 0L,
            i -> this.linTag.get(i) instanceof LinNumberTag<?> tag
                ? tag.value().longValue()
                : 0L
        );
    }

    /**
     * Get a short named with the given index.
     *
     * <p>If the index does not exist or its value is not a short tag,
     * then {@code 0} will be returned.</p>
     *
     * @param index the index
     * @return a short
     */
    public short getShort(int index) {
        return extractViaValue(index, LinShortTag.class, () -> (short) 0);
    }

    /**
     * Get a string named with the given index.
     *
     * <p>If the index does not exist or its value is not a string tag,
     * then {@code ""} will be returned.</p>
     *
     * @param index the index
     * @return a string
     */
    public String getString(int index) {
        return extractViaValue(index, LinStringTag.class, () -> "");
    }

}
