/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.forge;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.nbt.ShortNBT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Converts between JNBT and Minecraft NBT classes.
 */
final class NBTConverter {

    private NBTConverter() {
    }

    public static INBT toNative(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return toNative((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return toNative((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return toNative((LongTag) tag);

        } else if (tag instanceof StringTag) {
            return toNative((StringTag) tag);

        } else if (tag instanceof IntTag) {
            return toNative((IntTag) tag);

        } else if (tag instanceof ByteTag) {
            return toNative((ByteTag) tag);

        } else if (tag instanceof ByteArrayTag) {
            return toNative((ByteArrayTag) tag);

        } else if (tag instanceof CompoundTag) {
            return toNative((CompoundTag) tag);

        } else if (tag instanceof FloatTag) {
            return toNative((FloatTag) tag);

        } else if (tag instanceof ShortTag) {
            return toNative((ShortTag) tag);

        } else if (tag instanceof DoubleTag) {
            return toNative((DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static IntArrayNBT toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new IntArrayNBT(Arrays.copyOf(value, value.length));
    }

    public static ListNBT toNative(ListTag tag) {
        ListNBT list = new ListNBT();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static LongNBT toNative(LongTag tag) {
        return new LongNBT(tag.getValue());
    }

    public static StringNBT toNative(StringTag tag) {
        return new StringNBT(tag.getValue());
    }

    public static IntNBT toNative(IntTag tag) {
        return new IntNBT(tag.getValue());
    }

    public static ByteNBT toNative(ByteTag tag) {
        return new ByteNBT(tag.getValue());
    }

    public static ByteArrayNBT toNative(ByteArrayTag tag) {
        byte[] value = tag.getValue();
        return new ByteArrayNBT(Arrays.copyOf(value, value.length));
    }

    public static CompoundNBT toNative(CompoundTag tag) {
        CompoundNBT compound = new CompoundNBT();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static FloatNBT toNative(FloatTag tag) {
        return new FloatNBT(tag.getValue());
    }

    public static ShortNBT toNative(ShortTag tag) {
        return new ShortNBT(tag.getValue());
    }

    public static DoubleNBT toNative(DoubleTag tag) {
        return new DoubleNBT(tag.getValue());
    }

    public static Tag fromNative(INBT other) {
        if (other instanceof IntArrayNBT) {
            return fromNative((IntArrayNBT) other);

        } else if (other instanceof ListNBT) {
            return fromNative((ListNBT) other);

        } else if (other instanceof EndNBT) {
            return fromNative((EndNBT) other);

        } else if (other instanceof LongNBT) {
            return fromNative((LongNBT) other);

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

    public static IntArrayTag fromNative(IntArrayNBT other) {
        int[] value = other.getIntArray();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(ListNBT other) {
        other = other.copy();
        List<Tag> list = new ArrayList<>();
        Class<? extends Tag> listClass = StringTag.class;
        int tags = other.size();
        for (int i = 0; i < tags; i++) {
            Tag child = fromNative(other.remove(0));
            list.add(child);
            listClass = child.getClass();
        }
        return new ListTag(listClass, list);
    }

    public static EndTag fromNative(EndNBT other) {
        return new EndTag();
    }

    public static LongTag fromNative(LongNBT other) {
        return new LongTag(other.getLong());
    }

    public static StringTag fromNative(StringNBT other) {
        return new StringTag(other.getString());
    }

    public static IntTag fromNative(IntNBT other) {
        return new IntTag(other.getInt());
    }

    public static ByteTag fromNative(ByteNBT other) {
        return new ByteTag(other.getByte());
    }

    public static ByteArrayTag fromNative(ByteArrayNBT other) {
        byte[] value = other.getByteArray();
        return new ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static CompoundTag fromNative(CompoundNBT other) {
        Set<String> tags = other.keySet();
        Map<String, Tag> map = new HashMap<>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(FloatNBT other) {
        return new FloatTag(other.getFloat());
    }

    public static ShortTag fromNative(ShortNBT other) {
        return new ShortTag(other.getShort());
    }

    public static DoubleTag fromNative(DoubleNBT other) {
        return new DoubleTag(other.getDouble());
    }

}
