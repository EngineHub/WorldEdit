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

import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;

/**
 * Int-to-BaseBlock map, but with optimizations for common cases.
 */
class SubBlockMap extends AbstractInt2ObjectMap<BaseBlock> {

    private static boolean hasInt(BlockState b) {
        return BlockStateIdAccess.getBlockStateId(b).isPresent();
    }

    private static boolean isUncommon(BaseBlock block) {
        return block.hasNbtData() || !hasInt(block.toImmutableState());
    }

    private static int assumeAsInt(BlockState b) {
        return BlockStateIdAccess.getBlockStateId(b)
            .orElseThrow(() -> new IllegalStateException("Block state " + b + " did not have an ID"));
    }

    private static BaseBlock assumeAsBlock(int id) {
        if (id == Integer.MIN_VALUE) {
            return null;
        }
        BlockState state = BlockStateIdAccess.getBlockStateById(id);
        if (state == null) {
            throw new IllegalStateException("No state for ID " + id);
        }
        return state.toBaseBlock();
    }

    static final SubBlockMap EMPTY = new SubBlockMap();

    private final Int2IntMap commonMap = new Int2IntOpenHashMap(64, 1f);
    private final Int2ObjectMap<BaseBlock> uncommonMap = new Int2ObjectOpenHashMap<>(1, 1f);

    {
        commonMap.defaultReturnValue(Integer.MIN_VALUE);
    }

    @Override
    public int size() {
        return commonMap.size() + uncommonMap.size();
    }

    @Override
    public ObjectSet<Entry<BaseBlock>> int2ObjectEntrySet() {
        return new AbstractObjectSet<Entry<BaseBlock>>() {
            @Override
            public ObjectIterator<Entry<BaseBlock>> iterator() {
                return new ObjectIterator<Entry<BaseBlock>>() {

                    private final ObjectIterator<Int2IntMap.Entry> commonIter
                        = Int2IntMaps.fastIterator(commonMap);
                    private final ObjectIterator<Int2ObjectMap.Entry<BaseBlock>> uncommonIter
                        = Int2ObjectMaps.fastIterator(uncommonMap);

                    @Override
                    public boolean hasNext() {
                        return commonIter.hasNext() || uncommonIter.hasNext();
                    }

                    @Override
                    public Entry<BaseBlock> next() {
                        if (commonIter.hasNext()) {
                            Int2IntMap.Entry e = commonIter.next();
                            return new BasicEntry<>(
                                e.getIntKey(), assumeAsBlock(e.getIntValue())
                            );
                        }
                        if (uncommonIter.hasNext()) {
                            return uncommonIter.next();
                        }
                        throw new NoSuchElementException();
                    }
                };
            }

            @Override
            public int size() {
                return SubBlockMap.this.size();
            }
        };
    }

    @Override
    public BaseBlock get(int key) {
        int oldId = commonMap.get(key);
        if (oldId == Integer.MIN_VALUE) {
            return uncommonMap.get(key);
        }
        return assumeAsBlock(oldId);
    }

    @Override
    public boolean containsKey(int k) {
        return commonMap.containsKey(k) || uncommonMap.containsKey(k);
    }

    @Override
    public boolean containsValue(Object v) {
        BaseBlock block = (BaseBlock) v;
        if (isUncommon(block)) {
            return uncommonMap.containsValue(block);
        }
        return commonMap.containsValue(assumeAsInt(block.toImmutableState()));
    }

    @Override
    public BaseBlock put(int key, BaseBlock value) {
        if (isUncommon(value)) {
            BaseBlock old = uncommonMap.put(key, value);
            if (old == null) {
                // ensure common doesn't have the entry too
                int oldId = commonMap.remove(key);
                return assumeAsBlock(oldId);
            }
            return old;
        }
        int oldId = commonMap.put(key, assumeAsInt(value.toImmutableState()));
        return assumeAsBlock(oldId);
    }

    @Override
    public BaseBlock remove(int key) {
        int removed = commonMap.remove(key);
        if (removed == Integer.MIN_VALUE) {
            return uncommonMap.remove(key);
        }
        return assumeAsBlock(removed);
    }

    @Override
    public void replaceAll(BiFunction<? super Integer, ? super BaseBlock, ? extends BaseBlock> function) {
        for (ObjectIterator<Int2IntMap.Entry> iter = Int2IntMaps.fastIterator(commonMap);
             iter.hasNext(); ) {
            Int2IntMap.Entry next = iter.next();
            BaseBlock value = function.apply(next.getIntKey(), assumeAsBlock(next.getIntValue()));
            if (isUncommon(value)) {
                uncommonMap.put(next.getIntKey(), value);
                iter.remove();
            } else {
                next.setValue(assumeAsInt(value.toImmutableState()));
            }
        }
        for (ObjectIterator<Int2ObjectMap.Entry<BaseBlock>> iter = Int2ObjectMaps.fastIterator(uncommonMap);
             iter.hasNext(); ) {
            Int2ObjectMap.Entry<BaseBlock> next = iter.next();
            BaseBlock value = function.apply(next.getIntKey(), next.getValue());
            if (isUncommon(value)) {
                next.setValue(value);
            } else {
                commonMap.put(next.getIntKey(), assumeAsInt(value.toImmutableState()));
                iter.remove();
            }
        }
    }
}
