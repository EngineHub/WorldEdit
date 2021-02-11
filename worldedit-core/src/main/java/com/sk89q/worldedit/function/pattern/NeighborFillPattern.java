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

package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.HashMap;
import java.util.Map;

public class NeighborFillPattern extends AbstractExtentPattern {

    private final Map<BlockState, Integer> nearby = new HashMap<>();

    private final int radius;

    public NeighborFillPattern(Extent extent, int radius) {
        super(extent);

        this.radius = radius;
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        BaseBlock existing = getExtent().getFullBlock(position);
        if (!existing.getBlockType().getMaterial().isAir()) {
            return existing;
        }

        nearby.clear();

        BlockState highestState = null;
        int highestCount = Integer.MIN_VALUE;

        for (int y = -radius; y < radius + 1; y++) {
            for (int x = -radius; x < radius + 1; x++) {
                for (int z = -radius; z < radius + 1; z++) {
                    BlockState state = getExtent().getBlock(position.add(x, y, z));
                    if (!state.getBlockType().getMaterial().isAir()) {
                        int count = nearby.getOrDefault(state, 0) + 1;
                        nearby.put(state, count + 1);

                        if (count > highestCount) {
                            highestState = state;
                        }
                    }
                }
            }
        }

        if (highestState != null) {
            return highestState.toBaseBlock();
        }

        return existing;
    }
}
