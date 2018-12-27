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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper around a list of blocks located in the world.
 */
public class LocatedBlockList implements Iterable<LocatedBlock> {

    private final List<LocatedBlock> list;

    public LocatedBlockList() {
        list = new ArrayList<>();
    }

    public LocatedBlockList(Collection<? extends LocatedBlock> collection) {
        list = new ArrayList<>(collection);
    }

    public void add(LocatedBlock setBlockCall) {
        checkNotNull(setBlockCall);
        list.add(setBlockCall);
    }

    public <B extends BlockStateHolder<B>> void add(BlockVector3 location, B block) {
        add(new LocatedBlock(location, block.toBaseBlock()));
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

    @Override
    public Iterator<LocatedBlock> iterator() {
        return list.iterator();
    }

    public Iterator<LocatedBlock> reverseIterator() {
        return new Iterator<LocatedBlock>() {

            private final ListIterator<LocatedBlock> backingIterator = list.listIterator(list.size());

            @Override
            public boolean hasNext() {
                return backingIterator.hasPrevious();
            }

            @Override
            public LocatedBlock next() {
                return backingIterator.previous();
            }
        };
    }

}
