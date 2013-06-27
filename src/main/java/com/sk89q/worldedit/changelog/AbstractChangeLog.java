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

/**
 * An abstract implementation of {@link ChangeLog}.
 */
public abstract class AbstractChangeLog implements ChangeLog {

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("retainAll() not supported");
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (ReversibleChange change : this) {
            if (change == o) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object change : c) {
            if (change instanceof ReversibleChange
                    && !contains((ReversibleChange) change)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends ReversibleChange> c) {
        for (ReversibleChange change : c) {
            add(change);
        }
        return c.size() > 0;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll() not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll() not supported");
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        toArray(array);
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int i = 0;
        for (ReversibleChange change : this) {
            a[i] = (T) change;
        }
        return a;
    }

}
