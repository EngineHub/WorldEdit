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

package com.sk89q.worldedit.fabric;

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

    public static net.minecraft.nbt.Tag toNative(Tag tag) {
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

    public static net.minecraft.nbt.IntArrayTag toNative(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.ListTag toNative(ListTag tag) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (Tag child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.LongTag toNative(LongTag tag) {
        return new net.minecraft.nbt.LongTag(tag.getValue());
    }

    public static net.minecraft.nbt.StringTag toNative(StringTag tag) {
        return new net.minecraft.nbt.StringTag(tag.getValue());
    }

    public static net.minecraft.nbt.IntTag toNative(IntTag tag) {
        return new net.minecraft.nbt.IntTag(tag.getValue());
    }

    public static net.minecraft.nbt.ByteTag toNative(ByteTag tag) {
        return new net.minecraft.nbt.ByteTag(tag.getValue());
    }

    public static net.minecraft.nbt.ByteArrayTag toNative(ByteArrayTag tag) {
        byte[] value = tag.getValue();
        return new net.minecraft.nbt.ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.CompoundTag toNative(CompoundTag tag) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        for (Entry<String, Tag> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), toNative(child.getValue()));
        }
        return compound;
    }

    public static net.minecraft.nbt.FloatTag toNative(FloatTag tag) {
        return new net.minecraft.nbt.FloatTag(tag.getValue());
    }

    public static net.minecraft.nbt.ShortTag toNative(ShortTag tag) {
        return new net.minecraft.nbt.ShortTag(tag.getValue());
    }

    public static net.minecraft.nbt.DoubleTag toNative(DoubleTag tag) {
        return new net.minecraft.nbt.DoubleTag(tag.getValue());
    }

    public static Tag fromNative(net.minecraft.nbt.Tag other) {
        if (other instanceof net.minecraft.nbt.IntArrayTag) {
            return fromNative((net.minecraft.nbt.IntArrayTag) other);

        } else if (other instanceof net.minecraft.nbt.ListTag) {
            return fromNative((net.minecraft.nbt.ListTag) other);

        } else if (other instanceof net.minecraft.nbt.EndTag) {
            return fromNative((net.minecraft.nbt.EndTag) other);

        } else if (other instanceof net.minecraft.nbt.LongTag) {
            return fromNative((net.minecraft.nbt.LongTag) other);

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

    public static IntArrayTag fromNative(net.minecraft.nbt.IntArrayTag other) {
        int[] value = other.getIntArray();
        return new IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static ListTag fromNative(net.minecraft.nbt.ListTag other) {
        other = (net.minecraft.nbt.ListTag) other.copy();
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

    public static EndTag fromNative(net.minecraft.nbt.EndTag other) {
        return new EndTag();
    }

    public static LongTag fromNative(net.minecraft.nbt.LongTag other) {
        return new LongTag(other.getLong());
    }

    public static StringTag fromNative(net.minecraft.nbt.StringTag other) {
        return new StringTag(other.asString());
    }

    public static IntTag fromNative(net.minecraft.nbt.IntTag other) {
        return new IntTag(other.getInt());
    }

    public static ByteTag fromNative(net.minecraft.nbt.ByteTag other) {
        return new ByteTag(other.getByte());
    }

    public static ByteArrayTag fromNative(net.minecraft.nbt.ByteArrayTag other) {
        byte[] value = other.getByteArray();
        return new ByteArrayTag(Arrays.copyOf(value, value.length));
    }

    public static CompoundTag fromNative(net.minecraft.nbt.CompoundTag other) {
        Set<String> tags = other.getKeys();
        Map<String, Tag> map = new HashMap<>();
        for (String tagName : tags) {
            map.put(tagName, fromNative(other.getTag(tagName)));
        }
        return new CompoundTag(map);
    }

    public static FloatTag fromNative(net.minecraft.nbt.FloatTag other) {
        return new FloatTag(other.getFloat());
    }

    public static ShortTag fromNative(net.minecraft.nbt.ShortTag other) {
        return new ShortTag(other.getShort());
    }

    public static DoubleTag fromNative(net.minecraft.nbt.DoubleTag other) {
        return new DoubleTag(other.getDouble());
    }

}
