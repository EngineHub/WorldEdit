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

import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinDoubleTag;
import org.enginehub.linbus.tree.LinFloatTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinLongArrayTag;
import org.enginehub.linbus.tree.LinLongTag;
import org.enginehub.linbus.tree.LinShortTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTag;

import java.io.DataOutputStream;

/**
 * Converts between JNBT and lin-bus classes.
 *
 * @deprecated JNBT is being removed in WE8.
 */
@Deprecated
public class LinBusConverter {
    /**
     * Convert the given lin-bus tag to a legacy JNBT tag. If you know the specific type of the
     * lin-bus tag, you should just construct the legacy tag directly.
     *
     * @param tag the lin-bus tag
     * @param <V> the type of the lin-bus tag's value
     * @param <LT> the type of the lin-bus tag
     * @return the corresponding legacy JNBT tag
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <V, LT extends LinTag<? extends V>> Tag<V, LT> toJnbtTag(LT tag) {
        return (Tag<V, LT>) switch (tag.type().id()) {
            case BYTE_ARRAY -> new ByteArrayTag((LinByteArrayTag) tag);
            case BYTE -> new ByteTag((LinByteTag) tag);
            case COMPOUND -> new CompoundTag((LinCompoundTag) tag);
            case DOUBLE -> new DoubleTag((LinDoubleTag) tag);
            case END -> new EndTag();
            case FLOAT -> new FloatTag((LinFloatTag) tag);
            case INT_ARRAY -> new IntArrayTag((LinIntArrayTag) tag);
            case INT -> new IntTag((LinIntTag) tag);
            case LIST -> new ListTag((LinListTag<?>) tag);
            case LONG_ARRAY -> new LongArrayTag((LinLongArrayTag) tag);
            case LONG -> new LongTag((LinLongTag) tag);
            case SHORT -> new ShortTag((LinShortTag) tag);
            case STRING -> new StringTag((LinStringTag) tag);
        };
    }

    /**
     * Convert the given legacy stream to a {@link LinStream}. The legacy stream should not be used
     * after this.
     *
     * @param inputStream the legacy stream
     * @return the lin-bus stream
     */
    public static LinStream convertStream(NBTInputStream inputStream) {
        return LinBinaryIO.read(inputStream.is);
    }

    /**
     * Convert the given legacy stream to a {@link DataOutputStream}. The legacy stream should not
     * be used after this.
     *
     * @param outputStream the legacy stream
     * @return the data output stream
     */
    public static DataOutputStream convertStream(NBTOutputStream outputStream) {
        return outputStream.os;
    }

    private LinBusConverter() {
    }
}
