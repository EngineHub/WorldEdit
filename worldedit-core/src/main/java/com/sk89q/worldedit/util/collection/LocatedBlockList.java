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

import com.google.common.collect.Iterators;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wrapper around a list of blocks located in the world.
 */
public class LocatedBlockList implements Iterable<LocatedBlock> {

    private final BlockMap<BaseBlock> blocks = BlockMap.createForBaseBlock();
    private final PositionList order = PositionList.create(
        WorldEdit.getInstance().getConfiguration().extendedYLimit
    );

    public LocatedBlockList() {
    }

    public LocatedBlockList(Collection<? extends LocatedBlock> collection) {
        for (LocatedBlock locatedBlock : collection) {
            add(locatedBlock.getLocation(), locatedBlock.getBlock());
        }
    }

    public void add(LocatedBlock setBlockCall) {
        checkNotNull(setBlockCall);
        add(setBlockCall.getLocation(), setBlockCall.getBlock());
    }

    public <B extends BlockStateHolder<B>> void add(BlockVector3 location, B block) {
        blocks.put(location, block.toBaseBlock());
        order.add(location);
    }

    public boolean containsLocation(BlockVector3 location) {
        return blocks.containsKey(location);
    }

    public @Nullable BaseBlock get(BlockVector3 location) {
        return blocks.get(location);
    }

    public int size() {
        return order.size();
    }

    public void clear() {
        blocks.clear();
        order.clear();
    }

    @Override
    public Iterator<LocatedBlock> iterator() {
        return Iterators.transform(order.iterator(), position ->
            new LocatedBlock(position, blocks.get(position)));
    }

    public Iterator<LocatedBlock> reverseIterator() {
        return Iterators.transform(order.reverseIterator(), position ->
            new LocatedBlock(position, blocks.get(position)));
    }

}
