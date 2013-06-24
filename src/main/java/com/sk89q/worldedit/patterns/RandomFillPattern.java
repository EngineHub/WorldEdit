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

package com.sk89q.worldedit.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Randomly returns blocks depending on given probabilities for a list of blocks.
 */
public class RandomFillPattern implements Pattern {

    private static final Random random = new Random();
    private final List<BlockChance> blocks;

    /**
     * Construct the object.
     *
     * @param blocks a list of block probabilities
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
     * Get the list of block probabilities.
     * 
     * @return the list of block probabilities
     */
    public List<BlockChance> getBlocks() {
        return blocks;
    }

    @Override
    public BaseBlock next(Vector pos) {
        double r = random.nextDouble();

        for (BlockChance block : blocks) {
            if (r <= block.getChance()) {
                return block.getBlock();
            }
        }

        throw new RuntimeException("ProportionalFillPattern");
    }

    @Override
    public BaseBlock next(int x, int y, int z) {
        return next(null);
    }

    @Override
    public String toString() {
        return String.format("RandomFillPattern(blocks=%s)", blocks);
    }
    
}
