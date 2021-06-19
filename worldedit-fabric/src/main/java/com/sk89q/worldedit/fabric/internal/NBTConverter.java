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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.ByteArrayBinaryTag;
import com.sk89q.worldedit.util.nbt.ByteBinaryTag;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.DoubleBinaryTag;
import com.sk89q.worldedit.util.nbt.EndBinaryTag;
import com.sk89q.worldedit.util.nbt.FloatBinaryTag;
import com.sk89q.worldedit.util.nbt.IntArrayBinaryTag;
import com.sk89q.worldedit.util.nbt.IntBinaryTag;
import com.sk89q.worldedit.util.nbt.ListBinaryTag;
import com.sk89q.worldedit.util.nbt.LongArrayBinaryTag;
import com.sk89q.worldedit.util.nbt.LongBinaryTag;
import com.sk89q.worldedit.util.nbt.ShortBinaryTag;
import com.sk89q.worldedit.util.nbt.StringBinaryTag;

import java.util.Arrays;
import java.util.Set;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
public final class NBTConverter {

    private NBTConverter() {
    }

    public static net.minecraft.nbt.NbtElement toNative(BinaryTag tag) {
        if (tag instanceof IntArrayBinaryTag) {
            return toNative((IntArrayBinaryTag) tag);

        } else if (tag instanceof ListBinaryTag) {
            return toNative((ListBinaryTag) tag);

        } else if (tag instanceof LongBinaryTag) {
            return toNative((LongBinaryTag) tag);

        } else if (tag instanceof LongArrayBinaryTag) {
            return toNative((LongArrayBinaryTag) tag);

        } else if (tag instanceof StringBinaryTag) {
            return toNative((StringBinaryTag) tag);

        } else if (tag instanceof IntBinaryTag) {
            return toNative((IntBinaryTag) tag);

        } else if (tag instanceof ByteBinaryTag) {
            return toNative((ByteBinaryTag) tag);

        } else if (tag instanceof ByteArrayBinaryTag) {
            return toNative((ByteArrayBinaryTag) tag);

        } else if (tag instanceof CompoundBinaryTag) {
            return toNative((CompoundBinaryTag) tag);

        } else if (tag instanceof FloatBinaryTag) {
            return toNative((FloatBinaryTag) tag);

        } else if (tag instanceof ShortBinaryTag) {
            return toNative((ShortBinaryTag) tag);

        } else if (tag instanceof DoubleBinaryTag) {
            return toNative((DoubleBinaryTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static net.minecraft.nbt.NbtIntArray toNative(IntArrayBinaryTag tag) {
        int[] value = tag.value();
        return new net.minecraft.nbt.NbtIntArray(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.NbtList toNative(ListBinaryTag tag) {
        net.minecraft.nbt.NbtList list = new net.minecraft.nbt.NbtList();
        for (BinaryTag child : tag) {
            if (child instanceof EndBinaryTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.NbtLong toNative(LongBinaryTag tag) {
        return net.minecraft.nbt.NbtLong.of(tag.value());
    }

    public static net.minecraft.nbt.NbtLongArray toNative(LongArrayBinaryTag tag) {
        return new net.minecraft.nbt.NbtLongArray(tag.value().clone());
    }

    public static net.minecraft.nbt.NbtString toNative(StringBinaryTag tag) {
        return net.minecraft.nbt.NbtString.of(tag.value());
    }

    public static net.minecraft.nbt.NbtInt toNative(IntBinaryTag tag) {
        return net.minecraft.nbt.NbtInt.of(tag.value());
    }

    public static net.minecraft.nbt.NbtByte toNative(ByteBinaryTag tag) {
        return net.minecraft.nbt.NbtByte.of(tag.value());
    }

    public static net.minecraft.nbt.NbtByteArray toNative(ByteArrayBinaryTag tag) {
        return new net.minecraft.nbt.NbtByteArray(tag.value().clone());
    }

    public static net.minecraft.nbt.NbtCompound toNative(CompoundBinaryTag tag) {
        net.minecraft.nbt.NbtCompound compound = new net.minecraft.nbt.NbtCompound();
        for (String key : tag.keySet()) {
            compound.put(key, toNative(tag.get(key)));
        }
        return compound;
    }

    public static net.minecraft.nbt.NbtFloat toNative(FloatBinaryTag tag) {
        return net.minecraft.nbt.NbtFloat.of(tag.value());
    }

    public static net.minecraft.nbt.NbtShort toNative(ShortBinaryTag tag) {
        return net.minecraft.nbt.NbtShort.of(tag.value());
    }

    public static net.minecraft.nbt.NbtDouble toNative(DoubleBinaryTag tag) {
        return net.minecraft.nbt.NbtDouble.of(tag.value());
    }

    public static BinaryTag fromNative(net.minecraft.nbt.NbtElement other) {
        if (other instanceof net.minecraft.nbt.NbtIntArray) {
            return fromNative((net.minecraft.nbt.NbtIntArray) other);

        } else if (other instanceof net.minecraft.nbt.NbtList) {
            return fromNative((net.minecraft.nbt.NbtList) other);

        } else if (other instanceof net.minecraft.nbt.NbtNull) {
            return fromNative((net.minecraft.nbt.NbtNull) other);

        } else if (other instanceof net.minecraft.nbt.NbtLong) {
            return fromNative((net.minecraft.nbt.NbtLong) other);

        } else if (other instanceof net.minecraft.nbt.NbtLongArray) {
            return fromNative((net.minecraft.nbt.NbtLongArray) other);

        } else if (other instanceof net.minecraft.nbt.NbtString) {
            return fromNative((net.minecraft.nbt.NbtString) other);

        } else if (other instanceof net.minecraft.nbt.NbtInt) {
            return fromNative((net.minecraft.nbt.NbtInt) other);

        } else if (other instanceof net.minecraft.nbt.NbtByte) {
            return fromNative((net.minecraft.nbt.NbtByte) other);

        } else if (other instanceof net.minecraft.nbt.NbtByteArray) {
            return fromNative((net.minecraft.nbt.NbtByteArray) other);

        } else if (other instanceof net.minecraft.nbt.NbtCompound) {
            return fromNative((net.minecraft.nbt.NbtCompound) other);

        } else if (other instanceof net.minecraft.nbt.NbtFloat) {
            return fromNative((net.minecraft.nbt.NbtFloat) other);

        } else if (other instanceof net.minecraft.nbt.NbtShort) {
            return fromNative((net.minecraft.nbt.NbtShort) other);

        } else if (other instanceof net.minecraft.nbt.NbtDouble) {
            return fromNative((net.minecraft.nbt.NbtDouble) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayBinaryTag fromNative(net.minecraft.nbt.NbtIntArray other) {
        int[] value = other.getIntArray();
        return IntArrayBinaryTag.of(Arrays.copyOf(value, value.length));
    }

    public static ListBinaryTag fromNative(net.minecraft.nbt.NbtList other) {
        other = other.copy();
        ListBinaryTag.Builder list = ListBinaryTag.builder();
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            BinaryTag child = fromNative(other.remove(0));
            list.add(child);
        }
        return list.build();
    }

    public static EndBinaryTag fromNative(net.minecraft.nbt.NbtNull other) {
        return EndBinaryTag.get();
    }

    public static LongBinaryTag fromNative(net.minecraft.nbt.NbtLong other) {
        return LongBinaryTag.of(other.longValue());
    }

    public static LongArrayBinaryTag fromNative(net.minecraft.nbt.NbtLongArray other) {
        return LongArrayBinaryTag.of(other.getLongArray().clone());
    }

    public static StringBinaryTag fromNative(net.minecraft.nbt.NbtString other) {
        return StringBinaryTag.of(other.asString());
    }

    public static IntBinaryTag fromNative(net.minecraft.nbt.NbtInt other) {
        return IntBinaryTag.of(other.intValue());
    }

    public static ByteBinaryTag fromNative(net.minecraft.nbt.NbtByte other) {
        return ByteBinaryTag.of(other.byteValue());
    }

    public static ByteArrayBinaryTag fromNative(net.minecraft.nbt.NbtByteArray other) {
        return ByteArrayBinaryTag.of(other.getByteArray().clone());
    }

    public static CompoundBinaryTag fromNative(net.minecraft.nbt.NbtCompound other) {
        Set<String> tags = other.getKeys();
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (String tagName : tags) {
            builder.put(tagName, fromNative(other.get(tagName)));
        }
        return builder.build();
    }

    public static FloatBinaryTag fromNative(net.minecraft.nbt.NbtFloat other) {
        return FloatBinaryTag.of(other.floatValue());
    }

    public static ShortBinaryTag fromNative(net.minecraft.nbt.NbtShort other) {
        return ShortBinaryTag.of(other.shortValue());
    }

    public static DoubleBinaryTag fromNative(net.minecraft.nbt.NbtDouble other) {
        return DoubleBinaryTag.of(other.doubleValue());
    }

}
