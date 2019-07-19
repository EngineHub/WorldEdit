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
