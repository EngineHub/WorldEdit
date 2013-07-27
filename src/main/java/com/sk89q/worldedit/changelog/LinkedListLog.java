// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.changelog;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A type of {@link ChangeLog} that stores changes in a linked list containing a list
 * of {@link ReversibleChange}s.
 * 
 * <p>While this type of change log supports very free-form shapes, it is not the most
 * memory efficient.</p>
 */
public class LinkedListLog implements ChangeLog {
    
    private final LinkedList<ReversibleChange> changes = new LinkedList<ReversibleChange>();

    @Override
    public boolean isEmpty() {
        return changes.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return changes.contains(o);
    }

    @Override
    public int size() {
        return changes.size();
    }

    @Override
    public boolean add(ReversibleChange e) {
        return changes.add(e.clone());
    }

    @Override
    public boolean remove(Object o) {
        return changes.remove(o);
    }

    @Override
    public boolean addAll(Collection<? extends ReversibleChange> c) {
        return changes.addAll(c);
    }

    @Override
    public Iterator<ReversibleChange> iterator() {
        return changes.iterator();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return changes.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return changes.removeAll(c);
    }

    @Override
    public void clear() {
        changes.clear();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return changes.retainAll(c);
    }

    @Override
    public Object[] toArray() {
        return changes.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return changes.toArray(a);
    }

    @Override
    public Iterator<ReversibleChange> descendingIterator() {
        return changes.descendingIterator();
    }
    
}
