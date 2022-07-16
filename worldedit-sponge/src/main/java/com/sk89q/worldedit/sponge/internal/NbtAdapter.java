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

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.EndTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.ListTagBuilder;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NbtAdapter {
    /**
     * A separator to introduce errors if there is something to be separated. We should only see
     * single-part keys.
     */
    private static final String BREAKING_SEPARATOR = "if you see this, something is wrong";

    public static CompoundTag adaptToWorldEdit(DataView view) {
        CompoundTagBuilder builder = CompoundTagBuilder.create();
        for (Map.Entry<DataQuery, Object> entry : view.values(false).entrySet()) {
            builder.put(
                entry.getKey().asString(BREAKING_SEPARATOR),
                adaptUnknownToWorldEdit(entry.getValue())
            );
        }
        return builder.build();
    }

    private static Tag adaptUnknownToWorldEdit(Object object) {
        if (object instanceof DataView) {
            return adaptToWorldEdit((DataView) object);
        }
        if (object instanceof Boolean) {
            return new ByteTag((byte) ((Boolean) object ? 1 : 0));
        }
        if (object instanceof Byte) {
            return new ByteTag((Byte) object);
        }
        if (object instanceof Short) {
            return new ShortTag(((Short) object));
        }
        if (object instanceof Integer) {
            return new IntTag(((Integer) object));
        }
        if (object instanceof Long) {
            return new LongTag(((Long) object));
        }
        if (object instanceof Float) {
            return new FloatTag(((Float) object));
        }
        if (object instanceof Double) {
            return new DoubleTag(((Double) object));
        }
        if (object instanceof String) {
            return new StringTag((String) object);
        }
        if (object instanceof byte[]) {
            return new ByteArrayTag(((byte[]) object));
        }
        if (object instanceof Byte[]) {
            Byte[] array = (Byte[]) object;
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new ByteArrayTag(copy);
        }
        if (object instanceof int[]) {
            return new IntArrayTag(((int[]) object));
        }
        if (object instanceof Integer[]) {
            Integer[] array = (Integer[]) object;
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new IntArrayTag(copy);
        }
        if (object instanceof long[]) {
            return new LongArrayTag(((long[]) object));
        }
        if (object instanceof Long[]) {
            Long[] array = (Long[]) object;
            long[] copy = new long[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new LongArrayTag(copy);
        }
        if (object instanceof List) {
            List<?> objects = (List<?>) object;
            if (objects.isEmpty()) {
                return new ListTag(EndTag.class, Collections.emptyList());
            }
            Tag[] entries = new Tag[objects.size()];
            for (int i = 0; i < objects.size(); i++) {
                Object value = objects.get(i);
                entries[i] = adaptUnknownToWorldEdit(value);
            }
            return ListTagBuilder.createWith(entries).build();
        }
        if (object instanceof Map) {
            CompoundTagBuilder builder = CompoundTagBuilder.create();
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

    public static DataContainer adaptFromWorldEdit(CompoundTag tag) {
        // copy to container, no cloning used because it's unlikely to leak
        // and it's cheaper this way
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        for (Map.Entry<String, Tag<?, ?>> entry : tag.getValue().entrySet()) {
            container.set(DataQuery.of(entry.getKey()), adaptTagFromWorldEdit(entry.getValue()));
        }
        return container;
    }

    private static Object adaptTagFromWorldEdit(Tag<?, ?> value) {
        if (value instanceof ListTag<?, ?>) {
            return ((ListTag<?, ?>) value).getValue().stream()
                .map(NbtAdapter::adaptTagFromWorldEdit)
                .collect(Collectors.toList());
        }
        if (value instanceof CompoundTag) {
            return adaptFromWorldEdit(((CompoundTag) value));
        }
        // everything else is raw JDK types, so we can use it directly
        return value.getValue();
    }

    public static net.minecraft.nbt.Tag adaptNMSToWorldEdit(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return adaptNMSToWorldEdit((IntArrayTag) tag);

        } else if (tag instanceof ListTag) {
            return adaptNMSToWorldEdit((ListTag) tag);

        } else if (tag instanceof LongTag) {
            return adaptNMSToWorldEdit((LongTag) tag);

        } else if (tag instanceof LongArrayTag) {
            return adaptNMSToWorldEdit((LongArrayTag) tag);

        } else if (tag instanceof StringTag) {
            return adaptNMSToWorldEdit((StringTag) tag);

        } else if (tag instanceof IntTag) {
            return adaptNMSToWorldEdit((IntTag) tag);

        } else if (tag instanceof ByteTag) {
            return adaptNMSToWorldEdit((ByteTag) tag);

        } else if (tag instanceof ByteArrayTag) {
            return adaptNMSToWorldEdit((ByteArrayTag) tag);

        } else if (tag instanceof CompoundTag) {
            return adaptNMSToWorldEdit((CompoundTag) tag);

        } else if (tag instanceof FloatTag) {
            return adaptNMSToWorldEdit((FloatTag) tag);

        } else if (tag instanceof ShortTag) {
            return adaptNMSToWorldEdit((ShortTag) tag);

        } else if (tag instanceof DoubleTag) {
            return adaptNMSToWorldEdit((DoubleTag) tag);
        } else {
            throw new IllegalArgumentException("Can't convert tag of type " + tag.getClass().getCanonicalName());
        }
    }

    public static net.minecraft.nbt.IntArrayTag adaptNMSToWorldEdit(IntArrayTag tag) {
        int[] value = tag.getValue();
        return new net.minecraft.nbt.IntArrayTag(Arrays.copyOf(value, value.length));
    }

    public static net.minecraft.nbt.ListTag adaptNMSToWorldEdit(ListTag<?, ?> tag) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (Tag<?, ?> child : tag.getValue()) {
            if (child instanceof EndTag) {
                continue;
            }
            list.add(adaptNMSToWorldEdit(child));
        }
        return list;
    }

    public static net.minecraft.nbt.LongTag adaptNMSToWorldEdit(LongTag tag) {
        return net.minecraft.nbt.LongTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.LongArrayTag adaptNMSToWorldEdit(LongArrayTag tag) {
        return new net.minecraft.nbt.LongArrayTag(tag.getValue().clone());
    }

    public static net.minecraft.nbt.StringTag adaptNMSToWorldEdit(StringTag tag) {
        return net.minecraft.nbt.StringTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.IntTag adaptNMSToWorldEdit(IntTag tag) {
        return net.minecraft.nbt.IntTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.ByteTag adaptNMSToWorldEdit(ByteTag tag) {
        return net.minecraft.nbt.ByteTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.ByteArrayTag adaptNMSToWorldEdit(ByteArrayTag tag) {
        return new net.minecraft.nbt.ByteArrayTag(tag.getValue().clone());
    }

    public static net.minecraft.nbt.CompoundTag adaptNMSToWorldEdit(CompoundTag tag) {
        net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
        for (Map.Entry<String, Tag<?, ?>> child : tag.getValue().entrySet()) {
            compound.put(child.getKey(), adaptNMSToWorldEdit(child.getValue()));
        }
        return compound;
    }

    public static net.minecraft.nbt.FloatTag adaptNMSToWorldEdit(FloatTag tag) {
        return net.minecraft.nbt.FloatTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.ShortTag adaptNMSToWorldEdit(ShortTag tag) {
        return net.minecraft.nbt.ShortTag.valueOf(tag.getValue());
    }

    public static net.minecraft.nbt.DoubleTag adaptNMSToWorldEdit(DoubleTag tag) {
        return net.minecraft.nbt.DoubleTag.valueOf(tag.getValue());
    }
}
