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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Random;

/**
 * Generates patches of fruit (i.e. pumpkin patches).
 */
public class GardenPatchGenerator implements RegionFunction {

    private final Random random = new Random();
    private final EditSession editSession;
    private Pattern plant = getPumpkinPattern();
    private Pattern leafPattern = BlockTypes.OAK_LEAVES.getDefaultState().with(BlockTypes.OAK_LEAVES.getProperty("persistent"), true);
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
    public int getAffected() {
        return affected;
    }

    /**
     * Make a patch vine.
     *
     * @param basePos the base position
     * @param pos the vine position
     */
    private void placeVine(BlockVector3 basePos, BlockVector3 pos) throws MaxChangedBlocksException {
        if (pos.distance(basePos) > 4) return;
        if (!editSession.getBlock(pos).getBlockType().getMaterial().isAir()) return;

        for (int i = -1; i > -3; --i) {
            BlockVector3 testPos = pos.add(0, i, 0);
            if (editSession.getBlock(testPos).getBlockType().getMaterial().isAir()) {
                pos = testPos;
            } else {
                break;
            }
        }

        setBlockIfAir(editSession, pos, leafPattern);
        affected++;

        int t = random.nextInt(4);
        int h = random.nextInt(3) - 1;
        BlockVector3 p;

        BlockState log = BlockTypes.OAK_LOG.getDefaultState();

        switch (t) {
            case 0:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(1, 0, 0));
                }
                if (random.nextBoolean()) {
                    setBlockIfAir(editSession, pos.add(1, h, -1), log);
                    affected++;
                }
                setBlockIfAir(editSession, p = pos.add(0, 0, -1), plant.apply(p));
                affected++;
                break;

            case 1:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(0, 0, 1));
                }
                if (random.nextBoolean()) {
                    setBlockIfAir(editSession, pos.add(1, h, 0), log);
                    affected++;
                }
                setBlockIfAir(editSession, p = pos.add(1, 0, 1), plant.apply(p));
                affected++;
                break;

            case 2:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(0, 0, -1));
                }
                if (random.nextBoolean()) {
                    setBlockIfAir(editSession, pos.add(-1, h, 0), log);
                    affected++;
                }
                setBlockIfAir(editSession, p = pos.add(-1, 0, 1), plant.apply(p));
                affected++;
                break;

            case 3:
                if (random.nextBoolean()) {
                    placeVine(basePos, pos.add(-1, 0, 0));
                }
                if (random.nextBoolean()) {
                    setBlockIfAir(editSession, pos.add(-1, h, -1), log);
                    affected++;
                }
                setBlockIfAir(editSession, p = pos.add(-1, 0, -1), plant.apply(p));
                affected++;
                break;
        }
    }

    @Override
    public boolean apply(BlockVector3 position) throws WorldEditException {
        if (!editSession.getBlock(position).getBlockType().getMaterial().isAir()) {
            position = position.add(0, 1, 0);
        }

        if (editSession.getBlock(position.add(0, -1, 0)).getBlockType() != BlockTypes.GRASS_BLOCK) {
            return false;
        }


        if (editSession.getBlock(position).getBlockType().getMaterial().isAir()) {
            editSession.setBlock(position, leafPattern);
        }

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
        return BlockTypes.PUMPKIN.getDefaultState();
    }

    /**
     * Set a block only if there's no block already there.
     *
     * @param position the position
     * @param pattern the pattern to set
     * @return if block was changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private static boolean setBlockIfAir(EditSession session, BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
        return session.getBlock(position).getBlockType().getMaterial().isAir() && session.setBlock(position, pattern);
    }

    /**
     * Get a pattern that creates melons.
     *
     * @return a melon pattern
     */
    public static Pattern getMelonPattern() {
        return BlockTypes.MELON.getDefaultState();
    }
}
