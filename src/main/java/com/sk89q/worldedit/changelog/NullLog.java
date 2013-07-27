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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link ChangeLog} that remembers nothing.
 */
public class NullLog extends AbstractChangeLog {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean add(ReversibleChange e) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public Iterator<ReversibleChange> iterator() {
        return new Iterator<ReversibleChange>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ReversibleChange next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterator<ReversibleChange> descendingIterator() {
        return iterator();
    }

}
