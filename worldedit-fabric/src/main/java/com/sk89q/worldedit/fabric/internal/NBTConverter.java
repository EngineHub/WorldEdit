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

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;

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
public final class NBTConverter {

    private NBTConverter() {
    }

    public static net.minecraft.nbt.NbtElement toNative(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return toNative((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return toNative((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return toNative((LongTag) tag);

        } else if (tag instanceof LongArrayTag) {
            return toNative((LongArrayTag) tag);

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

    public static net.minecraft.nbt.NbtIntArray toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new net.minecraft.nbt.NbtIntArray(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.NbtList toNative(ListTag tag) {
        net.minecraft.nbt.NbtList list = new net.minecraft.nbt.NbtList();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.NbtLong toNative(LongTag tag) {
        return net.minecraft.nbt.NbtLong.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtLongArray toNative(LongArrayTag tag) {
        return new net.minecraft.nbt.NbtLongArray(tag.getValue().clone());
    }

    public static net.minecraft.nbt.NbtString toNative(StringTag tag) {
        return net.minecraft.nbt.NbtString.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtInt toNative(IntTag tag) {
        return net.minecraft.nbt.NbtInt.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtByte toNative(ByteTag tag) {
        return net.minecraft.nbt.NbtByte.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtByteArray toNative(ByteArrayTag tag) {
        return new net.minecraft.nbt.NbtByteArray(tag.getValue().clone());
    }

    public static net.minecraft.nbt.NbtCompound toNative(CompoundTag tag) {
        net.minecraft.nbt.NbtCompound compound = new net.minecraft.nbt.NbtCompound();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static net.minecraft.nbt.NbtFloat toNative(FloatTag tag) {
        return net.minecraft.nbt.NbtFloat.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtShort toNative(ShortTag tag) {
        return net.minecraft.nbt.NbtShort.of(tag.getValue());
    }

    public static net.minecraft.nbt.NbtDouble toNative(DoubleTag tag) {
        return net.minecraft.nbt.NbtDouble.of(tag.getValue());
    }

    public static Tag fromNative(net.minecraft.nbt.NbtElement other) {
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

    public static IntArrayTag fromNative(net.minecraft.nbt.NbtIntArray other) {
        int[] value = other.getIntArray();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(net.minecraft.nbt.NbtList other) {
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

    public static EndTag fromNative(net.minecraft.nbt.NbtNull other) {
        return new EndTag();
    }

    public static LongTag fromNative(net.minecraft.nbt.NbtLong other) {
        return new LongTag(other.longValue());
    }

    public static LongArrayTag fromNative(net.minecraft.nbt.NbtLongArray other) {
        return new LongArrayTag(other.getLongArray().clone());
    }

    public static StringTag fromNative(net.minecraft.nbt.NbtString other) {
        return new StringTag(other.asString());
    }

    public static IntTag fromNative(net.minecraft.nbt.NbtInt other) {
        return new IntTag(other.intValue());
    }

    public static ByteTag fromNative(net.minecraft.nbt.NbtByte other) {
        return new ByteTag(other.byteValue());
    }

    public static ByteArrayTag fromNative(net.minecraft.nbt.NbtByteArray other) {
        return new ByteArrayTag(other.getByteArray().clone());
    }

    public static CompoundTag fromNative(net.minecraft.nbt.NbtCompound other) {
        Set<String> tags = other.getKeys();
        Map<String, Tag> map = new HashMap<>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.get(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(net.minecraft.nbt.NbtFloat other) {
        return new FloatTag(other.floatValue());
    }

    public static ShortTag fromNative(net.minecraft.nbt.NbtShort other) {
        return new ShortTag(other.shortValue());
    }

    public static DoubleTag fromNative(net.minecraft.nbt.NbtDouble other) {
        return new DoubleTag(other.doubleValue());
    }

}
