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

package com.sk89q.worldedit.patterns;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.RandomPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @deprecated See {@link RandomPattern}
 */
@Deprecated
public class RandomFillPattern implements Pattern {
    /**
     * Random number generator.
     */
    private static final Random random = new Random();
    /**
     * Blocks and their proportions.
     */
    private List<BlockChance> blocks;

    /**
     * Construct the object.
     *
     * @param blocks
     */
    public RandomFillPattern(List<BlockChance> blocks) {
        double max = 0;

        for (BlockChance block : blocks) {
            max += block.getChance();
        }

        List<BlockChance> finalBlocks = new ArrayList<BlockChance>();

        double i = 0;

        for (BlockChance block : blocks) {
            double v = block.getChance() / max;
            i += v;
            finalBlocks.add(new BlockChance(block.getBlock(), i));
        }

        this.blocks = finalBlocks;
    }

    /**
     * Get next block.
     *
     * @param pos
     * @return
     */
    public BaseBlock next(Vector pos) {
        double r = random.nextDouble();

        for (BlockChance block : blocks) {
            if (r <= block.getChance()) {
                return block.getBlock();
            }
        }

        throw new RuntimeException("ProportionalFillPattern");
    }

    public BaseBlock next(int x, int y, int z) {
        return next(null);
    }
}
