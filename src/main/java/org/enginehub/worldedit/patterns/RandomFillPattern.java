// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.worldedit.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Pattern proportionally fills.
 */
public class RandomFillPattern implements Pattern {
    
    private static final Random random = new Random();
    private List<BlockChance> blocks;

    /**
     * Construct the object.
     *
     * @param blocks the list of block chances
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

    @Override
    public BaseBlock next(Vector pos) {
        double r = random.nextDouble();

        for (BlockChance block : blocks) {
            if (r <= block.getChance()) {
                return block.getBlock();
            }
        }

        // This is never called
        throw new RuntimeException("ProportionalFillPattern");
    }
    
}
