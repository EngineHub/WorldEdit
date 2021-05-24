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

package com.sk89q.worldedit.forge.internal;

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
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;

import java.util.Arrays;
import java.util.Set;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
public final class NBTConverter {

    private NBTConverter() {
    }

    public static INBT toNative(BinaryTag tag) {
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

    public static IntArrayNBT toNative(IntArrayBinaryTag tag) {
        int[] value = tag.value();
        return new IntArrayNBT(Arrays.copyOf(value, value.length));
    }

    public static ListNBT toNative(ListBinaryTag tag) {
        ListNBT list = new ListNBT();
        for (BinaryTag child : tag) {
            if (child instanceof EndBinaryTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static LongNBT toNative(LongBinaryTag tag) {
        return LongNBT.valueOf(tag.value());
    }

    public static LongArrayNBT toNative(LongArrayBinaryTag tag) {
        return new LongArrayNBT(tag.value().clone());
    }

    public static StringNBT toNative(StringBinaryTag tag) {
        return StringNBT.valueOf(tag.value());
    }

    public static IntNBT toNative(IntBinaryTag tag) {
        return IntNBT.valueOf(tag.value());
    }

    public static ByteNBT toNative(ByteBinaryTag tag) {
        return ByteNBT.valueOf(tag.value());
    }

    public static ByteArrayNBT toNative(ByteArrayBinaryTag tag) {
        return new ByteArrayNBT(tag.value().clone());
    }

    public static CompoundNBT toNative(CompoundBinaryTag tag) {
        CompoundNBT compound = new CompoundNBT();
        for (String key : tag.keySet()) {
            compound.put(key, toNative(tag.get(key)));
        }
        return compound;
    }

    public static FloatNBT toNative(FloatBinaryTag tag) {
        return FloatNBT.valueOf(tag.value());
    }

    public static ShortNBT toNative(ShortBinaryTag tag) {
        return ShortNBT.valueOf(tag.value());
    }

    public static DoubleNBT toNative(DoubleBinaryTag tag) {
        return DoubleNBT.valueOf(tag.value());
    }

    public static BinaryTag fromNative(INBT other) {
        if (other instanceof IntArrayNBT) {
            return fromNative((IntArrayNBT) other);

        } else if (other instanceof ListNBT) {
            return fromNative((ListNBT) other);

        } else if (other instanceof EndNBT) {
            return fromNative((EndNBT) other);

        } else if (other instanceof LongNBT) {
            return fromNative((LongNBT) other);

        } else if (other instanceof LongArrayNBT) {
            return fromNative((LongArrayNBT) other);

        } else if (other instanceof StringNBT) {
            return fromNative((StringNBT) other);

        } else if (other instanceof IntNBT) {
            return fromNative((IntNBT) other);

        } else if (other instanceof ByteNBT) {
            return fromNative((ByteNBT) other);

        } else if (other instanceof ByteArrayNBT) {
            return fromNative((ByteArrayNBT) other);

        } else if (other instanceof CompoundNBT) {
            return fromNative((CompoundNBT) other);

        } else if (other instanceof FloatNBT) {
            return fromNative((FloatNBT) other);

        } else if (other instanceof ShortNBT) {
            return fromNative((ShortNBT) other);

        } else if (other instanceof DoubleNBT) {
            return fromNative((DoubleNBT) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayBinaryTag fromNative(IntArrayNBT other) {
        int[] value = other.getIntArray();
        return IntArrayBinaryTag.of(Arrays.copyOf(value, value.length));
    }

    public static ListBinaryTag fromNative(ListNBT other) {
        other = other.copy();
        ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.builder();
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            BinaryTag child = fromNative(other.remove(0));
            list.add(child);
        }
        return list.build();
    }

    public static EndBinaryTag fromNative(EndNBT other) {
        return EndBinaryTag.get();
    }

    public static LongBinaryTag fromNative(LongNBT other) {
        return LongBinaryTag.of(other.getLong());
    }

    public static LongArrayBinaryTag fromNative(LongArrayNBT other) {
        return LongArrayBinaryTag.of(other.getAsLongArray().clone());
    }

    public static StringBinaryTag fromNative(StringNBT other) {
        return StringBinaryTag.of(other.getString());
    }

    public static IntBinaryTag fromNative(IntNBT other) {
        return IntBinaryTag.of(other.getInt());
    }

    public static ByteBinaryTag fromNative(ByteNBT other) {
        return ByteBinaryTag.of(other.getByte());
    }

    public static ByteArrayBinaryTag fromNative(ByteArrayNBT other) {
        return ByteArrayBinaryTag.of(other.getByteArray().clone());
    }

    public static CompoundBinaryTag fromNative(CompoundNBT other) {
        Set<String> tags = other.keySet();
        CompoundBinaryTag.Builder map = CompoundBinaryTag.builder();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return map.build();
    }

    public static FloatBinaryTag fromNative(FloatNBT other) {
        return FloatBinaryTag.of(other.getFloat());
    }

    public static ShortBinaryTag fromNative(ShortNBT other) {
        return ShortBinaryTag.of(other.getShort());
    }

    public static DoubleBinaryTag fromNative(DoubleNBT other) {
        return DoubleBinaryTag.of(other.getDouble());
    }

}
