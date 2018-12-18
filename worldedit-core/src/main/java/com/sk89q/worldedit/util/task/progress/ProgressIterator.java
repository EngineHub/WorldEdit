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

package com.sk89q.worldedit.util.task.progress;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An iterator that keeps track of how many entries have been visited and
 * calculates a "percent completed" using a provided total count.
 *
 * <p>The returned progress percentage will always be between 0 or 1
 * (inclusive). If the iterator returns more entries than the total count,
 * then 100% will be returned for the progress.</p>
 *
 * @param <V> the type
 */
public class ProgressIterator<V> implements Iterator<V>, ProgressObservable {

    private final Iterator<V> iterator;
    private final int count;
    private int visited = 0;

    /**
     * Create a new instance.
     *
     * @param iterator the iterator
     * @param count the count
     */
    private ProgressIterator(Iterator<V> iterator, int count) {
        checkNotNull(iterator);
        this.iterator = iterator;
        this.count = count;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public V next() {
        V value = iterator.next();
        visited++;
        return value;
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public Progress getProgress() {
        return Progress.of(count > 0 ? Math.min(1, Math.max(0, (visited / (double) count))) : 1);
    }

    /**
     * Create a new instance.
     *
     * @param iterator the iterator
     * @param count the number of objects
     * @param <V> the type
     * @return an instance
     */
    public static <V> ProgressIterator<V> create(Iterator<V> iterator, int count) {
        return new ProgressIterator<V>(iterator, count);
    }

    /**
     * Create a new instance from a list.
     *
     * @param list a list
     * @param <V> the type
     * @return an instance
     */
    public static <V> ProgressIterator<V> create(List<V> list) {
        return create(list.iterator(), list.size());
    }

}
