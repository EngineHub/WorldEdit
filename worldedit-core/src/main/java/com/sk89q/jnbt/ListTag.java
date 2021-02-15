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

import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagLike;
import com.sk89q.worldedit.util.nbt.ListBinaryTag;
import com.sk89q.worldedit.util.nbt.NumberBinaryTag;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The {@code TAG_List} tag.
 *
 * @deprecated Use {@link com.sk89q.worldedit.util.nbt.ListBinaryTag}.
 */
@Deprecated
public final class ListTag extends Tag {

    private final ListBinaryTag innerTag;

    /**
     * Creates the tag with an empty name.
     *
     * @param type the type of tag
     * @param value the value of the tag
     */
    public ListTag(Class<? extends Tag> type, List<? extends Tag> value) {
        this(ListBinaryTag.of(
            AdventureNBTConverter.getAdventureType(type),
            value.stream().map(BinaryTagLike::asBinaryTag).collect(Collectors.toList())
        ));
    }

    public ListTag(ListBinaryTag adventureTag) {
        this.innerTag = adventureTag;
    }

    @Override
    public ListBinaryTag asBinaryTag() {
        return this.innerTag;
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return AdventureNBTConverter.getJNBTType(this.innerTag.elementType());
    }

    @Override
    public List<Tag> getValue() {
        return this.innerTag.stream()
            .map(AdventureNBTConverter::fromAdventure)
            .collect(Collectors.toList());
    }

    /**
     * Create a new list tag with this tag's name and type.
     *
     * @param list the new list
     * @return a new list tag
     */
    public ListTag setValue(List<Tag> list) {
        return new ListTag(getType(), list);
    }

    private <T> T accessIfExists(int index, Supplier<T> defaultValue, IntFunction<T> accessor) {
        if (index >= this.innerTag.size()) {
            return defaultValue.get();
        }
        return accessor.apply(index);
    }

    /**
     * Get the tag if it exists at the given index.
     *
     * @param index the index
     * @return the tag or null
     */
    @Nullable
    public Tag getIfExists(int index) {
        return accessIfExists(
            index,
            () -> null,
            i -> AdventureNBTConverter.fromAdventure(this.innerTag.get(i))
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
        return accessIfExists(
            index,
            () -> new byte[0],
            this.innerTag::getByteArray
        );
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
        return accessIfExists(
            index,
            () -> (byte) 0,
            this.innerTag::getByte
        );
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
        return accessIfExists(
            index,
            () -> 0.0,
            this.innerTag::getDouble
        );
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
            i -> {
                BinaryTag tag = this.innerTag.get(i);
                if (tag instanceof NumberBinaryTag) {
                    return ((NumberBinaryTag) tag).doubleValue();
                }
                return 0.0;
            }
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
        return accessIfExists(
            index,
            () -> 0.0f,
            this.innerTag::getFloat
        );
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
        return accessIfExists(
            index,
            () -> new int[0],
            this.innerTag::getIntArray
        );
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
        return accessIfExists(
            index,
            () -> 0,
            this.innerTag::getInt
        );
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
            i -> {
                BinaryTag tag = this.innerTag.get(i);
                if (tag instanceof NumberBinaryTag) {
                    return ((NumberBinaryTag) tag).intValue();
                }
                return 0;
            }
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
    public List<Tag> getList(int index) {
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
    public ListTag getListTag(int index) {
        return new ListTag(accessIfExists(
            index,
            ListBinaryTag::empty,
            this.innerTag::getList
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
     * @return a list of tags
     * @param <T> the NBT type
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(int index, Class<T> listType) {
        ListTag listTag = getListTag(index);
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
        return accessIfExists(
            index,
            () -> 0L,
            this.innerTag::getLong
        );
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
            i -> {
                BinaryTag tag = this.innerTag.get(i);
                if (tag instanceof NumberBinaryTag) {
                    return ((NumberBinaryTag) tag).longValue();
                }
                return 0L;
            }
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
        return accessIfExists(
            index,
            () -> (short) 0,
            this.innerTag::getShort
        );
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
        return accessIfExists(
            index,
            () -> "",
            this.innerTag::getString
        );
    }

}
