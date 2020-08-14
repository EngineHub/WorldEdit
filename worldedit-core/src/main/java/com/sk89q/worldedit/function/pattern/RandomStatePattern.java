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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.FuzzyBlockState;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomStatePattern implements Pattern {

    private final Random rand = new Random();
    private final List<BaseBlock> blocks;

    public RandomStatePattern(FuzzyBlockState state) {
        blocks = state.getBlockType().getAllStates().stream().filter(state::equalsFuzzy)
                .map(BlockState::toBaseBlock).collect(Collectors.toList());
    }

    @Override
    public BaseBlock applyBlock(BlockVector3 position) {
        return blocks.get(rand.nextInt(blocks.size()));
    }
}
