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
 * An {@link ArrayList} that takes {@link Map.Entry}-like tuples. This class
 * exists for legacy reasons.
 *
 * @param <A> the first type in the tuple
 * @param <B> the second type in the tuple
 */
public class TupleArrayList<A, B> extends ArrayList<Map.Entry<A, B>> {

    /**
     * Add an item to the list.
     *
     * @param a the 'key'
     * @param b the 'value'
     */
    public void put(A a, B b) {
        add(new Tuple<>(a, b));
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

    @Override
    public Iterator<Map.Entry<A, B>> iterator() {
        return FastListIterator.forwardIterator(this);
    }

    /**
     * Return an entry iterator that traverses in the reverse direction.
     *
     * @return an entry iterator
     */
    public Iterator<Map.Entry<A, B>> reverseIterator() {
        return FastListIterator.reverseIterator(this);
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
