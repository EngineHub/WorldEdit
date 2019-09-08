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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.util.collection.LocatedBlockList;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * An extension of {@link ArrayListHistory} that stores {@link BlockChange}s
 * separately in two {@link ArrayList}s.
 *
 * <p>Whether this is a good idea or not is highly questionable, but this class
 * exists because this is how history was implemented in WorldEdit for
 * many years.</p>
 */
public class BlockOptimizedHistory extends ArrayListHistory {

    private static Change createChange(LocatedBlock block) {
        return new BlockChange(block.getLocation(), block.getBlock(), block.getBlock());
    }

    private final LocatedBlockList previous = new LocatedBlockList();
    private final LocatedBlockList current = new LocatedBlockList();

    @Override
    public void add(Change change) {
        checkNotNull(change);

        if (change instanceof BlockChange) {
            BlockChange blockChange = (BlockChange) change;
            BlockVector3 position = blockChange.getPosition();
            if (!previous.containsLocation(position)) {
                previous.add(position, blockChange.getPrevious());
            }
            current.add(position, blockChange.getCurrent());
        } else {
            super.add(change);
        }
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return Iterators.concat(
                super.forwardIterator(),
                Iterators.transform(current.iterator(), BlockOptimizedHistory::createChange));
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return Iterators.concat(
                super.backwardIterator(),
                Iterators.transform(previous.reverseIterator(), BlockOptimizedHistory::createChange));
    }

    @Override
    public int size() {
        return super.size() + previous.size();
    }
}
