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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.LocatedBlock;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wrapper around a list of blocks located in the world.
 */
public class LocatedBlockList implements Iterable<LocatedBlock> {

    private final Map<BlockVector3, BaseBlock> map = new LinkedHashMap<>();

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
        map.put(location, block.toBaseBlock());
    }

    public boolean containsLocation(BlockVector3 location) {
        return map.containsKey(location);
    }

    public @Nullable BaseBlock get(BlockVector3 location) {
        return map.get(location);
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<LocatedBlock> iterator() {
        return Iterators.transform(map.entrySet().iterator(), e -> new LocatedBlock(e.getKey(), e.getValue()));
    }

    public Iterator<LocatedBlock> reverseIterator() {
        List<LocatedBlock> data = Arrays.asList(Iterators.toArray(iterator(), LocatedBlock.class));
        Collections.reverse(data);
        return data.iterator();
    }

}
