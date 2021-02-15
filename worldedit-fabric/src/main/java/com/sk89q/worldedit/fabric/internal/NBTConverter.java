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

    public static net.minecraft.nbt.Tag toNative(BinaryTag tag) {
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

    public static net.minecraft.nbt.IntArrayTag toNative(IntArrayBinaryTag tag) {
        int[] value = tag.value();
        return new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.ListTag toNative(ListBinaryTag tag) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (BinaryTag child : tag) {
            if (child instanceof EndBinaryTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.LongTag toNative(LongBinaryTag tag) {
        return net.minecraft.nbt.LongTag.of(tag.value());
    }

    public static net.minecraft.nbt.LongArrayTag toNative(LongArrayBinaryTag tag) {
        return new net.minecraft.nbt.LongArrayTag(tag.value().clone());
    }

    public static net.minecraft.nbt.StringTag toNative(StringBinaryTag tag) {
        return net.minecraft.nbt.StringTag.of(tag.value());
    }

    public static net.minecraft.nbt.IntTag toNative(IntBinaryTag tag) {
        return net.minecraft.nbt.IntTag.of(tag.value());
    }

    public static net.minecraft.nbt.ByteTag toNative(ByteBinaryTag tag) {
        return net.minecraft.nbt.ByteTag.of(tag.value());
    }

    public static net.minecraft.nbt.ByteArrayTag toNative(ByteArrayBinaryTag tag) {
        return new net.minecraft.nbt.ByteArrayTag(tag.value().clone());
    }

    public static net.minecraft.nbt.CompoundTag toNative(CompoundBinaryTag tag) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        for (String key : tag.keySet()) {
            compound.put(key, toNative(tag.get(key)));
        }
        return compound;
    }

    public static net.minecraft.nbt.FloatTag toNative(FloatBinaryTag tag) {
        return net.minecraft.nbt.FloatTag.of(tag.value());
    }

    public static net.minecraft.nbt.ShortTag toNative(ShortBinaryTag tag) {
        return net.minecraft.nbt.ShortTag.of(tag.value());
    }

    public static net.minecraft.nbt.DoubleTag toNative(DoubleBinaryTag tag) {
        return net.minecraft.nbt.DoubleTag.of(tag.value());
    }

    public static BinaryTag fromNative(net.minecraft.nbt.Tag other) {
        if (other instanceof net.minecraft.nbt.IntArrayTag) {
            return fromNative((net.minecraft.nbt.IntArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.ListTag) {
            return fromNative((net.minecraft.nbt.ListTag) other);

        } else if (other instanceof net.minecraft.nbt.EndTag) {
            return fromNative((net.minecraft.nbt.EndTag) other);

        } else if (other instanceof net.minecraft.nbt.LongTag) {
            return fromNative((net.minecraft.nbt.LongTag) other);

        } else if (other instanceof net.minecraft.nbt.LongArrayTag) {
            return fromNative((net.minecraft.nbt.LongArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.StringTag) {
            return fromNative((net.minecraft.nbt.StringTag) other);

        } else if (other instanceof net.minecraft.nbt.IntTag) {
            return fromNative((net.minecraft.nbt.IntTag) other);

        } else if (other instanceof net.minecraft.nbt.ByteTag) {
            return fromNative((net.minecraft.nbt.ByteTag) other);

        } else if (other instanceof net.minecraft.nbt.ByteArrayTag) {
            return fromNative((net.minecraft.nbt.ByteArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.CompoundTag) {
            return fromNative((net.minecraft.nbt.CompoundTag) other);

        } else if (other instanceof net.minecraft.nbt.FloatTag) {
            return fromNative((net.minecraft.nbt.FloatTag) other);

        } else if (other instanceof net.minecraft.nbt.ShortTag) {
            return fromNative((net.minecraft.nbt.ShortTag) other);

        } else if (other instanceof net.minecraft.nbt.DoubleTag) {
            return fromNative((net.minecraft.nbt.DoubleTag) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static IntArrayBinaryTag fromNative(net.minecraft.nbt.IntArrayTag other) {
        int[] value = other.getIntArray();
        return IntArrayBinaryTag.of(Arrays.copyOf(value, value.length));
    }

    public static ListBinaryTag fromNative(net.minecraft.nbt.ListTag other) {
        other = other.copy();
        ListBinaryTag.Builder list = ListBinaryTag.builder();
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            BinaryTag child = fromNative(other.remove(0));
            list.add(child);
        }
        return list.build();
    }

    public static EndBinaryTag fromNative(net.minecraft.nbt.EndTag other) {
        return EndBinaryTag.get();
    }

    public static LongBinaryTag fromNative(net.minecraft.nbt.LongTag other) {
        return LongBinaryTag.of(other.getLong());
    }

    public static LongArrayBinaryTag fromNative(net.minecraft.nbt.LongArrayTag other) {
        return LongArrayBinaryTag.of(other.getLongArray().clone());
    }

    public static StringBinaryTag fromNative(net.minecraft.nbt.StringTag other) {
        return StringBinaryTag.of(other.asString());
    }

    public static IntBinaryTag fromNative(net.minecraft.nbt.IntTag other) {
        return IntBinaryTag.of(other.getInt());
    }

    public static ByteBinaryTag fromNative(net.minecraft.nbt.ByteTag other) {
        return ByteBinaryTag.of(other.getByte());
    }

    public static ByteArrayBinaryTag fromNative(net.minecraft.nbt.ByteArrayTag other) {
        return ByteArrayBinaryTag.of(other.getByteArray().clone());
    }

    public static CompoundBinaryTag fromNative(net.minecraft.nbt.CompoundTag other) {
        Set<String> tags = other.getKeys();
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        for (String tagName : tags) {
            builder.put(tagName, fromNative(other.get(tagName)));
        }
        return builder.build();
    }

    public static FloatBinaryTag fromNative(net.minecraft.nbt.FloatTag other) {
        return FloatBinaryTag.of(other.getFloat());
    }

    public static ShortBinaryTag fromNative(net.minecraft.nbt.ShortTag other) {
        return ShortBinaryTag.of(other.getShort());
    }

    public static DoubleBinaryTag fromNative(net.minecraft.nbt.DoubleTag other) {
        return DoubleBinaryTag.of(other.getDouble());
    }

}
