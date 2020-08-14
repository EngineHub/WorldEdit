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

package com.sk89q.worldedit.util.collection;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;
import static com.sk89q.worldedit.math.BitMath.fixSign;
import static com.sk89q.worldedit.math.BitMath.mask;

/**
 * A space-efficient map implementation for block locations.
 */
public class BlockMap<V> extends AbstractMap<BlockVector3, V> {

    public static <V> BlockMap<V> create() {
        return create(() -> new Int2ObjectOpenHashMap<>(64, 0.9f));
    }

    public static BlockMap<BaseBlock> createForBaseBlock() {
        return create(Int2BaseBlockMap::new);
    }

    private static <V> BlockMap<V> create(Supplier<Int2ObjectMap<V>> subMapSupplier) {
        return new BlockMap<>(subMapSupplier);
    }

    public static <V> BlockMap<V> copyOf(Map<? extends BlockVector3, ? extends V> source) {
        return new BlockMap<>(Int2ObjectOpenHashMap::new, source);
    }

    /*
     * Stores blocks by sub-dividing them into smaller groups.
     * A block location is 26 bits long for x + z, and usually
     * 8 bits for y, although mods such as cubic chunks may
     * expand this to infinite. We support up to 32 bits of y.
     *
     * Grouping key stores 20 bits x + z, 24 bits y.
     * Inner key stores 6 bits x + z, 8 bits y.
     * Order (lowest to highest) is x-z-y.
     */

    private static final long BITS_24 = mask(24);
    private static final long BITS_20 = mask(20);
    private static final int BITS_8 = mask(8);
    private static final int BITS_6 = mask(6);

    private static long toGroupKey(BlockVector3 location) {
        return ((location.getX() >>> 6) & BITS_20)
            | (((location.getZ() >>> 6) & BITS_20) << 20)
            | (((location.getY() >>> 8) & BITS_24) << (20 + 20));
    }

    private static int toInnerKey(BlockVector3 location) {
        return (location.getX() & BITS_6)
            | ((location.getZ() & BITS_6) << 6)
            | ((location.getY() & BITS_8) << (6 + 6));
    }

    private static final long GROUP_X = BITS_20;
    private static final long GROUP_Z = BITS_20 << 20;
    private static final long GROUP_Y = BITS_24 << (20 + 20);
    private static final int INNER_X = BITS_6;
    private static final int INNER_Z = BITS_6 << 6;
    private static final int INNER_Y = BITS_8 << (6 + 6);

    private static BlockVector3 reconstructLocation(long group, int inner) {
        int groupX = (int) ((group & GROUP_X) << 6);
        int x = fixSign(groupX | (inner & INNER_X), 26);
        int groupZ = (int) ((group & GROUP_Z) >>> (20 - 6));
        int z = fixSign(groupZ | ((inner & INNER_Z) >>> 6), 26);
        int groupY = (int) ((group & GROUP_Y) >>> (20 + 20 - 8));
        int y = groupY | ((inner & INNER_Y) >>> (6 + 6));
        return BlockVector3.at(x, y, z);
    }

    private final Long2ObjectMap<Int2ObjectMap<V>> maps = new Long2ObjectOpenHashMap<>(4, 0.75f);
    private final Supplier<Int2ObjectMap<V>> subMapSupplier;
    private Set<Entry<BlockVector3, V>> entrySet;
    private Collection<V> values;

    private BlockMap(Supplier<Int2ObjectMap<V>> subMapSupplier) {
        this.subMapSupplier = subMapSupplier;
    }

    private BlockMap(Supplier<Int2ObjectMap<V>> subMapSupplier, Map<? extends BlockVector3, ? extends V> source) {
        this.subMapSupplier = subMapSupplier;
        putAll(source);
    }

    private Int2ObjectMap<V> getOrCreateMap(long groupKey) {
        return maps.computeIfAbsent(groupKey, k -> subMapSupplier.get());
    }

    private Int2ObjectMap<V> getOrEmptyMap(long groupKey) {
        return maps.getOrDefault(groupKey, Int2ObjectMaps.emptyMap());
    }

    /**
     * Apply the function the the map at {@code groupKey}, and if the function empties the map,
     * delete it from {@code maps}.
     */
    private <R> R cleanlyModifyMap(long groupKey, Function<Int2ObjectMap<V>, R> func) {
        Int2ObjectMap<V> map = maps.get(groupKey);
        if (map != null) {
            R result = func.apply(map);
            if (map.isEmpty()) {
                maps.remove(groupKey);
            }
            return result;
        }
        map = subMapSupplier.get();
        R result = func.apply(map);
        if (!map.isEmpty()) {
            maps.put(groupKey, map);
        }
        return result;
    }

    @Override
    public V put(BlockVector3 key, V value) {
        return getOrCreateMap(toGroupKey(key)).put(toInnerKey(key), value);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        BlockVector3 vec = (BlockVector3) key;
        return getOrEmptyMap(toGroupKey(vec))
            .getOrDefault(toInnerKey(vec), defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super BlockVector3, ? super V> action) {
        maps.forEach((groupKey, m) ->
            m.forEach((innerKey, block) ->
                action.accept(reconstructLocation(groupKey, innerKey), block)
            )
        );
    }

    @Override
    public void replaceAll(BiFunction<? super BlockVector3, ? super V, ? extends V> function) {
        maps.forEach((groupKey, m) ->
            m.replaceAll((innerKey, block) ->
                function.apply(reconstructLocation(groupKey, innerKey), block)
            )
        );
    }

    @Override
    public V putIfAbsent(BlockVector3 key, V value) {
        return getOrCreateMap(toGroupKey(key)).putIfAbsent(toInnerKey(key), value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        BlockVector3 vec = (BlockVector3) key;
        return cleanlyModifyMap(toGroupKey(vec),
            map -> map.remove(toInnerKey(vec), value));
    }

    @Override
    public boolean replace(BlockVector3 key, V oldValue, V newValue) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.replace(toInnerKey(key), oldValue, newValue));
    }

    @Override
    public V replace(BlockVector3 key, V value) {
        return getOrCreateMap(toGroupKey(key)).replace(toInnerKey(key), value);
    }

    @Override
    public V computeIfAbsent(BlockVector3 key, Function<? super BlockVector3, ? extends V> mappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.computeIfAbsent(toInnerKey(key), ik -> mappingFunction.apply(key)));
    }

    @Override
    public V computeIfPresent(BlockVector3 key, BiFunction<? super BlockVector3, ? super V, ? extends V> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.computeIfPresent(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, block)));
    }

    @Override
    public V compute(BlockVector3 key, BiFunction<? super BlockVector3, ? super V, ? extends V> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.compute(toInnerKey(key), (ik, block) -> remappingFunction.apply(key, block)));
    }

    @Override
    public V merge(BlockVector3 key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return cleanlyModifyMap(toGroupKey(key),
            map -> map.merge(toInnerKey(key), value, remappingFunction));
    }

    @Override
    public Set<Entry<BlockVector3, V>> entrySet() {
        Set<Entry<BlockVector3, V>> es = entrySet;
        if (es == null) {
            entrySet = es = new AbstractSet<Entry<BlockVector3, V>>() {
                @Override
                public Iterator<Entry<BlockVector3, V>> iterator() {
                    return new Iterator<Entry<BlockVector3, V>>() {

                        private final ObjectIterator<Long2ObjectMap.Entry<Int2ObjectMap<V>>> primaryIterator
                            = Long2ObjectMaps.fastIterator(maps);
                        private Long2ObjectMap.Entry<Int2ObjectMap<V>> currentPrimaryEntry;
                        private ObjectIterator<Int2ObjectMap.Entry<V>> secondaryIterator;
                        private boolean finished;
                        private LazyEntry next;

                        @Override
                        public boolean hasNext() {
                            if (finished) {
                                return false;
                            }
                            if (next == null) {
                                LazyEntry proposedNext = computeNext();
                                if (proposedNext == null) {
                                    finished = true;
                                    return false;
                                }
                                next = proposedNext;
                            }
                            return true;
                        }

                        private LazyEntry computeNext() {
                            if (secondaryIterator == null || !secondaryIterator.hasNext()) {
                                if (!primaryIterator.hasNext()) {
                                    return null;
                                }

                                currentPrimaryEntry = primaryIterator.next();
                                secondaryIterator = Int2ObjectMaps.fastIterator(currentPrimaryEntry.getValue());
                                // be paranoid
                                checkState(secondaryIterator.hasNext(),
                                    "Should not have an empty map entry, it should have been removed!");
                            }
                            Int2ObjectMap.Entry<V> next = secondaryIterator.next();
                            return new LazyEntry(currentPrimaryEntry.getLongKey(), next.getIntKey(), next.getValue());
                        }

                        @Override
                        public Entry<BlockVector3, V> next() {
                            if (!hasNext()) {
                                throw new NoSuchElementException();
                            }
                            LazyEntry tmp = next;
                            next = null;
                            return tmp;
                        }

                        @Override
                        public void remove() {
                            secondaryIterator.remove();
                            // ensure invariants hold
                            if (currentPrimaryEntry.getValue().isEmpty()) {
                                // the remove call cleared this map. call remove on the primary iter
                                primaryIterator.remove();
                            }
                        }
                    };
                }

                @Override
                public int size() {
                    return BlockMap.this.size();
                }
            };
        }
        return es;
    }

    private final class LazyEntry implements Entry<BlockVector3, V> {

        private final long groupKey;
        private final int innerKey;
        private BlockVector3 lazyKey;
        private V value;

        private LazyEntry(long groupKey, int innerKey, V value) {
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
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return getOrCreateMap(groupKey).put(innerKey, value);
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry<?, ?>) o;
            if (o instanceof BlockMap.LazyEntry) {
                @SuppressWarnings("unchecked")
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
        Int2ObjectMap<V> activeMap = maps.get(toGroupKey(vec));
        if (activeMap == null) {
            return false;
        }
        return activeMap.containsKey(toInnerKey(vec));
    }

    @Override
    public V get(Object key) {
        BlockVector3 vec = (BlockVector3) key;
        Int2ObjectMap<V> activeMap = maps.get(toGroupKey(vec));
        if (activeMap == null) {
            return null;
        }
        return activeMap.get(toInnerKey(vec));
    }

    @Override
    public V remove(Object key) {
        BlockVector3 vec = (BlockVector3) key;
        long groupKey = toGroupKey(vec);
        Int2ObjectMap<V> activeMap = maps.get(groupKey);
        if (activeMap == null) {
            return null;
        }
        V removed = activeMap.remove(toInnerKey(vec));
        if (activeMap.isEmpty()) {
            maps.remove(groupKey);
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends BlockVector3, ? extends V> m) {
        if (m instanceof BlockMap) {
            // optimize insertions:
            ((BlockMap<V>) m).maps.forEach((groupKey, map) ->
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
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            values = vs = new AbstractCollection<V>() {
                @Override
                public Iterator<V> iterator() {
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
            @SuppressWarnings("unchecked")
            BlockMap<V> other = (BlockMap<V>) o;
            return maps.equals(other.maps);
        }
        return super.equals(o);
    }

    // satisfy checkstyle
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
