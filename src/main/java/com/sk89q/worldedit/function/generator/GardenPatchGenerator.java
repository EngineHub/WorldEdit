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

package com.sk89q.worldedit.function.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.AffectedCounter;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;

import java.util.Random;

/**
 * Generates patches of fruit (i.e. pumpkin patches).
 */
public class GardenPatchGenerator implements RegionFunction, AffectedCounter {

    private final Random random = new Random();
    private final EditSession editSession;
    private Pattern plant = getPumpkinPattern();
    private int affected;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     */
    public GardenPatchGenerator(EditSession editSession) {
        this.editSession = editSession;
    }

    /**
     * Get the plant pattern that is placed.
     *
     * @return the plant pattern
     */
    public Pattern getPlant() {
        return plant;
    }

    /**
     * Set the plant pattern that is placed.
     *
     * @param plant the plant pattern
     */
    public void setPlant(Pattern plant) {
        this.plant = plant;
    }

    /**
     * Get the number of affected blocks.
     *
     * @return affected count
     */
    @Override
    public int getAffected() {
        return affected;
    }

    /**
     * Make a patch vine.
     *
     * @param basePos the base position
     * @param pos the vine position
     */
    private void placeVine(Vector basePos, Vector pos) throws MaxChangedBlocksException {
        if (pos.distance(basePos) > 4) return;
        if (editSession.getBlockType(pos) != 0) return;

        for (int i = -1; i > -3; --i) {
            Vector testPos = pos.add(0, i, 0);
            if (editSession.getBlockType(testPos) == BlockID.AIR) {
                pos = testPos;
            } else {
                break;
            }
        }

        editSession.setBlockIfAir(pos, new BaseBlock(BlockID.LEAVES));
        affected++;

        int t = random.nextInt(4);
        int h = random.nextInt(3) - 1;
        Vector p;

        BaseBlock log = new BaseBlock(BlockID.LOG);

        switch (t) {
            case 0:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(1, 0, 0));
                }
                if (random.nextBoolean()) {
                    editSession.setBlockIfAir(pos.add(1, h, -1), log);
                    affected++;
                }
                editSession.setBlockIfAir(p = pos.add(0, 0, -1), plant.apply(p));
                affected++;
                break;

            case 1:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(0, 0, 1));
                }
                if (random.nextBoolean()) {
                    editSession.setBlockIfAir(pos.add(1, h, 0), log);
                    affected++;
                }
                editSession.setBlockIfAir(p = pos.add(1, 0, 1), plant.apply(p));
                affected++;
                break;

            case 2:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(0, 0, -1));
                }
                if (random.nextBoolean()) {
                    editSession.setBlockIfAir(pos.add(-1, h, 0), log);
                    affected++;
                }
                editSession.setBlockIfAir(p = pos.add(-1, 0, 1), plant.apply(p));
                affected++;
                break;

            case 3:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(-1, 0, 0));
                }
                if (random.nextBoolean()) {
                    editSession.setBlockIfAir(pos.add(-1, h, -1), log);
                    affected++;
                }
                editSession.setBlockIfAir(p = pos.add(-1, 0, -1), plant.apply(p));
                affected++;
                break;
        }
    }

    @Override
    public boolean apply(Vector position) throws WorldEditException {
        if (editSession.getBlock(position).getType() != BlockID.AIR) {
            position = position.add(0, 1, 0);
        }

        if (editSession.getBlock(position.add(0, -1, 0)).getType() != BlockID.GRASS) {
            return false;
        }

        BaseBlock leavesBlock = new BaseBlock(BlockID.LEAVES);

        editSession.setBlockIfAir(position, leavesBlock);

        placeVine(position, position.add(0, 0, 1));
        placeVine(position, position.add(0, 0, -1));
        placeVine(position, position.add(1, 0, 0));
        placeVine(position, position.add(-1, 0, 0));

        return true;
    }

    /**
     * Get a pattern that creates pumpkins with different faces.
     *
     * @return a pumpkin pattern
     */
    public static Pattern getPumpkinPattern() {
        RandomPattern pattern = new RandomPattern();
        for (int i = 0; i < 4; i++) {
            pattern.add(new BlockPattern(new BaseBlock(BlockID.PUMPKIN, i)), 100);
        }
        return pattern;
    }

    /**
     * Get a pattern that creates melons.
     *
     * @return a melon pattern
     */
    public static Pattern getMelonPattern() {
        return new BlockPattern(new BaseBlock(BlockID.MELON_BLOCK));
    }
}
