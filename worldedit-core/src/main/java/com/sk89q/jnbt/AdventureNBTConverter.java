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

/**
 * Converts between JNBT and Adventure-NBT classes.
 *
 * @deprecated JNBT is being removed in WE8.
 */
@Deprecated
public class AdventureNBTConverter {

    private AdventureNBTConverter() {

    }

    public static Tag fromAdventure(BinaryTag other) {
        if (other instanceof IntArrayBinaryTag) {
            return fromAdventure((IntArrayBinaryTag) other);
        } else if (other instanceof ListBinaryTag) {
            return fromAdventure((ListBinaryTag) other);
        } else if (other instanceof EndBinaryTag) {
            return fromAdventure();
        } else if (other instanceof LongBinaryTag) {
            return fromAdventure((LongBinaryTag) other);
        } else if (other instanceof LongArrayBinaryTag) {
            return fromAdventure((LongArrayBinaryTag) other);
        } else if (other instanceof StringBinaryTag) {
            return fromAdventure((StringBinaryTag) other);
        } else if (other instanceof IntBinaryTag) {
            return fromAdventure((IntBinaryTag) other);
        } else if (other instanceof ByteBinaryTag) {
            return fromAdventure((ByteBinaryTag) other);
        } else if (other instanceof ByteArrayBinaryTag) {
            return fromAdventure((ByteArrayBinaryTag) other);
        } else if (other instanceof CompoundBinaryTag) {
            return fromAdventure((CompoundBinaryTag) other);
        } else if (other instanceof FloatBinaryTag) {
            return fromAdventure((FloatBinaryTag) other);
        } else if (other instanceof ShortBinaryTag) {
            return fromAdventure((ShortBinaryTag) other);
        } else if (other instanceof DoubleBinaryTag) {
            return fromAdventure((DoubleBinaryTag) other);
        } else {
            throw new IllegalArgumentException("Can't convert other of type " + other.getClass().getCanonicalName());
        }
    }

    public static DoubleTag fromAdventure(DoubleBinaryTag other) {
        return new DoubleTag(other);
    }

    public static ShortTag fromAdventure(ShortBinaryTag other) {
        return new ShortTag(other);
    }

    public static FloatTag fromAdventure(FloatBinaryTag other) {
        return new FloatTag(other);
    }

    public static CompoundTag fromAdventure(CompoundBinaryTag other) {
        return new CompoundTag(other);
    }

    public static ByteArrayTag fromAdventure(ByteArrayBinaryTag other) {
        return new ByteArrayTag(other);
    }

    public static ByteTag fromAdventure(ByteBinaryTag other) {
        return new ByteTag(other);
    }

    public static IntTag fromAdventure(IntBinaryTag other) {
        return new IntTag(other);
    }

    public static StringTag fromAdventure(StringBinaryTag other) {
        return new StringTag(other);
    }

    public static LongArrayTag fromAdventure(LongArrayBinaryTag other) {
        return new LongArrayTag(other);
    }

    public static LongTag fromAdventure(LongBinaryTag other) {
        return new LongTag(other);
    }

    public static EndTag fromAdventure() {
        return new EndTag();
    }

    public static ListTag fromAdventure(ListBinaryTag other) {
        return new ListTag(other);
    }

    public static IntArrayTag fromAdventure(IntArrayBinaryTag other) {
        return new IntArrayTag(other);
    }
}
