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

package com.sk89q.worldedit.neoforge.internal;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.enginehub.linbus.common.LinTagId;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinEndTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.Arrays;
import java.util.Set;

/**
 * Converts between LinBus and Minecraft NBT classes.
 */
public final class NBTConverter {

    private NBTConverter() {
    }

    public static Tag toNative(LinTag<?> tag) {
        return switch (tag) {
            case LinIntArrayTag t -> toNative(t);
            case LinListTag<?> t -> toNative(t);
            case LinLongTag t -> toNative(t);
            case LinLongArrayTag t -> toNative(t);
            case LinStringTag t -> toNative(t);
            case LinIntTag t -> toNative(t);
            case LinByteTag t -> toNative(t);
            case LinByteArrayTag t -> toNative(t);
            case LinCompoundTag t -> toNative(t);
            case LinFloatTag t -> toNative(t);
            case LinShortTag t -> toNative(t);
            case LinDoubleTag t -> toNative(t);
            case LinEndTag ignored -> EndTag.INSTANCE;
        };
    }

    public static IntArrayTag toNative(LinIntArrayTag tag) {
        int[] value = tag.value();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag toNative(LinListTag<?> tag) {
        ListTag list = new ListTag();
        for (LinTag<?> child : tag.value()) {
            list.addAndUnwrap(toNative(child));
        }
        return list;
    }

    public static LongTag toNative(LinLongTag tag) {
        return LongTag.valueOf(tag.value());
    }

    public static LongArrayTag toNative(LinLongArrayTag tag) {
        return new LongArrayTag(tag.value().clone());
    }

    public static StringTag toNative(LinStringTag tag) {
        return StringTag.valueOf(tag.value());
    }

    public static IntTag toNative(LinIntTag tag) {
        return IntTag.valueOf(tag.value());
    }

    public static ByteTag toNative(LinByteTag tag) {
        return ByteTag.valueOf(tag.value());
    }

    public static ByteArrayTag toNative(LinByteArrayTag tag) {
        return new ByteArrayTag(tag.value().clone());
    }

    public static CompoundTag toNative(LinCompoundTag tag) {
        CompoundTag compound = new CompoundTag();
        tag.value().forEach((key, value) -> compound.put(key, toNative(value)));
        return compound;
    }

    public static FloatTag toNative(LinFloatTag tag) {
        return FloatTag.valueOf(tag.value());
    }

    public static ShortTag toNative(LinShortTag tag) {
        return ShortTag.valueOf(tag.value());
    }

    public static DoubleTag toNative(LinDoubleTag tag) {
        return DoubleTag.valueOf(tag.value());
    }

    public static LinTag<?> fromNative(Tag other) {
        return switch (other) {
            case IntArrayTag tags -> fromNative(tags);
            case ListTag tags -> fromNative(tags);
            case EndTag endTag -> fromNative(endTag);
            case LongTag longTag -> fromNative(longTag);
            case LongArrayTag tags -> fromNative(tags);
            case StringTag stringTag -> fromNative(stringTag);
            case IntTag intTag -> fromNative(intTag);
            case ByteTag byteTag -> fromNative(byteTag);
            case ByteArrayTag tags -> fromNative(tags);
            case CompoundTag compoundTag -> fromNative(compoundTag);
            case FloatTag floatTag -> fromNative(floatTag);
            case ShortTag shortTag -> fromNative(shortTag);
            case DoubleTag doubleTag -> fromNative(doubleTag);
        };
    }

    public static LinIntArrayTag fromNative(IntArrayTag other) {
        int[] value = other.getAsIntArray();
        return LinIntArrayTag.of(Arrays.copyOf(value, value.length));
    }

    private static byte identifyRawElementType(ListTag list) {
        byte b = 0;

        for (Tag tag : list) {
            byte c = tag.getId();
            if (b == 0) {
                b = c;
            } else if (b != c) {
                return 10;
            }
        }

        return b;
    }

    private static CompoundTag wrapTag(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            return compoundTag;
        }
        var compoundTag = new CompoundTag();
        compoundTag.put("", tag);
        return compoundTag;
    }

    public static LinListTag<?> fromNative(ListTag other) {
        byte rawType = identifyRawElementType(other);
        LinListTag.Builder<LinTag<?>> list = LinListTag.builder(LinTagType.fromId(
                LinTagId.fromId(rawType)
        ));
        for (Tag tag : other) {
            if (rawType == LinTagId.COMPOUND.id() && !(tag instanceof CompoundTag)) {
                list.add(fromNative(wrapTag(tag)));
            } else {
                list.add(fromNative(tag));
            }
        }
        return list.build();
    }

    public static LinEndTag fromNative(EndTag other) {
        return LinEndTag.instance();
    }

    public static LinLongTag fromNative(LongTag other) {
        return LinLongTag.of(other.value());
    }

    public static LinLongArrayTag fromNative(LongArrayTag other) {
        return LinLongArrayTag.of(other.getAsLongArray().clone());
    }

    public static LinStringTag fromNative(StringTag other) {
        return LinStringTag.of(other.value());
    }

    public static LinIntTag fromNative(IntTag other) {
        return LinIntTag.of(other.value());
    }

    public static LinByteTag fromNative(ByteTag other) {
        return LinByteTag.of(other.value());
    }

    public static LinByteArrayTag fromNative(ByteArrayTag other) {
        return LinByteArrayTag.of(other.getAsByteArray().clone());
    }

    public static LinCompoundTag fromNative(CompoundTag other) {
        Set<String> tags = other.keySet();
        LinCompoundTag.Builder builder = LinCompoundTag.builder();
        for (String tagName : tags) {
            builder.put(tagName, fromNative(other.get(tagName)));
        }
        return builder.build();
    }

    public static LinFloatTag fromNative(FloatTag other) {
        return LinFloatTag.of(other.value());
    }

    public static LinShortTag fromNative(ShortTag other) {
        return LinShortTag.of(other.value());
    }

    public static LinDoubleTag fromNative(DoubleTag other) {
        return LinDoubleTag.of(other.value());
    }

}
