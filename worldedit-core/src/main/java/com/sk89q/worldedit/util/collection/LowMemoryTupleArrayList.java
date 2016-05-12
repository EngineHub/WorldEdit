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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * An list like class that takes {@link Map.Entry}-like tuples. This class
 * exists for legacy reasons.
 *
 * @param <A> the first type in the tuple
 * @param <B> the second type in the tuple
 */
@Deprecated
public class LowMemoryTupleArrayList<A, B> implements Iterable<Map.Entry<A, B>> {

    private ArrayList<A> key = new ArrayList<A>();
    private ArrayList<B> value = new ArrayList<B>();

    /**
     * Add an item to the list.
     *
     * @param a the 'key'
     * @param b the 'value'
     */
    public void put(A a, B b) {
        key.add(a);
        value.add(b);
    }

    public void clear() {
        key.clear();
        value.clear();
    }

    public int size() {
        return key.size();
    }

    /**
     * Return an entry iterator that traverses in the reverse direction.
     *
     * @param reverse true to return the reverse iterator
     * @return an entry iterator
     */
    public Iterator<Map.Entry<A, B>> iterator(boolean reverse) {
        return reverse ? reverseIterator() : iterator();
    }

    /**
     * Return an entry iterator that traverses in the reverse direction.
     *
     * @return an entry iterator
     */
    public Iterator<Map.Entry<A, B>> reverseIterator() {
        return new Iterator<Map.Entry<A, B>>() {
            private int index = key.size();

            @Override
            public boolean hasNext() {
                return index != 0;
            }

            @Override
            public Map.Entry<A, B> next() {
                Tuple<A, B> result = new Tuple<A, B>(key.get(index - 1), value.get(index - 1));
                --index;
                return result;
            }

            public void remove() {
                key.remove(index);
                value.remove(index);
            }
        };
    }

    public Iterator<Map.Entry<A, B>> iterator() {
        return new Iterator<Map.Entry<A, B>>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index != key.size();
            }

            @Override
            public Map.Entry<A, B> next() {
                Tuple<A, B> result = new Tuple<A, B>(key.get(index), value.get(index));
                ++index;
                return result;
            }

            public void remove() {
                key.remove(index);
                value.remove(index);
            }
        };
    }

    private static class Tuple<A, B> implements Map.Entry<A, B> {
        private A key;
        private B value;

        private Tuple(A key, B value) {
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
