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

import com.google.common.collect.AbstractIterator;
import com.sk89q.worldedit.math.BlockVector3;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongListIterator;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

class LongPositionList implements PositionList {

    private final LongList delegate = new LongArrayList();

    @Override
    public BlockVector3 get(int index) {
        return BlockVector3.fromLongPackedForm(delegate.getLong(index));
    }

    @Override
    public void add(BlockVector3 vector) {
        delegate.add(vector.toLongPackedForm());
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Iterator<BlockVector3> iterator() {
        return new PositionIterator(delegate.iterator(),
            LongListIterator::hasNext,
            LongListIterator::nextLong);
    }

    @Override
    public Iterator<BlockVector3> reverseIterator() {
        return new PositionIterator(delegate.listIterator(size()),
            LongListIterator::hasPrevious,
            LongListIterator::previousLong);
    }

    private static final class PositionIterator extends AbstractIterator<BlockVector3> {

        private final LongListIterator iterator;
        private final Predicate<LongListIterator> hasNext;
        private final ToLongFunction<LongListIterator> next;

        private PositionIterator(LongListIterator iterator,
                                 Predicate<LongListIterator> hasNext,
                                 ToLongFunction<LongListIterator> next) {
            this.iterator = iterator;
            this.hasNext = hasNext;
            this.next = next;
        }

        @Override
        protected BlockVector3 computeNext() {
            return hasNext.test(iterator)
                ? BlockVector3.fromLongPackedForm(next.applyAsLong(iterator))
                : endOfData();
        }
    }
}
