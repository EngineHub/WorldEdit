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

import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A class which contains NBT-related utility methods.
 *
 * @deprecated JNBT is being removed for adventure-nbt in WorldEdit 8.
 */
@Deprecated
public final class NBTUtils {

    /**
     * Default private constructor.
     */
    private NBTUtils() {
    }

    /**
     * Gets the type name of a tag.
     *
     * @param clazz the tag class
     * @return The type name.
     */
    public static String getTypeName(Class<? extends Tag<?, ?>> clazz) {
        if (clazz.equals(ByteArrayTag.class)) {
            return "TAG_Byte_Array";
        } else if (clazz.equals(ByteTag.class)) {
            return "TAG_Byte";
        } else if (clazz.equals(CompoundTag.class)) {
            return "TAG_Compound";
        } else if (clazz.equals(DoubleTag.class)) {
            return "TAG_Double";
        } else if (clazz.equals(EndTag.class)) {
            return "TAG_End";
        } else if (clazz.equals(FloatTag.class)) {
            return "TAG_Float";
        } else if (clazz.equals(IntTag.class)) {
            return "TAG_Int";
        } else if (clazz.equals(ListTag.class)) {
            return "TAG_List";
        } else if (clazz.equals(LongTag.class)) {
            return "TAG_Long";
        } else if (clazz.equals(ShortTag.class)) {
            return "TAG_Short";
        } else if (clazz.equals(StringTag.class)) {
            return "TAG_String";
        } else if (clazz.equals(IntArrayTag.class)) {
            return "TAG_Int_Array";
        } else if (clazz.equals(LongArrayTag.class)) {
            return "TAG_Long_Array";
        } else {
            throw new IllegalArgumentException("Invalid tag class ("
                + clazz.getName() + ").");
        }
    }

    /**
     * Gets the type code of a tag class.
     *
     * @param clazz the tag class
     * @return The type code.
     * @throws IllegalArgumentException if the tag class is invalid.
     */
    public static int getTypeCode(Class<? extends Tag<?, ?>> clazz) {
        if (clazz == ByteArrayTag.class) {
            return NBTConstants.TYPE_BYTE_ARRAY;
        } else if (clazz == ByteTag.class) {
            return NBTConstants.TYPE_BYTE;
        } else if (clazz == CompoundTag.class) {
            return NBTConstants.TYPE_COMPOUND;
        } else if (clazz == DoubleTag.class) {
            return NBTConstants.TYPE_DOUBLE;
        } else if (clazz == EndTag.class) {
            return NBTConstants.TYPE_END;
        } else if (clazz == FloatTag.class) {
            return NBTConstants.TYPE_FLOAT;
        } else if (clazz == IntArrayTag.class) {
            return NBTConstants.TYPE_INT_ARRAY;
        } else if (clazz == IntTag.class) {
            return NBTConstants.TYPE_INT;
        } else if (clazz.equals(ListTag.class) /* I hate this, it wouldn't do == b/c generics */) {
            return NBTConstants.TYPE_LIST;
        } else if (clazz == LongArrayTag.class) {
            return NBTConstants.TYPE_LONG_ARRAY;
        } else if (clazz == LongTag.class) {
            return NBTConstants.TYPE_LONG;
        } else if (clazz == ShortTag.class) {
            return NBTConstants.TYPE_SHORT;
        } else if (clazz == StringTag.class) {
            return NBTConstants.TYPE_STRING;
        }
        throw new IllegalArgumentException("Invalid tag class (" + clazz.getName() + ")");
    }

    /**
     * Gets the class of a type of tag.
     *
     * @param type the type
     * @return The class.
     * @throws IllegalArgumentException if the tag type is invalid.
     */
    public static Class<? extends Tag<?, ?>> getTypeClass(int type) {
        return NBTConstants.getClassFromType(type);
    }

    /**
     * Read a vector from a list tag containing ideally three values: the
     * X, Y, and Z components.
     *
     * <p>For values that are unavailable, their values will be 0.</p>
     *
     * @param listTag the list tag
     * @return a vector
     */
    public static Vector3 toVector(ListTag<?, ?> listTag) {
        checkNotNull(listTag);
        return Vector3.at(listTag.asDouble(0), listTag.asDouble(1), listTag.asDouble(2));
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items the map to read from
     * @param key the key to look for
     * @param expected the expected NBT class type
     * @return child tag
     * @throws InvalidFormatException if the format of the items is invalid
     */
    public static <T extends Tag<?, ?>> T getChildTag(Map<String, Tag<?, ?>> items, String key, Class<T> expected) throws InvalidFormatException {
        if (!items.containsKey(key)) {
            throw new InvalidFormatException("Missing a \"" + key + "\" tag");
        }
        Tag<?, ?> tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new InvalidFormatException(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

}
