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

package com.sk89q.worldedit.operations;

import java.util.Iterator;
import java.util.Random;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

/**
 * Generate fruit patches that have leaves around them.
 */
public class GenerateFruitPatches implements Operation, BlockChange {

    private static final Random random = new Random();
    
    private final EditSession context;
    private final Region region;
    private final Pattern fruit;  
    
    private int affected = 0;
    
    /**
     * Create a patch generation operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     * @param fruit pattern for the fruit
     */
    public GenerateFruitPatches(EditSession context, Region region, Pattern fruit) {
        this.context = context;
        this.region = region;
        this.fruit = fruit;
    }

    @Override
    public Operation resume(Execution opt) throws WorldEditException {
        Iterator<BlockVector> points = region.columnIterator();
        int lowestY = region.getMinimumPoint().getBlockY();
        
        while (points.hasNext()) {
            Vector columnPt = points.next();

            // Don't want to be in the ground
            if (!context.getBlock(columnPt).isAir()) {
                continue;
            }
            
            // The gods don't want a pumpkin patch here
            if (random.nextDouble() < 0.98) {
                continue;
            }

            for (int y = columnPt.getBlockY(); y >= lowestY; --y) {
                // Check if we hit the ground
                int t = context.getBlock(columnPt.setY(y)).getType();
                if (t == BlockID.GRASS || t == BlockID.DIRT) {
                    makePumpkinPatch(columnPt.setY(y + 1));
                    ++affected;
                    break;
                } else if (t != BlockID.AIR) { // Trees won't grow on this!
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Make a pumpkin patch at the given location.
     * 
     * @param basePos position
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private void makePumpkinPatch(Vector basePos) throws MaxChangedBlocksException {
        // BaseBlock logBlock = new BaseBlock(BlockID.LOG);
        BaseBlock leavesBlock = new BaseBlock(BlockID.LEAVES);

        // setBlock(basePos.subtract(0, 1, 0), logBlock);
        context.setBlockIfAir(basePos, leavesBlock);

        makePumpkinPatchVine(basePos, basePos.add(0, 0, 1));
        makePumpkinPatchVine(basePos, basePos.add(0, 0, -1));
        makePumpkinPatchVine(basePos, basePos.add(1, 0, 0));
        makePumpkinPatchVine(basePos, basePos.add(-1, 0, 0));
    }

    /**
     * Make the vine for a pumpkin patch.
     * 
     * @param basePos position to start at
     * @param pos current position
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private void makePumpkinPatchVine(Vector basePos, Vector pos) throws MaxChangedBlocksException {
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
                makePumpkinPatchVine(basePos, pos.add(1, 0, 0));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(1, h, -1), log);

            fruitPos = pos.add(0, 0, -1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 1:
            // Possibly make the vine
            if (random.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(0, 0, 1));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(1, h, 0), log);

            fruitPos = pos.add(1, 0, 1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 2:
            // Possibly make the vine
            if (random.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(0, 0, -1));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(-1, h, 0), log);
            
            fruitPos = pos.add(-1, 0, 1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;

        case 3:
            // Possibly make the vine
            if (random.nextBoolean())
                makePumpkinPatchVine(basePos, pos.add(-1, 0, 0));

            // Possibly make the log
            if (random.nextBoolean())
                context.setBlockIfAir(pos.add(-1, h, -1), log);
            
            fruitPos = pos.add(-1, 0, -1);
            context.setBlockIfAir(fruitPos, fruit.next(fruitPos));
            
            break;
        }
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }

    @Override
    public int getBlocksChanged() {
        return affected;
    }

}
