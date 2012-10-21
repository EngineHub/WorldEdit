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

package com.sk89q.worldedit.structures;

import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.patterns.Pattern;

/**
 * Generates fruit patches with leaves around a central fruit.
 */
public class FruitPatch implements Structure {

    private final Random random = new Random();
    private final Pattern fruit;
    
    /**
     * Create a new patch structure generator.
     * 
     * @param fruit the fruit
     */
    public FruitPatch(Pattern fruit) {
        this.fruit = fruit;
    }

    @Override
    public boolean generate(EditSession context, Vector position) throws MaxChangedBlocksException {
        int type = context.getBlock(position.add(0, -1, 0)).getType();

        if (type == BlockID.GRASS || type == BlockID.DIRT) {
            generatePatch(context, position);
            return true;
        }
        
        return false;
    }

    /**
     * Make a fruit patch at the given location.
     * 
     * @param basePos position
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private void generatePatch(EditSession context, Vector basePos) throws MaxChangedBlocksException {
        // BaseBlock logBlock = new BaseBlock(BlockID.LOG);
        BaseBlock leavesBlock = new BaseBlock(BlockID.LEAVES);

        // setBlock(basePos.subtract(0, 1, 0), logBlock);
        context.setBlockIfAir(basePos, leavesBlock);

        generateVine(context, basePos, basePos.add(0, 0, 1));
        generateVine(context, basePos, basePos.add(0, 0, -1));
        generateVine(context, basePos, basePos.add(1, 0, 0));
        generateVine(context, basePos, basePos.add(-1, 0, 0));
    }

    /**
     * Make the vine for the patch.
     * 
     * @param basePos position to start at
     * @param pos current position
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private void generateVine(EditSession context, Vector basePos, Vector pos) throws MaxChangedBlocksException {
        if (pos.distance(basePos) > 4) return;
        if (context.getBlockType(pos) != 0) return;

        for (int i = -1; i > -3; --i) {
            Vector testPos = pos.add(0, i, 0);
            if (context.getBlockType(testPos) == BlockID.AIR) {
                pos = testPos;
            } else {
                break;
            }
        }

        context.setBlockIfAir(pos, new BaseBlock(BlockID.LEAVES));

        int t = random.nextInt(4);
        int h = random.nextInt(3) - 1;

        Vector fruitPos;
        BaseBlock log = new BaseBlock(BlockID.LOG);

        switch (t) {
        case 0:
            // Possibly make the vine
            if (random.nextBoolean())
                generateVine(context, basePos, pos.add(1, 0, 0));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(1, h, -1), log);

            fruitPos = pos.add(0, 0, -1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 1:
            // Possibly make the vine
            if (random.nextBoolean())
                generateVine(context, basePos, pos.add(0, 0, 1));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(1, h, 0), log);

            fruitPos = pos.add(1, 0, 1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 2:
            // Possibly make the vine
            if (random.nextBoolean())
                generateVine(context, basePos, pos.add(0, 0, -1));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(-1, h, 0), log);
            
            fruitPos = pos.add(-1, 0, 1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 3:
            // Possibly make the vine
            if (random.nextBoolean())
                generateVine(context, basePos, pos.add(-1, 0, 0));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(-1, h, -1), log);
            
            fruitPos = pos.add(-1, 0, -1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;
        }
    }

}
