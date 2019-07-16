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

package com.sk89q.worldedit.util.collection;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A space-efficient map implementation for block locations.
 */
public class BlockMap extends AbstractMap<BlockVector3, BaseBlock> {

    public static BlockMap create() {
        return new BlockMap();
    }

    public static BlockMap copyOf(Map<? extends BlockVector3, ? extends BaseBlock> source) {
        return new BlockMap(source);
    }

    private static final int WORLD_XZ_MINMAX = 30_000_000;
    private static final int WORLD_Y_MAX = 4095;

    /*
     * Stores blocks by sub-dividing them into smaller groups.
     * A block location is 26 bits long for x + z, and usually
     * 8 bits for y, although mods such as cubic chunks may
     * expand this to infinite. We support up to 12 bits of y.
     *
     * Each group uses 14 bits of x + z, and 4 bits of y.
     * This allows us to fit the group location neatly into a single int.
     * We store Y in the top bits, Z in the middle bits, and X in the low bits.
     *
     * This means that each group has 12 bits of x + z, and 8 bits of y.
     */

    private static final int BITS_14 = 0x3F_FF;
    private static final int BITS_12 = 0x0F_FF;
    private static final int BITS_8 = 0xFF;
    private static final int BITS_4 = 0x0F;

    private static boolean isHorizontallyOOB(int h) {
        return h < -WORLD_XZ_MINMAX || h > WORLD_XZ_MINMAX;
    }

    private static int toGroupKey(BlockVector3 location) {
        if (isHorizontallyOOB(location.getX()) ||
            isHorizontallyOOB(location.getZ()) ||
            location.getY() < 0 || location.getY() > WORLD_Y_MAX) {
            throw new IllegalArgumentException("Location exceeds OrderedBlockMap limits: " + location);
        }
        return ((location.getX() >>> 12) & BITS_14)
            | (((location.getZ() >>> 12) & BITS_14) << 14)
            | (((location.getY() >>> 8) & BITS_4) << (14 + 14));
    }

    private static int toInnerKey(BlockVector3 location) {
        return (location.getX() & BITS_12)
            | ((location.getZ() & BITS_12) << 12)
            | ((location.getY() & BITS_8) << (12 + 12));
    }

    private static final int GROUP_X = BITS_14;
    private static final int GROUP_Z = BITS_14 << 14;
    private static final int GROUP_Y = BITS_4 << (14 + 14);
    private static final int INNER_X = BITS_12;
    private static final int INNER_Z = BITS_12 << 12;
    private static final int INNER_Y = BITS_8 << (12 + 12);

    private static BlockVector3 reconstructLocation(int group, int inner) {
        int x = fixSign(((group & GROUP_X) << 12) | (inner & INNER_X));
        int z = fixSign(((group & GROUP_Z) >>> (14 - 12)) | ((inner & INNER_Z) >>> 12));
        int y = ((group & GROUP_Y) >>> (14 + 14 - 8)) | ((inner & INNER_Y) >>> (12 + 12));
        return BlockVector3.at(x, y, z);
    }

    private static final int FIX_SIGN_SHIFT = 32 - 26;

    /**
     * Fix horizontal sign -- we have a 26-bit two's-complement int,
     * we need it to be a 32-bit two's-complement int.
     */
    private static int fixSign(int h) {
        // Using https://stackoverflow.com/a/29266331/436524
        return (h << FIX_SIGN_SHIFT) >> FIX_SIGN_SHIFT;
    }

    private final Map<Integer, Map<Integer, BaseBlock>> maps = new HashMap<>();
    private Set<Entry<BlockVector3, BaseBlock>> entrySet;
    private Collection<BaseBlock> values;

    private BlockMap() {
    }

    private BlockMap(Map<? extends BlockVector3, ? extends BaseBlock> source) {
        putAll(source);
    }

    private Map<Integer, BaseBlock> getOrCreateMap(int groupKey) {
        return maps.computeIfAbsent(groupKey, k -> new HashMap<>());
    }

    private Map<Integer, BaseBlock> getOrEmptyMap(int groupKey) {
        return maps.getOrDefault(groupKey, ImmutableMap.of());
    }

    /**
     * Apply the function the the map at {@code groupKey}, and if the function empties the map,
     * delete it from {@code maps}.
     */
    private <R> R cleanlyModifyMap(int groupKey, Function<Map<Integer, BaseBlock>, R> func) {
        Map<Integer, BaseBlock> map = maps.get(groupKey);
        if (map != null) {
            R result = func.apply(map);
            if (map.isEmpty()) {
                maps.remove(groupKey);
            }
            return result;
        }
        map = new HashMap<>();
        R result = func.apply(map);
        if (!map.isEmpty()) {
            maps.put(groupKey, map);
        }
        return result;
    }

    @Override
    public BaseBlock put(BlockVector3 key, BaseBlock value) {
        return getOrCreateMap(toGroupKey(key)).put(toInnerKey(key), value);
    }

    @Override
    public BaseBlock getOrDefault(Object key, BaseBlock defaultValue) {
        BlockVector3 vec = (BlockVector3) key;
        return getOrEmptyMap(toGroupKey(vec))
            .getOrDefault(toInnerKey(vec), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super BlockVector3, ? super BaseBlock> action) {
        maps.forEach((groupKey, m) ->
            m.forEach((innerKey, block) ->
                action.accept(reconstructLocation(groupKey, innerKey), block)
            )
        );
    }

    @Override
    public void replaceAll(BiFunction<? super BlockVector3, ? super BaseBlock, ? extends BaseBlock> function) {
        maps.forEach((groupKey, m) ->
            m.replaceAll((innerKey, block) ->
                function.apply(reconstructLocation(groupKey, innerKey), block)
            )
        );
    }

    @Override
    public BaseBlock putIfAbsent(BlockVector3 key, BaseBlock value) {
        return getOrCreateMap(toGroupKey(key)).putIfAbsent(toInnerKey(key), value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        BlockVector3 vec = (BlockVector3) key;
        return cleanlyModifyMap(toGroupKey(vec),
            map -> map.remove(toInnerKey(vec), value));
    }

    @Override
    public boolean replace(BlockVector3 key, BaseBlock oldValue, BaseBlock newValue) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.replace(toInnerKey(key), oldValue, newValue));
    }

    @Override
    public BaseBlock replace(BlockVector3 key, BaseBlock value) {
        return getOrCreateMap(toGroupKey(key)).replace(toInnerKey(key), value);
    }

    @Override
    public BaseBlock computeIfAbsent(BlockVector3 key, Function<? super BlockVector3, ? extends BaseBlock> mappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.computeIfAbsent(toInnerKey(key), ik -> mappingFunction.apply(key)));
    }

    @Override
    public BaseBlock computeIfPresent(BlockVector3 key, BiFunction<? super BlockVector3, ? super BaseBlock, ? extends BaseBlock> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.computeIfPresent(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, block)));
    }

    @Override
    public BaseBlock compute(BlockVector3 key, BiFunction<? super BlockVector3, ? super BaseBlock, ? extends BaseBlock> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.compute(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, block)));
    }

    @Override
    public BaseBlock merge(BlockVector3 key, BaseBlock value, BiFunction<? super BaseBlock, ? super BaseBlock, ? extends BaseBlock> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.merge(toInnerKey(key), value, remappingFunction));
    }

    @Override
    public Set<Entry<BlockVector3, BaseBlock>> entrySet() {
        Set<Entry<BlockVector3, BaseBlock>> es = entrySet;
        if (es == null) {
            entrySet = es = new AbstractSet<Entry<BlockVector3, BaseBlock>>() {
                @Override
                public Iterator<Entry<BlockVector3, BaseBlock>> iterator() {
                    return maps.entrySet().stream()
                        .flatMap(ge -> {
                            int groupKey = ge.getKey();
                            return ge.getValue().entrySet().stream()
                                .map(ie -> (Entry<BlockVector3, BaseBlock>)
                                    new LazyEntry(groupKey, ie.getKey(), ie.getValue()));
                        })
                        .iterator();
                }

                @Override
                public int size() {
                    return BlockMap.this.size();
                }
            };
        }
        return es;
    }

    private final class LazyEntry implements Map.Entry<BlockVector3, BaseBlock> {

        private final int groupKey;
        private final int innerKey;
        private BlockVector3 lazyKey;
        private BaseBlock value;

        private LazyEntry(int groupKey, int innerKey, BaseBlock value) {
            this.groupKey = groupKey;
            this.innerKey = innerKey;
            this.value = value;
        }

        @Override
        public BlockVector3 getKey() {
            BlockVector3 result = lazyKey;
            if (result == null) {
                lazyKey = result = reconstructLocation(groupKey, innerKey);
            }
            return result;
        }

        @Override
        public BaseBlock getValue() {
            return value;
        }

        @Override
        public BaseBlock setValue(BaseBlock value) {
            this.value = value;
            return getOrCreateMap(groupKey).put(innerKey, value);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            if (o instanceof LazyEntry) {
                LazyEntry otherE = (LazyEntry) o;
                return otherE.groupKey == groupKey
                    && otherE.innerKey == innerKey
                    && Objects.equals(value, e.getValue());
            }
            return Objects.equals(getKey(), e.getKey()) && Objects.equals(value, e.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        return maps.values().stream().anyMatch(m -> m.containsValue(value));
    }

    @Override
    public boolean containsKey(Object key) {
        BlockVector3 vec = (BlockVector3) key;
        Map<Integer, BaseBlock> activeMap = maps.get(toGroupKey(vec));
        if (activeMap == null) {
            return false;
        }
        return activeMap.containsKey(toInnerKey(vec));
    }

    @Override
    public BaseBlock get(Object key) {
        BlockVector3 vec = (BlockVector3) key;
        Map<Integer, BaseBlock> activeMap = maps.get(toGroupKey(vec));
        if (activeMap == null) {
            return null;
        }
        return activeMap.get(toInnerKey(vec));
    }

    @Override
    public BaseBlock remove(Object key) {
        BlockVector3 vec = (BlockVector3) key;
        Map<Integer, BaseBlock> activeMap = maps.get(toGroupKey(vec));
        if (activeMap == null) {
            return null;
        }
        BaseBlock removed = activeMap.remove(toInnerKey(vec));
        if (activeMap.isEmpty()) {
            maps.remove(toGroupKey(vec));
        }
        return removed;
    }

    @Override
    public void putAll(Map<? extends BlockVector3, ? extends BaseBlock> m) {
        if (m instanceof BlockMap) {
            // optimize insertions:
            ((BlockMap) m).maps.forEach((groupKey, map) ->
                getOrCreateMap(groupKey).putAll(map)
            );
        } else {
            super.putAll(m);
        }
    }

    @Override
    public void clear() {
        maps.clear();
    }

    @Override
    public int size() {
        return maps.values().stream().mapToInt(Map::size).sum();
    }

    // no keySet override, since we can't really optimize it.
    // we can optimize values access though, by skipping BV construction.

    @Override
    public Collection<BaseBlock> values() {
        Collection<BaseBlock> vs = values;
        if (vs == null) {
            values = vs = new AbstractCollection<BaseBlock>() {
                @Override
                public Iterator<BaseBlock> iterator() {
                    return maps.values().stream()
                        .flatMap(m -> m.values().stream())
                        .iterator();
                }

                @Override
                public int size() {
                    return BlockMap.this.size();
                }
            };
        }
        return vs;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BlockMap) {
            // optimize by skipping entry translations:
            return maps.equals(((BlockMap) o).maps);
        }
        return super.equals(o);
    }

    // satisfy checkstyle
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
