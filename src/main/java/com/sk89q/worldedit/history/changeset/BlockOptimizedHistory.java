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

package com.sk89q.worldedit.history.changeset;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.util.collection.TupleArrayList;

import java.util.ArrayList;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Map.Entry;

/**
 * An extension of {@link ArrayListHistory} that stores {@link BlockChange}s
 * separately in two {@link ArrayList}s.
 * </p>
 * Whether this is a good idea or not is highly questionable, but this class
 * exists because this is how history was implemented in WorldEdit for
 * many years.
 */
public class BlockOptimizedHistory extends ArrayListHistory {

    private final TupleArrayList<BlockVector, BaseBlock> previous = new TupleArrayList<BlockVector, BaseBlock>();
    private final TupleArrayList<BlockVector, BaseBlock> current = new TupleArrayList<BlockVector, BaseBlock>();

    @Override
    public void add(Change change) {
        checkNotNull(change);

        if (change instanceof BlockChange) {
            BlockChange blockChange = (BlockChange) change;
            BlockVector position = blockChange.getPosition();
            previous.put(position, blockChange.getPrevious());
            current.put(position, blockChange.getCurrent());
        } else {
            super.add(change);
        }
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return Iterators.concat(
                super.forwardIterator(),
                Iterators.transform(current.iterator(), createTransform()));
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return Iterators.concat(
                super.backwardIterator(),
                Iterators.transform(previous.iterator(true), createTransform()));
    }

    @Override
    public int size() {
        return super.size() + previous.size();
    }

    /**
     * Create a function that transforms each entry from the double array lists' iterator
     * into an {@link Change}.
     *
     * @return a function
     */
    private Function<Entry<BlockVector, BaseBlock>, Change> createTransform() {
        return new Function<Entry<BlockVector, BaseBlock>, Change>() {
            @Override
            public Change apply(Entry<BlockVector, BaseBlock> entry) {
                return new BlockChange(entry.getKey(), entry.getValue(), entry.getValue());
            }
        };
    }

}
