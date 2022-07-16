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

package com.sk89q.worldedit.sponge.internal;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import org.enginehub.linbus.tree.LinTagType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NbtAdapter {
    /**
     * A separator to introduce errors if there is something to be separated. We should only see
     * single-part keys.
     */
    private static final String BREAKING_SEPARATOR = "if you see this, something is wrong";

    public static LinCompoundTag adaptToWorldEdit(DataView view) {
        LinCompoundTag.Builder builder = LinCompoundTag.builder();
        for (Map.Entry<DataQuery, Object> entry : view.values(false).entrySet()) {
            builder.put(
                entry.getKey().asString(BREAKING_SEPARATOR),
                adaptUnknownToWorldEdit(entry.getValue())
            );
        }
        return builder.build();
    }

    private static LinTag<?> adaptUnknownToWorldEdit(Object object) {
        if (object instanceof DataView) {
            return adaptToWorldEdit((DataView) object);
        }
        if (object instanceof Boolean) {
            return LinByteTag.of((byte) ((Boolean) object ? 1 : 0));
        }
        if (object instanceof Byte) {
            return LinByteTag.of((Byte) object);
        }
        if (object instanceof Short) {
            return LinShortTag.of(((Short) object));
        }
        if (object instanceof Integer) {
            return LinIntTag.of(((Integer) object));
        }
        if (object instanceof Long) {
            return LinLongTag.of(((Long) object));
        }
        if (object instanceof Float) {
            return LinFloatTag.of(((Float) object));
        }
        if (object instanceof Double) {
            return LinDoubleTag.of(((Double) object));
        }
        if (object instanceof String) {
            return LinStringTag.of((String) object);
        }
        if (object instanceof byte[]) {
            return LinByteArrayTag.of(((byte[]) object));
        }
        if (object instanceof Byte[] array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return LinByteArrayTag.of(copy);
        }
        if (object instanceof int[]) {
            return LinIntArrayTag.of(((int[]) object));
        }
        if (object instanceof Integer[] array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return LinIntArrayTag.of(copy);
        }
        if (object instanceof long[]) {
            return LinLongArrayTag.of(((long[]) object));
        }
        if (object instanceof Long[] array) {
            long[] copy = new long[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return LinLongArrayTag.of(copy);
        }
        if (object instanceof List<?> objects) {
            if (objects.isEmpty()) {
                return LinListTag.empty(LinTagType.endTag());
            }
            LinTag<?> first = adaptUnknownToWorldEdit(objects.get(0));
            @SuppressWarnings("unchecked")
            LinListTag.Builder<LinTag<?>> builder = LinListTag.builder((LinTagType<LinTag<?>>) first.type());
            builder.add(first);
            for (int i = 1; i < objects.size(); i++) {
                Object value = objects.get(i);
                builder.add(adaptUnknownToWorldEdit(value));
            }
            return builder.build();
        }
        if (object instanceof Map) {
            LinCompoundTag.Builder builder = LinCompoundTag.builder();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                String key = entry.getKey() instanceof DataQuery
                    ? ((DataQuery) entry.getKey()).asString(BREAKING_SEPARATOR)
                    : entry.getKey().toString();
                builder.put(key, adaptUnknownToWorldEdit(entry.getValue()));
            }
            return builder.build();
        }
        if (object instanceof DataSerializable) {
            return adaptToWorldEdit(((DataSerializable) object).toContainer());
        }
        throw new UnsupportedOperationException("Unable to translate into NBT: " + object.getClass());
    }

    public static DataContainer adaptFromWorldEdit(LinCompoundTag tag) {
        // copy to container, no cloning used because it's unlikely to leak
        // and it's cheaper this way
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        for (var entry : tag.value().entrySet()) {
            container.set(DataQuery.of(entry.getKey()), adaptTagFromWorldEdit(entry.getValue()));
        }
        return container;
    }

    private static Object adaptTagFromWorldEdit(LinTag<?> value) {
        if (value instanceof LinListTag<?> listTag) {
            return listTag.value().stream()
                .map(NbtAdapter::adaptTagFromWorldEdit)
                .collect(Collectors.toList());
        }
        if (value instanceof LinCompoundTag compoundTag) {
            return adaptFromWorldEdit(compoundTag);
        }
        // everything else is raw JDK types, so we can use it directly
        return value.value();
    }

    public static Tag adaptNMSToWorldEdit(LinTag<?> tag) {
        if (tag instanceof LinIntArrayTag intArrayTag) {
            return adaptNMSToWorldEdit(intArrayTag);
        } else if (tag instanceof LinListTag<?> listTag) {
            return adaptNMSToWorldEdit(listTag);
        } else if (tag instanceof LinLongTag longTag) {
            return adaptNMSToWorldEdit(longTag);
        } else if (tag instanceof LinLongArrayTag longArrayTag) {
            return adaptNMSToWorldEdit(longArrayTag);
        } else if (tag instanceof LinStringTag stringTag) {
            return adaptNMSToWorldEdit(stringTag);
        } else if (tag instanceof LinIntTag intTag) {
            return adaptNMSToWorldEdit(intTag);
        } else if (tag instanceof LinByteTag byteTag) {
            return adaptNMSToWorldEdit(byteTag);
        } else if (tag instanceof LinByteArrayTag byteArrayTag) {
            return adaptNMSToWorldEdit(byteArrayTag);
        } else if (tag instanceof LinCompoundTag compoundTag) {
            return adaptNMSToWorldEdit(compoundTag);
        } else if (tag instanceof LinFloatTag floatTag) {
            return adaptNMSToWorldEdit(floatTag);
        } else if (tag instanceof LinShortTag shortTag) {
            return adaptNMSToWorldEdit(shortTag);
        } else if (tag instanceof LinDoubleTag doubleTag) {
            return adaptNMSToWorldEdit(doubleTag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static IntArrayTag adaptNMSToWorldEdit(LinIntArrayTag tag) {
        return new IntArrayTag(tag.value());
    }

    public static ListTag adaptNMSToWorldEdit(LinListTag<?> tag) {
        ListTag list = new ListTag();
        for (LinTag<?> child : tag.value()) {
            list.add(adaptNMSToWorldEdit(child));
        }
        return list;
    }

    public static LongTag adaptNMSToWorldEdit(LinLongTag tag) {
        return LongTag.valueOf(tag.valueAsLong());
    }

    public static LongArrayTag adaptNMSToWorldEdit(LinLongArrayTag tag) {
        return new LongArrayTag(tag.value());
    }

    public static StringTag adaptNMSToWorldEdit(LinStringTag tag) {
        return StringTag.valueOf(tag.value());
    }

    public static IntTag adaptNMSToWorldEdit(LinIntTag tag) {
        return IntTag.valueOf(tag.valueAsInt());
    }

    public static ByteTag adaptNMSToWorldEdit(LinByteTag tag) {
        return ByteTag.valueOf(tag.valueAsByte());
    }

    public static ByteArrayTag adaptNMSToWorldEdit(LinByteArrayTag tag) {
        return new ByteArrayTag(tag.value());
    }

    public static CompoundTag adaptNMSToWorldEdit(LinCompoundTag tag) {
        CompoundTag compound = new CompoundTag();
        for (var child : tag.value().entrySet()) {
            compound.put(child.getKey(), adaptNMSToWorldEdit(child.getValue()));
        }
        return compound;
    }

    public static FloatTag adaptNMSToWorldEdit(LinFloatTag tag) {
        return FloatTag.valueOf(tag.valueAsFloat());
    }

    public static ShortTag adaptNMSToWorldEdit(LinShortTag tag) {
        return ShortTag.valueOf(tag.valueAsShort());
    }

    public static DoubleTag adaptNMSToWorldEdit(LinDoubleTag tag) {
        return DoubleTag.valueOf(tag.valueAsDouble());
    }
}
