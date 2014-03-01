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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.operation.RegionFunction;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.RandomFillPattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates flora (which may include tall grass, flowers, etc.).
 * <p>
 * The current implementation is not biome-aware, but it may become so in
 * the future.
 */
public class FloraGenerator implements RegionFunction {

    private final EditSession editSession;
    private boolean biomeAware = false;
    private final Pattern desertPattern = getDesertPattern();
    private final Pattern temperatePattern = getTemperatePattern();

    /**
     * Create a new flora generator.
     *
     * @param editSession the edit session
     */
    public FloraGenerator(EditSession editSession) {
        this.editSession = editSession;
    }

    /**
     * Return whether the flora generator is set to be biome-aware.
     * <p>
     * By default, it is currently disabled by default, but this may change.
     *
     * @return true if biome aware
     */
    public boolean isBiomeAware() {
        return biomeAware;
    }

    /**
     * Set whether the generator is biome aware.
     * <p>
     * It is currently not possible to make the generator biome-aware.
     *
     * @param biomeAware must always be false
     */
    public void setBiomeAware(boolean biomeAware) {
        if (biomeAware) {
            throw new IllegalArgumentException("Cannot enable biome-aware mode; not yet implemented");
        }
        this.biomeAware = biomeAware;
    }

    /**
     * Get a pattern for plants to place inside a desert environment.
     *
     * @return a pattern that places flora
     */
    public static Pattern getDesertPattern() {
        List<BlockChance> chance = new ArrayList<BlockChance>();
        chance.add(new BlockChance(new BaseBlock(BlockID.DEAD_BUSH), 30));
        chance.add(new BlockChance(new BaseBlock(BlockID.CACTUS), 20));
        chance.add(new BlockChance(new BaseBlock(BlockID.AIR), 300));
        return new RandomFillPattern(chance);
    }

    /**
     * Get a pattern for plants to place inside a temperate environment.
     *
     * @return a pattern that places flora
     */
    public static Pattern getTemperatePattern() {
        List<BlockChance> chance = new ArrayList<BlockChance>();
        chance.add(new BlockChance(new BaseBlock(BlockID.LONG_GRASS, 1), 300));
        chance.add(new BlockChance(new BaseBlock(BlockID.RED_FLOWER), 5));
        chance.add(new BlockChance(new BaseBlock(BlockID.YELLOW_FLOWER), 5));
        return new RandomFillPattern(chance);
    }

    @Override
    public boolean apply(Vector position) throws WorldEditException {
        BaseBlock block = editSession.getBlock(position);

        if (block.getType() == BlockID.GRASS) {
            editSession.setBlock(position.add(0, 1, 0), temperatePattern.next(position));
            return true;
        } else if (block.getType() == BlockID.SAND) {
            editSession.setBlock(position.add(0, 1, 0), desertPattern.next(position));
            return true;
        }

        return false;
    }

}
