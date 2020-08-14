/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockDistributionCounter implements RegionFunction {

    private final Extent extent;
    private final boolean separateStates;

    private final List<Countable<BlockState>> distribution = new ArrayList<>();
    private final Map<BlockState, Countable<BlockState>> map = new HashMap<>();

    public BlockDistributionCounter(Extent extent, boolean separateStates) {
        this.extent = extent;
        this.separateStates = separateStates;
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        BlockState blk = extent.getBlock(position);
        if (!separateStates) {
            blk = blk.getBlockType().getDefaultState();
        }

        if (map.containsKey(blk)) {
            map.get(blk).increment();
        } else {
            Countable<BlockState> c = new Countable<>(blk, 1);
            map.put(blk, c);
            distribution.add(c);
        }

        return true;
    }

    /**
     * Gets the distribution list.
     *
     * @return The distribution
     */
    public List<Countable<BlockState>> getDistribution() {
        Collections.sort(distribution);
        Collections.reverse(distribution);
        return this.distribution;
    }
}
