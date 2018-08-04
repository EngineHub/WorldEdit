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

package com.sk89q.worldedit.world.biome;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Functions;
import com.sk89q.worldedit.util.WeightedChoice;
import com.sk89q.worldedit.util.WeightedChoice.Choice;
import com.sk89q.worldedit.util.function.LevenshteinDistance;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Utility methods related to biomes.
 */
public final class Biomes {

    private Biomes() {
    }

    /**
     * Find a biome that matches the given input name.
     *
     * @param biomes a list of biomes
     * @param name the name to test
     * @param registry a biome registry
     * @return a biome or null
     */
    @Nullable
    public static BaseBiome findBiomeByName(Collection<BaseBiome> biomes, String name, BiomeRegistry registry) {
        checkNotNull(biomes);
        checkNotNull(name);
        checkNotNull(registry);

        Function<String, ? extends Number> compare = new LevenshteinDistance(name, false, LevenshteinDistance.STANDARD_CHARS);
        WeightedChoice<BaseBiome> chooser = new WeightedChoice<>(Functions.compose(compare::apply, new BiomeName(registry)), 0);
        for (BaseBiome biome : biomes) {
            chooser.consider(biome);
        }
        Optional<Choice<BaseBiome>> choice = chooser.getChoice();
        if (choice.isPresent() && choice.get().getScore() <= 1) {
            return choice.get().getValue();
        } else {
            return null;
        }
    }

}
