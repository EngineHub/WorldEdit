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

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagType;
import com.sk89q.worldedit.util.nbt.BinaryTagTypes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Converts between JNBT and Adventure-NBT classes.
 *
 * @deprecated JNBT is being removed in WE8.
 */
@Deprecated
public class AdventureNBTConverter {
    private static final BiMap<Class<? extends Tag>, BinaryTagType<?>> TAG_TYPES =
        new ImmutableBiMap.Builder<Class<? extends Tag>, BinaryTagType<?>>()
            .put(ByteArrayTag.class, BinaryTagTypes.BYTE_ARRAY)
            .put(ByteTag.class, BinaryTagTypes.BYTE)
            .put(CompoundTag.class, BinaryTagTypes.COMPOUND)
            .put(DoubleTag.class, BinaryTagTypes.DOUBLE)
            .put(EndTag.class, BinaryTagTypes.END)
            .put(FloatTag.class, BinaryTagTypes.FLOAT)
            .put(IntArrayTag.class, BinaryTagTypes.INT_ARRAY)
            .put(IntTag.class, BinaryTagTypes.INT)
            .put(ListTag.class, BinaryTagTypes.LIST)
            .put(LongArrayTag.class, BinaryTagTypes.LONG_ARRAY)
            .put(LongTag.class, BinaryTagTypes.LONG)
            .put(ShortTag.class, BinaryTagTypes.SHORT)
            .put(StringTag.class, BinaryTagTypes.STRING)
            .build();

    private static final Map<BinaryTagType<?>, Function<BinaryTag, Tag>> CONVERSION;

    static {
        ImmutableMap.Builder<BinaryTagType<?>, Function<BinaryTag, Tag>> conversion =
            ImmutableMap.builder();

        for (Map.Entry<Class<? extends Tag>, BinaryTagType<?>> tag : TAG_TYPES.entrySet()) {
            Constructor<?>[] constructors = tag.getKey().getConstructors();
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == 1 && BinaryTag.class.isAssignableFrom(c.getParameterTypes()[0])) {
                    conversion.put(tag.getValue(), binaryTag -> {
                        try {
                            return (Tag) c.newInstance(binaryTag);
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch (InvocationTargetException e) {
                            // I assume this is always a RuntimeException since we control the ctor
                            throw (RuntimeException) e.getCause();
                        }
                    });
                    break;
                }
            }
        }

        CONVERSION = conversion.build();
    }

    public static BinaryTagType<?> getAdventureType(Class<? extends Tag> type) {
        return Objects.requireNonNull(TAG_TYPES.get(type), () -> "Missing entry for " + type);
    }

    public static Class<? extends Tag> getJNBTType(BinaryTagType<?> type) {
        return Objects.requireNonNull(TAG_TYPES.inverse().get(type), () -> "Missing entry for " + type);
    }

    private AdventureNBTConverter() {
    }

    public static Tag fromAdventure(BinaryTag other) {
        Function<BinaryTag, Tag> conversion = CONVERSION.get(other.type());
        if (conversion == null) {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
        return conversion.apply(other);
    }
}
