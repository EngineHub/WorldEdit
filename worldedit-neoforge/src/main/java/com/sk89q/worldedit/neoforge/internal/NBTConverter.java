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
 * Converts between JNBT and Minecraft NBT classes.
 */
public final class NBTConverter {

    private NBTConverter() {
    }

    public static net.minecraft.nbt.Tag toNative(LinTag<?> tag) {
        if (tag instanceof LinIntArrayTag t) {
            return toNative(t);
        } else if (tag instanceof LinListTag<?> t) {
            return toNative(t);
        } else if (tag instanceof LinLongTag t) {
            return toNative(t);
        } else if (tag instanceof LinLongArrayTag t) {
            return toNative(t);
        } else if (tag instanceof LinStringTag t) {
            return toNative(t);
        } else if (tag instanceof LinIntTag t) {
            return toNative(t);
        } else if (tag instanceof LinByteTag t) {
            return toNative(t);
        } else if (tag instanceof LinByteArrayTag t) {
            return toNative(t);
        } else if (tag instanceof LinCompoundTag t) {
            return toNative(t);
        } else if (tag instanceof LinFloatTag t) {
            return toNative(t);
        } else if (tag instanceof LinShortTag t) {
            return toNative(t);
        } else if (tag instanceof LinDoubleTag t) {
            return toNative(t);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static net.minecraft.nbt.IntArrayTag toNative(LinIntArrayTag tag) {
        int[] value = tag.value();
        return new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.ListTag toNative(LinListTag<?> tag) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (LinTag<?> child : tag.value()) {
            list.add(toNative(child));
        }
        return list;
    }

    public static net.minecraft.nbt.LongTag toNative(LinLongTag tag) {
        return net.minecraft.nbt.LongTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.LongArrayTag toNative(LinLongArrayTag tag) {
        return new net.minecraft.nbt.LongArrayTag(tag.value().clone());
    }

    public static net.minecraft.nbt.StringTag toNative(LinStringTag tag) {
        return net.minecraft.nbt.StringTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.IntTag toNative(LinIntTag tag) {
        return net.minecraft.nbt.IntTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.ByteTag toNative(LinByteTag tag) {
        return net.minecraft.nbt.ByteTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.ByteArrayTag toNative(LinByteArrayTag tag) {
        return new net.minecraft.nbt.ByteArrayTag(tag.value().clone());
    }

    public static net.minecraft.nbt.CompoundTag toNative(LinCompoundTag tag) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        tag.value().forEach((key, value) -> compound.put(key, toNative(value)));
        return compound;
    }

    public static net.minecraft.nbt.FloatTag toNative(LinFloatTag tag) {
        return net.minecraft.nbt.FloatTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.ShortTag toNative(LinShortTag tag) {
        return net.minecraft.nbt.ShortTag.valueOf(tag.value());
    }

    public static net.minecraft.nbt.DoubleTag toNative(LinDoubleTag tag) {
        return net.minecraft.nbt.DoubleTag.valueOf(tag.value());
    }

    public static LinTag<?> fromNative(net.minecraft.nbt.Tag other) {
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

    public static LinIntArrayTag fromNative(net.minecraft.nbt.IntArrayTag other) {
        int[] value = other.getAsIntArray();
        return LinIntArrayTag.of(Arrays.copyOf(value, value.length));
    }

    public static LinListTag<?> fromNative(net.minecraft.nbt.ListTag other) {
        LinListTag.Builder<LinTag<?>> list = LinListTag.builder(LinTagType.fromId(
            LinTagId.fromId(other.getElementType())
        ));
        for (net.minecraft.nbt.Tag tag : other) {
            list.add(fromNative(tag));
        }
        return list.build();
    }

    public static LinEndTag fromNative(net.minecraft.nbt.EndTag other) {
        return LinEndTag.instance();
    }

    public static LinLongTag fromNative(net.minecraft.nbt.LongTag other) {
        return LinLongTag.of(other.getAsLong());
    }

    public static LinLongArrayTag fromNative(net.minecraft.nbt.LongArrayTag other) {
        return LinLongArrayTag.of(other.getAsLongArray().clone());
    }

    public static LinStringTag fromNative(net.minecraft.nbt.StringTag other) {
        return LinStringTag.of(other.getAsString());
    }

    public static LinIntTag fromNative(net.minecraft.nbt.IntTag other) {
        return LinIntTag.of(other.getAsInt());
    }

    public static LinByteTag fromNative(net.minecraft.nbt.ByteTag other) {
        return LinByteTag.of(other.getAsByte());
    }

    public static LinByteArrayTag fromNative(net.minecraft.nbt.ByteArrayTag other) {
        return LinByteArrayTag.of(other.getAsByteArray().clone());
    }

    public static LinCompoundTag fromNative(net.minecraft.nbt.CompoundTag other) {
        Set<String> tags = other.getAllKeys();
        LinCompoundTag.Builder builder = LinCompoundTag.builder();
        for (String tagName : tags) {
            builder.put(tagName, fromNative(other.get(tagName)));
        }
        return builder.build();
    }

    public static LinFloatTag fromNative(net.minecraft.nbt.FloatTag other) {
        return LinFloatTag.of(other.getAsFloat());
    }

    public static LinShortTag fromNative(net.minecraft.nbt.ShortTag other) {
        return LinShortTag.of(other.getAsShort());
    }

    public static LinDoubleTag fromNative(net.minecraft.nbt.DoubleTag other) {
        return LinDoubleTag.of(other.getAsDouble());
    }

}
