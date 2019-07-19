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

import com.google.common.collect.AbstractIterator;
import com.sk89q.worldedit.math.BlockVector3;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class VectorPositionList implements PositionList {

    private final List<BlockVector3> delegate = new ObjectArrayList<>();

    @Override
    public BlockVector3 get(int index) {
        return delegate.get(index);
    }

    @Override
    public void add(BlockVector3 vector) {
        delegate.add(vector);
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
        return delegate.iterator();
    }

    @Override
    public Iterator<BlockVector3> reverseIterator() {
        return new AbstractIterator<BlockVector3>() {

            private final ListIterator<BlockVector3> iterator = delegate.listIterator(size());

            @Override
            protected BlockVector3 computeNext() {
                return iterator.hasPrevious() ? iterator.previous() : endOfData();
            }
        };
    }
}
