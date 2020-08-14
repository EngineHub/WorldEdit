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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Double array lists to work like a Map, but not really.
 *
 * <p>The usefulness of this class is highly questionable.</p>
 */
public class DoubleArrayList<A, B> implements Iterable<Map.Entry<A, B>> {

    private final List<A> listA = new ArrayList<>();
    private final List<B> listB = new ArrayList<>();
    private boolean isReversed = false;

    /**
     * Construct the object.
     *
     * @param isReversed true if the list should be reversed
     */
    public DoubleArrayList(boolean isReversed) {
        this.isReversed = isReversed;
    }

    /**
     * Add an item.
     *
     * @param a the first item
     * @param b the second item
     */
    public void put(A a, B b) {
        listA.add(a);
        listB.add(b);
    }

    /**
     * Get size.
     *
     * @return count of objects
     */
    public int size() {
        return listA.size();
    }

    /**
     * Clear the list.
     */
    public void clear() {
        listA.clear();
        listB.clear();
    }

    /**
     * Get an entry set.
     *
     * @return entry set
     */
    public Iterator<Map.Entry<A, B>> iterator(boolean reversed) {
        if (reversed) {
            return new ReverseEntryIterator<>(
                    listA.listIterator(listA.size()),
                    listB.listIterator(listB.size()));
        } else {
            return new ForwardEntryIterator<>(
                    listA.iterator(),
                    listB.iterator());
        }
    }

    @Override
    public Iterator<Map.Entry<A, B>> iterator() {
        return iterator(isReversed);
    }

    /**
     * Entry iterator.
     */
    public class ForwardEntryIterator<T extends Map.Entry<A, B>>
            implements Iterator<Map.Entry<A, B>> {

        private final Iterator<A> keyIterator;
        private final Iterator<B> valueIterator;

        public ForwardEntryIterator(Iterator<A> keyIterator, Iterator<B> valueIterator) {
            this.keyIterator = keyIterator;
            this.valueIterator = valueIterator;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        @Override
        public Map.Entry<A, B> next() throws NoSuchElementException {
            return new Entry<A, B>(keyIterator.next(), valueIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Entry iterator.
     */
    public class ReverseEntryIterator<T extends Map.Entry<A, B>>
            implements Iterator<Map.Entry<A, B>> {

        private final ListIterator<A> keyIterator;
        private final ListIterator<B> valueIterator;

        public ReverseEntryIterator(ListIterator<A> keyIterator, ListIterator<B> valueIterator) {
            this.keyIterator = keyIterator;
            this.valueIterator = valueIterator;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasPrevious();
        }

        @Override
        public Map.Entry<A, B> next() throws NoSuchElementException {
            return new Entry<A, B>(keyIterator.previous(), valueIterator.previous());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Class to masquerade as Map.Entry.
     */
    public class Entry<C, D> implements Map.Entry<A, B> {
        private final A key;
        private final B value;

        private Entry(A key, B value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public A getKey() {
            return key;
        }

        @Override
        public B getValue() {
            return value;
        }

        @Override
        public B setValue(B value) {
            throw new UnsupportedOperationException();
        }
    }

}
