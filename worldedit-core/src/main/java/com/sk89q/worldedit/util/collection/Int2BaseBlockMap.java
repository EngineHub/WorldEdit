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
import javax.annotation.Nonnull;

/**
 * Int-to-BaseBlock map, but with optimizations for common cases.
 */
class Int2BaseBlockMap extends AbstractInt2ObjectMap<BaseBlock> {

    /**
     * Given a {@link BaseBlock}, retrieve the internal ID if it's useful,
     * i.e. the block has no NBT data.
     *
     * @param block the block to get the ID for
     * @return the internal ID, or {@link BlockStateIdAccess#invalidId()} if not useful
     */
    private static int optimizedInternalId(BaseBlock block) {
        if (block.getNbtReference() != null) {
            return BlockStateIdAccess.invalidId();
        }
        return BlockStateIdAccess.getBlockStateId(block.toImmutableState());
    }

    private static BaseBlock assumeAsBlock(int id) {
        if (!BlockStateIdAccess.isValidInternalId(id)) {
            return null;
        }
        BlockState state = BlockStateIdAccess.getBlockStateById(id);
        if (state == null) {
            throw new IllegalStateException("No state for ID " + id);
        }
        return state.toBaseBlock();
    }

    private final Int2IntMap commonMap = new Int2IntOpenHashMap(64, 0.9f);
    private final Int2ObjectMap<BaseBlock> uncommonMap = new Int2ObjectOpenHashMap<>(1, 0.75f);

    {
        commonMap.defaultReturnValue(BlockStateIdAccess.invalidId());
    }

    @Override
    public int size() {
        return commonMap.size() + uncommonMap.size();
    }

    @Override
    public ObjectSet<Entry<BaseBlock>> int2ObjectEntrySet() {
        return new AbstractObjectSet<Entry<BaseBlock>>() {
            @Override
            @Nonnull
            public ObjectIterator<Entry<BaseBlock>> iterator() {
                return new ObjectIterator<Entry<BaseBlock>>() {

                    private final ObjectIterator<Int2IntMap.Entry> commonIter
                        = Int2IntMaps.fastIterator(commonMap);
                    private final ObjectIterator<Entry<BaseBlock>> uncommonIter
                        = Int2ObjectMaps.fastIterator(uncommonMap);
                    private boolean lastNextFromCommon = false;

                    @Override
                    public boolean hasNext() {
                        return commonIter.hasNext() || uncommonIter.hasNext();
                    }

                    @Override
                    public Entry<BaseBlock> next() {
                        if (commonIter.hasNext()) {
                            Int2IntMap.Entry e = commonIter.next();
                            lastNextFromCommon = true;
                            return new BasicEntry<>(
                                e.getIntKey(), assumeAsBlock(e.getIntValue())
                            );
                        }
                        if (uncommonIter.hasNext()) {
                            lastNextFromCommon = false;
                            return uncommonIter.next();
                        }
                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        if (lastNextFromCommon) {
                            commonIter.remove();
                        } else {
                            uncommonIter.remove();
                        }
                    }
                };
            }

            @Override
            public int size() {
                return Int2BaseBlockMap.this.size();
            }
        };
    }

    @Override
    public BaseBlock get(int key) {
        int oldId = commonMap.get(key);
        if (!BlockStateIdAccess.isValidInternalId(oldId)) {
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
        int internalId = optimizedInternalId(block);
        if (!BlockStateIdAccess.isValidInternalId(internalId)) {
            return uncommonMap.containsValue(block);
        }
        return commonMap.containsValue(internalId);
    }

    @Override
    public BaseBlock put(int key, BaseBlock value) {
        int internalId = optimizedInternalId(value);
        if (!BlockStateIdAccess.isValidInternalId(internalId)) {
            BaseBlock old = uncommonMap.put(key, value);
            if (old == null) {
                // ensure common doesn't have the entry too
                int oldId = commonMap.remove(key);
                return assumeAsBlock(oldId);
            }
            return old;
        }
        int oldId = commonMap.put(key, internalId);
        return assumeAsBlock(oldId);
    }

    @Override
    public BaseBlock remove(int key) {
        int removed = commonMap.remove(key);
        if (!BlockStateIdAccess.isValidInternalId(removed)) {
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
            int internalId = optimizedInternalId(value);
            if (!BlockStateIdAccess.isValidInternalId(internalId)) {
                uncommonMap.put(next.getIntKey(), value);
                iter.remove();
            } else {
                next.setValue(internalId);
            }
        }
        for (ObjectIterator<Entry<BaseBlock>> iter = Int2ObjectMaps.fastIterator(uncommonMap);
             iter.hasNext(); ) {
            Entry<BaseBlock> next = iter.next();
            BaseBlock value = function.apply(next.getIntKey(), next.getValue());
            int internalId = optimizedInternalId(value);
            if (!BlockStateIdAccess.isValidInternalId(internalId)) {
                next.setValue(value);
            } else {
                commonMap.put(next.getIntKey(), internalId);
                iter.remove();
            }
        }
    }
}
