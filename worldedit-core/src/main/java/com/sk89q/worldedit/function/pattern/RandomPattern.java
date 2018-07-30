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

package com.sk89q.worldedit.function.pattern;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Uses a random pattern of a weighted list of patterns.
 */
public class RandomPattern extends AbstractPattern {

    private final Random random = new Random();
    private List<Chance> patterns = new ArrayList<>();
    private double max = 0;

    /**
     * Add a pattern to the weight list of patterns.
     *
     * <p>The probability for the pattern added is chance / max where max is
     * the sum of the probabilities of all added patterns.</p>
     *
     * @param pattern the pattern
     * @param chance the chance, which can be any positive number
     */
    public void add(Pattern pattern, double chance) {
        checkNotNull(pattern);
        patterns.add(new Chance(pattern, chance));
        max += chance;
    }

    @Override
    public BlockStateHolder apply(Vector position) {
        double r = random.nextDouble();
        double offset = 0;

        for (Chance chance : patterns) {
            if (r <= (offset + chance.getChance()) / max) {
                return chance.getPattern().apply(position);
            }
            offset += chance.getChance();
        }

        throw new RuntimeException("ProportionalFillPattern");
    }

    private static class Chance {
        private Pattern pattern;
        private double chance;

        private Chance(Pattern pattern, double chance) {
            this.pattern = pattern;
            this.chance = chance;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public double getChance() {
            return chance;
        }
    }

}
